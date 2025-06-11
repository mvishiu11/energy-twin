package com.energytwin.microgrid.core.scenario;

import org.apache.commons.math3.distribution.MultivariateNormalDistribution;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.random.RandomGenerator;
import org.apache.commons.rng.simple.RandomSource;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** 50 draws from a Gaussian copula that couples load and PV. */
public final class MonteCarloGenerator implements ScenarioGenerator {

    private final int H, N;
    public MonteCarloGenerator(int horizon, int draws){ H=horizon; N=draws; }

    @Override public List<Scenario> generate(double[][] loadQ, double[][] pvQ){

        double[] mu = new double[2*H];
        double[] sig = new double[2*H];

        for(int k=0;k<H;k++){
            mu[k]     = loadQ[1][k];                           // mean = median
            sig[k]    = 0.5*(loadQ[2][k]-loadQ[0][k]);         // σ ≈ half-range
            mu[k+H]   = pvQ[1][k];
            sig[k+H]  = 0.5*(pvQ[2][k]-pvQ[0][k]);
        }

        /* simple exponential correlation ρ^{|Δt|}, ρ=0.6     */
        RealMatrix cov = MatrixUtils.createRealIdentityMatrix(2*H);
        final double rho=0.6;
        for(int i=0;i<2*H;i++) for(int j=0;j<2*H;j++)
            cov.setEntry(i,j, Math.pow(rho, Math.abs(i-j))*sig[i]*sig[j]);

        MultivariateNormalDistribution mvn =
                new MultivariateNormalDistribution((RandomGenerator) RandomSource.XO_RO_SHI_RO_64_S.create(), mu, cov.getData());

        List<Scenario> list = new ArrayList<>(N);
        for(int d=0; d<N; d++){
            double[] draw = mvn.sample();
            double[] L = Arrays.copyOfRange(draw,0,H);
            double[] P = Arrays.copyOfRange(draw,H,2*H);
            list.add(new Scenario(L,P, 1.0/N));
        }
        return list;
    }
}
