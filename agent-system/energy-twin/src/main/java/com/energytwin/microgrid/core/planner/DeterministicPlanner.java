package com.energytwin.microgrid.core.planner;

import com.energytwin.microgrid.core.aggregator.AggregatorMetaStore;
import com.energytwin.microgrid.core.scenario.Scenario;
import com.google.ortools.Loader;
import com.google.ortools.linearsolver.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** LP on the median scenario; returns an Action list. */
public final class DeterministicPlanner {

    private final int H;                        // horizon
    private final Map<String, AggregatorMetaStore.BatteryMeta> bats;
    private final double extCapKw;

    public DeterministicPlanner(int horizon,
                                Map<String,AggregatorMetaStore.BatteryMeta> bats,
                                double extCapKw){
        this.H = horizon; this.bats = bats; this.extCapKw = extCapKw;
        Loader.loadNativeLibraries();          // OR-Tools JNI
    }

    public List<Action> solve(Scenario median, double socNow){
        MPSolver solver = MPSolver.createSolver("GLOP");  // LP
        if (solver==null) throw new IllegalStateException("No LP solver");

        /* ---- variables -------------------------------------------------- */
        Map<String, MPVariable[]> chg = new java.util.HashMap<>();
        Map<String, MPVariable[]> dsg = new java.util.HashMap<>();
        Map<String, MPVariable[]> soc = new java.util.HashMap<>();

        for (var e : bats.entrySet()){
            String id = e.getKey();
            var m = e.getValue();

            chg.put(id, solver.makeNumVarArray(H, 0, m.cRate()*m.capacity(), id+"_c"));
            dsg.put(id, solver.makeNumVarArray(H, 0, m.cRate()*m.capacity(), id+"_d"));
            soc.put(id, solver.makeNumVarArray(H+1, 0, m.capacity(), id+"_e"));
            // SoC dynamics
            for(int k=0;k<H;k++){
                double ηc = m.etaC(), ηd = m.etaD();
                MPConstraint c = solver.makeConstraint(0,0,"soc_"+id+"_"+k);
                c.setCoefficient(soc.get(id)[k+1], 1);
                c.setCoefficient(soc.get(id)[k], -1);
                c.setCoefficient(chg.get(id)[k], -ηc);
                c.setCoefficient(dsg.get(id)[k], +1/ηd);
            }
            // initial SoC = socNow split equally (simple)
            soc.get(id)[0].setBounds(socNow / bats.size(),
                    socNow / bats.size());
        }

        // external import (+) / spill (–)
        MPVariable[] ext = solver.makeNumVarArray(H, -extCapKw, extCapKw, "Ext");

        /* ---- power balance ------------------------------------------------ */
        for(int k=0;k<H;k++){
            MPConstraint bal = solver.makeConstraint(0,0,"bal_"+k);
            bal.setCoefficient(ext[k], 1);
            bal.setBounds(median.loadKw()[k]-median.pvKw()[k],
                    median.loadKw()[k]-median.pvKw()[k]);
            for (var id : bats.keySet()){
                bal.setCoefficient(chg.get(id)[k], +1);
                bal.setCoefficient(dsg.get(id)[k], -1);
            }
        }

        /* ---- objective: minimise energy loss ----------------------------- */
        MPObjective obj = solver.objective();
        for(int k=0;k<H;k++){
            obj.setCoefficient(ext[k], 1);            // import cost 1
            for (var id : bats.keySet()){
                obj.setCoefficient(chg.get(id)[k], 0.01); // charge loss proxy
                obj.setCoefficient(dsg.get(id)[k], 0.01); // discharge loss
            }
        }
        obj.setMinimization();

        /* ---- solve ------------------------------------------------------- */
        if (solver.solve() != MPSolver.ResultStatus.OPTIMAL) return List.of();

        /* ---- translate to Action list ------------------------------------ */
        List<Action> actions = new ArrayList<>();
        for(int k=0;k<H;k++){
            for (var id : bats.keySet()){
                double c = chg.get(id)[k].solutionValue();
                double d = dsg.get(id)[k].solutionValue();
                if (c>1e-3) actions.add(new Action(k,id, +c, 0));
                if (d>1e-3) actions.add(new Action(k,id, -d, 0));
            }
            double ex = ext[k].solutionValue();
            if (Math.abs(ex)>1e-3)
                actions.add(new Action(k,"External",0,ex));
        }
        return actions;
    }
}
