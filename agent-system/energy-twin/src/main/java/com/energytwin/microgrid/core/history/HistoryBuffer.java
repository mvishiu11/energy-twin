package com.energytwin.microgrid.core.history;

/**
 * Ring–buffer that stores the last <code>len</code> ticks of four time-series:
 * load [kW], pv [kW], ambient temperature [°C] and battery SoC [kWh].
 * All getters return the series in chronological order
 * (oldest → newest).
 */
public final class HistoryBuffer {

    private final int len;
    private final double[] load, pv, irr, temp, soc;
    private int head = 0;          // next cell to overwrite
    private int count = 0;

    public HistoryBuffer(int len) {
        this.len = len;
        load = new double[len];  pv  = new double[len];
        irr  = new double[len];  temp = new double[len];
        soc  = new double[len];
    }

    /** push newest sample (kW, kW, °C, kWh) */
    public void push(double l,double p,double g,double t,double s) {
        load[head]=l;  pv[head]=p;  irr[head]=g;  temp[head]=t; soc[head]=s;
        head = (head+1)%len;  if(count<len) count++;
    }

    public int size() { return count; }
    public boolean isFull() { return count == len; }

    public double[] getLoad() { return snapshot(load); }
    public double[] getPv()   { return snapshot(pv); }
    public double[] getTemp() { return snapshot(temp); }
    public double[] getSoc()  { return snapshot(soc); }
    public double[] getIrr()  { return snapshot(irr); }

    public double getLoadLast() { return load[head]; }
    public double getPvLast() { return pv[head]; }

    /* ---------- helpers ---------- */
    private double[] snapshot(double[] a){
        double[] out = new double[count];
        for (int i=0;i<count;i++){
            out[i] = a[(head - count + i + len) % len];
        }
        return out;
    }
}
