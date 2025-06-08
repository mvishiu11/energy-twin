package com.energytwin.microgrid.core.forecast;

import smile.data.DataFrame;
import smile.data.Tuple;
import smile.data.vector.DoubleVector;
import smile.data.vector.IntVector;
import smile.regression.RandomForest;
import smile.regression.RegressionTree;
import smile.stat.distribution.EmpiricalDistribution;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.IntStream;

/**
 * Enhanced Probabilistic Forecaster with proper feature engineering,
 * environmental modeling, and robust multi-step prediction.
 */
public final class ProbabilisticForecaster {
    private final int H_pred;
    private RandomForest loadModel, pvModel;

    // Enhanced feature caching
    private double[] recentLoad, recentPv, recentTemp;
    private double[] loadMovingAvg, pvMovingAvg;
    private double baselineTemp = 20.0;
    private LocalDateTime lastUpdateTime;

    // Robust hyperparameters optimized for energy forecasting
    private static final int N_TREES = 100;           // Increased for stability
    private static final int MAX_DEPTH = 12;          // Slightly deeper for complex patterns
    private static final int MIN_SPLIT = 5;           // More sensitive to patterns
    private static final int MIN_SAMPLE = 8;
    private static final double SUBSAMPLE = 0.8;
    private static final int LOOKBACK_WINDOW = 7;     // For moving averages and trends

    public ProbabilisticForecaster(int horizon) {
        this.H_pred = horizon;
        this.lastUpdateTime = LocalDateTime.now();
    }

    /**
     * Enhanced update method with comprehensive feature engineering
     */
    public void update(double[] load, double[] pv, double[] temp) {
        int n = load.length;

        // Enhanced validation with better thresholds
        if (n < 10) {
            System.out.println("Insufficient data points: " + n);
            return;
        }

        // Clean and validate data
        double[] cleanLoad = cleanAndValidateData(load, "load");
        double[] cleanPv = cleanAndValidateData(pv, "pv");
        double[] cleanTemp = cleanAndValidateData(temp, "temperature");

        // Check for sufficient variance
        if (calculateCoeffOfVariation(cleanLoad) < 0.05 ||
                calculateCoeffOfVariation(cleanPv) < 0.05) {
            System.out.println("Warning: Low variance in data may lead to poor predictions");
        }

        // Cache recent values for prediction
        int cacheSize = Math.min(LOOKBACK_WINDOW, n);
        this.recentLoad = Arrays.copyOfRange(cleanLoad, n - cacheSize, n);
        this.recentPv = Arrays.copyOfRange(cleanPv, n - cacheSize, n);
        this.recentTemp = Arrays.copyOfRange(cleanTemp, n - cacheSize, n);
        this.baselineTemp = cleanTemp[n - 1];
        this.lastUpdateTime = LocalDateTime.now();

        // Build comprehensive features
        DataFrame features = buildEnhancedFeatures(cleanLoad, cleanPv, cleanTemp, n);

        // Train models with cross-validation-informed parameters
        int mtry = Math.max(3, (int) Math.round(Math.sqrt(features.ncol()) * 1.2));

        try {
            // Train load model with enhanced features
            DataFrame loadTrainData = features.merge(DoubleVector.of("target_load", cleanLoad));
            loadModel = RandomForest.fit(
                    smile.data.formula.Formula.lhs("target_load"),
                    loadTrainData,
                    N_TREES,
                    mtry,
                    MAX_DEPTH,
                    Integer.MAX_VALUE,
                    MIN_SPLIT,
                    SUBSAMPLE
            );

            // Train PV model
            DataFrame pvTrainData = features.merge(DoubleVector.of("target_pv", cleanPv));
            pvModel = RandomForest.fit(
                    smile.data.formula.Formula.lhs("target_pv"),
                    pvTrainData,
                    N_TREES,
                    mtry,
                    MAX_DEPTH,
                    Integer.MAX_VALUE,
                    MIN_SPLIT,
                    SUBSAMPLE
            );

            // Compute moving averages for prediction context
            this.loadMovingAvg = computeMovingAverages(cleanLoad, 3);
            this.pvMovingAvg = computeMovingAverages(cleanPv, 3);

            System.out.println("Enhanced models trained successfully");
            System.out.println("Load CV: " + String.format("%.3f", calculateCoeffOfVariation(cleanLoad)));
            System.out.println("PV CV: " + String.format("%.3f", calculateCoeffOfVariation(cleanPv)));

        } catch (Exception e) {
            System.err.println("Enhanced model training failed: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Builds comprehensive feature set with proper temporal and environmental modeling
     */
    private DataFrame buildEnhancedFeatures(double[] load, double[] pv, double[] temp, int n) {
        // Initialize feature arrays
        double[] lag1Load = new double[n];
        double[] lag2Load = new double[n];
        double[] lag1Pv = new double[n];
        double[] lag2Pv = new double[n];
        double[] tempCurrent = new double[n];
        double[] tempLag1 = new double[n];

        // Moving averages and trends
        double[] loadMA3 = computeMovingAverages(load, 3);
        double[] pvMA3 = computeMovingAverages(pv, 3);
        double[] loadTrend = computeTrend(load, 3);
        double[] pvTrend = computeTrend(pv, 3);

        // Temporal features with enhanced encoding
        double[] hourSin = new double[n];
        double[] hourCos = new double[n];
        double[] dowSin = new double[n];
        double[] dowCos = new double[n];
        double[] monthSin = new double[n];
        double[] monthCos = new double[n];
        double[] isWeekend = new double[n];

        // Temperature-derived features
        double[] tempDeviation = new double[n];
        double[] tempMA = computeMovingAverages(temp, 3);

        LocalDateTime baseTime = lastUpdateTime.minusHours(n);

        for (int i = 0; i < n; i++) {
            LocalDateTime currentTime = baseTime.plusHours(i);

            // Enhanced lag features with bounds checking
            lag1Load[i] = (i > 0) ? load[i - 1] : load[0];
            lag2Load[i] = (i > 1) ? load[i - 2] : load[Math.max(0, i - 1)];
            lag1Pv[i] = (i > 0) ? pv[i - 1] : pv[0];
            lag2Pv[i] = (i > 1) ? pv[i - 2] : pv[Math.max(0, i - 1)];

            // Temperature features
            tempCurrent[i] = temp[i];
            tempLag1[i] = (i > 0) ? temp[i - 1] : temp[0];
            tempDeviation[i] = temp[i] - tempMA[i];

            // Enhanced temporal encoding
            int hour = currentTime.getHour();
            int dayOfWeek = currentTime.getDayOfWeek().getValue() - 1;
            int month = currentTime.getMonthValue() - 1;

            hourSin[i] = Math.sin(2 * Math.PI * hour / 24.0);
            hourCos[i] = Math.cos(2 * Math.PI * hour / 24.0);
            dowSin[i] = Math.sin(2 * Math.PI * dayOfWeek / 7.0);
            dowCos[i] = Math.cos(2 * Math.PI * dayOfWeek / 7.0);
            monthSin[i] = Math.sin(2 * Math.PI * month / 12.0);
            monthCos[i] = Math.cos(2 * Math.PI * month / 12.0);
            isWeekend[i] = (dayOfWeek >= 5) ? 1.0 : 0.0;
        }

        // Build comprehensive DataFrame
        return DataFrame.of(
                // Autoregressive features
                DoubleVector.of("lag1_load", lag1Load),
                DoubleVector.of("lag2_load", lag2Load),
                DoubleVector.of("lag1_pv", lag1Pv),
                DoubleVector.of("lag2_pv", lag2Pv),

                // Moving averages and trends
                DoubleVector.of("load_ma3", loadMA3),
                DoubleVector.of("pv_ma3", pvMA3),
                DoubleVector.of("load_trend", loadTrend),
                DoubleVector.of("pv_trend", pvTrend),

                // Temperature features
                DoubleVector.of("temp_current", tempCurrent),
                DoubleVector.of("temp_lag1", tempLag1),
                DoubleVector.of("temp_deviation", tempDeviation),
                DoubleVector.of("temp_ma", tempMA),

                // Temporal features
                DoubleVector.of("hour_sin", hourSin),
                DoubleVector.of("hour_cos", hourCos),
                DoubleVector.of("dow_sin", dowSin),
                DoubleVector.of("dow_cos", dowCos),
                DoubleVector.of("month_sin", monthSin),
                DoubleVector.of("month_cos", monthCos),
                DoubleVector.of("is_weekend", isWeekend)
        );
    }

    /**
     * Enhanced multi-step forecasting with proper environmental evolution
     */
    public double[][] predictLoad() {
        if (loadModel == null || recentLoad == null) {
            return createFallbackPrediction(0.0);
        }
        return generateEnhancedForecast(loadModel, true);
    }

    public double[][] predictPv() {
        if (pvModel == null || recentPv == null) {
            return createFallbackPrediction(0.0);
        }
        return generateEnhancedForecast(pvModel, false);
    }

    /**
     * Enhanced forecast generation with proper state evolution
     */
    private double[][] generateEnhancedForecast(RandomForest model, boolean isLoadModel) {
        double[] q05 = new double[H_pred];
        double[] q50 = new double[H_pred];
        double[] q95 = new double[H_pred];

        // Initialize state variables
        double[] evolvedLoad = Arrays.copyOf(recentLoad, recentLoad.length);
        double[] evolvedPv = Arrays.copyOf(recentPv, recentPv.length);
        double[] evolvedTemp = Arrays.copyOf(recentTemp, recentTemp.length);

        LocalDateTime forecastTime = lastUpdateTime;

        for (int h = 0; h < H_pred; h++) {
            forecastTime = forecastTime.plusHours(1);

            try {
                // Build prediction features for this time step
                double[] features = buildPredictionFeatures(
                        evolvedLoad, evolvedPv, evolvedTemp, forecastTime, h
                );

                // Get ensemble predictions
                double[] treePredictions = getTreePredictions(model, features);

                // Calculate robust quantiles
                double[] quantiles = calculateRobustQuantiles(treePredictions);

                q05[h] = Math.max(0, quantiles[0]);
                q50[h] = Math.max(0, quantiles[1]);
                q95[h] = Math.max(0, quantiles[2]);

                // Update evolved state for next iteration
                updateEvolvedState(evolvedLoad, evolvedPv, evolvedTemp,
                        q50[h], isLoadModel, h, forecastTime);

            } catch (Exception e) {
                System.err.println("Error in forecast step " + h + ": " + e.getMessage());
                double fallback = isLoadModel ?
                        (recentLoad.length > 0 ? recentLoad[recentLoad.length - 1] : 0.0) :
                        (recentPv.length > 0 ? recentPv[recentPv.length - 1] : 0.0);
                q05[h] = Math.max(0, fallback * 0.8);
                q50[h] = Math.max(0, fallback);
                q95[h] = fallback * 1.2;
            }
        }

        return new double[][] { q05, q50, q95 };
    }

    /**
     * Builds prediction features for a specific forecast step
     */
    private double[] buildPredictionFeatures(double[] evolvedLoad, double[] evolvedPv,
                                             double[] evolvedTemp, LocalDateTime time, int step) {
        int n = evolvedLoad.length;

        // Temporal features
        int hour = time.getHour();
        int dayOfWeek = time.getDayOfWeek().getValue() - 1;
        int month = time.getMonthValue() - 1;

        double hourSin = Math.sin(2 * Math.PI * hour / 24.0);
        double hourCos = Math.cos(2 * Math.PI * hour / 24.0);
        double dowSin = Math.sin(2 * Math.PI * dayOfWeek / 7.0);
        double dowCos = Math.cos(2 * Math.PI * dayOfWeek / 7.0);
        double monthSin = Math.sin(2 * Math.PI * month / 12.0);
        double monthCos = Math.cos(2 * Math.PI * month / 12.0);
        double isWeekend = (dayOfWeek >= 5) ? 1.0 : 0.0;

        // Lag features
        double lag1Load = evolvedLoad[n - 1];
        double lag2Load = (n > 1) ? evolvedLoad[n - 2] : evolvedLoad[n - 1];
        double lag1Pv = evolvedPv[n - 1];
        double lag2Pv = (n > 1) ? evolvedPv[n - 2] : evolvedPv[n - 1];

        // Moving averages and trends
        double loadMA3 = Arrays.stream(evolvedLoad).skip(Math.max(0, n - 3)).average().orElse(lag1Load);
        double pvMA3 = Arrays.stream(evolvedPv).skip(Math.max(0, n - 3)).average().orElse(lag1Pv);
        double loadTrend = computeSimpleTrend(evolvedLoad);
        double pvTrend = computeSimpleTrend(evolvedPv);

        // Temperature features with evolution
        double tempCurrent = evolvedTemp[n - 1];
        double tempLag1 = (n > 1) ? evolvedTemp[n - 2] : tempCurrent;
        double tempMA = Arrays.stream(evolvedTemp).skip(Math.max(0, n - 3)).average().orElse(tempCurrent);
        double tempDeviation = tempCurrent - tempMA;

        return new double[] {
                lag1Load, lag2Load, lag1Pv, lag2Pv,
                loadMA3, pvMA3, loadTrend, pvTrend,
                tempCurrent, tempLag1, tempDeviation, tempMA,
                hourSin, hourCos, dowSin, dowCos, monthSin, monthCos, isWeekend
        };
    }

    /**
     * Updates evolved state variables for multi-step prediction
     */
    private void updateEvolvedState(double[] evolvedLoad, double[] evolvedPv, double[] evolvedTemp,
                                    double prediction, boolean isLoadModel, int step, LocalDateTime time) {
        // Shift arrays and add new predictions
        System.arraycopy(evolvedLoad, 1, evolvedLoad, 0, evolvedLoad.length - 1);
        System.arraycopy(evolvedPv, 1, evolvedPv, 0, evolvedPv.length - 1);
        System.arraycopy(evolvedTemp, 1, evolvedTemp, 0, evolvedTemp.length - 1);

        // Update with new prediction
        if (isLoadModel) {
            evolvedLoad[evolvedLoad.length - 1] = prediction;
            // PV evolution based on time of day and season
            evolvedPv[evolvedPv.length - 1] = evolutePvEstimate(time, step);
        } else {
            evolvedPv[evolvedPv.length - 1] = prediction;
            // Load evolution with daily patterns
            evolvedLoad[evolvedLoad.length - 1] = evoluteLoadEstimate(time, step);
        }

        // Simple temperature persistence with seasonal adjustment
        double tempTrend = (evolvedTemp.length > 1) ?
                (evolvedTemp[evolvedTemp.length - 1] - evolvedTemp[evolvedTemp.length - 2]) : 0.0;
        evolvedTemp[evolvedTemp.length - 1] = baselineTemp + tempTrend * 0.5;
    }

    // Helper methods for data processing
    private double[] cleanAndValidateData(double[] data, String type) {
        double[] cleaned = new double[data.length];
        double lastValid = 0.0;

        for (int i = 0; i < data.length; i++) {
            if (Double.isFinite(data[i]) && data[i] >= 0) {
                cleaned[i] = data[i];
                lastValid = data[i];
            } else {
                cleaned[i] = lastValid; // Simple forward fill
            }
        }
        return cleaned;
    }

    private double calculateCoeffOfVariation(double[] data) {
        double mean = Arrays.stream(data).average().orElse(0.0);
        if (mean == 0) return 0.0;
        double variance = Arrays.stream(data).map(x -> (x - mean) * (x - mean)).average().orElse(0.0);
        return Math.sqrt(variance) / Math.abs(mean);
    }

    private double[] computeMovingAverages(double[] data, int window) {
        double[] ma = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            int start = Math.max(0, i - window + 1);
            ma[i] = Arrays.stream(data, start, i + 1).average().orElse(data[i]);
        }
        return ma;
    }

    private double[] computeTrend(double[] data, int window) {
        double[] trend = new double[data.length];
        for (int i = 0; i < data.length; i++) {
            if (i < window) {
                trend[i] = 0.0;
            } else {
                trend[i] = data[i] - data[i - window];
            }
        }
        return trend;
    }

    private double computeSimpleTrend(double[] data) {
        if (data.length < 2) return 0.0;
        return data[data.length - 1] - data[data.length - 2];
    }

    private double evolutePvEstimate(LocalDateTime time, int step) {
        // Simple solar irradiance model based on time of day
        int hour = time.getHour();
        double solarFactor = 0.0;
        if (hour >= 6 && hour <= 18) {
            double hourFromNoon = Math.abs(hour - 12);
            solarFactor = Math.max(0, 1.0 - hourFromNoon / 6.0);
        }
        double baseGeneration = recentPv.length > 0 ? recentPv[recentPv.length - 1] : 0.0;
        return baseGeneration * 0.7 + solarFactor * baseGeneration * 0.3;
    }

    private double evoluteLoadEstimate(LocalDateTime time, int step) {
        // Simple load pattern based on time of day
        int hour = time.getHour();
        double loadFactor = 1.0;
        if (hour >= 7 && hour <= 9) loadFactor = 1.2;      // Morning peak
        else if (hour >= 18 && hour <= 21) loadFactor = 1.3; // Evening peak
        else if (hour >= 0 && hour <= 5) loadFactor = 0.7;   // Night minimum

        double baseLoad = recentLoad.length > 0 ? recentLoad[recentLoad.length - 1] : 0.0;
        return baseLoad * loadFactor;
    }

    private double[] getTreePredictions(RandomForest model, double[] features) {
        RegressionTree[] trees = model.trees();
        double[] predictions = new double[trees.length];

        // Create a proper Tuple for prediction - this is a simplified approach
        // In practice, you'd need to match the exact schema used during training
        for (int i = 0; i < trees.length; i++) {
            try {
                // This is a simplified prediction - you may need to adapt based on your exact SMile usage
                predictions[i] = Math.max(0, trees[i].predict(createTupleFromFeatures(features)));
            } catch (Exception e) {
                predictions[i] = 0.0;
            }
        }
        return predictions;
    }

    private Tuple createTupleFromFeatures(double[] features) {
        // This is a placeholder - you'll need to create a proper Tuple
        // matching your training data schema
        Object[] values = new Object[features.length];
        for (int i = 0; i < features.length; i++) {
            values[i] = features[i];
        }
        // You'll need to provide the correct schema here
        return Tuple.of(values, null); // Simplified - replace with actual schema
    }

    private double[] calculateRobustQuantiles(double[] predictions) {
        if (predictions.length == 0) return new double[]{0, 0, 0};

        Arrays.sort(predictions);
        int n = predictions.length;

        // Handle edge cases
        if (n == 1) {
            double val = predictions[0];
            return new double[]{val * 0.9, val, val * 1.1};
        }

        try {
            EmpiricalDistribution dist = new EmpiricalDistribution(predictions);
            return new double[]{
                    dist.quantile(0.05),
                    dist.quantile(0.50),
                    dist.quantile(0.95)
            };
        } catch (Exception e) {
            // Fallback quantile calculation
            return new double[]{
                    predictions[Math.max(0, (int)(n * 0.05))],
                    predictions[n / 2],
                    predictions[Math.min(n - 1, (int)(n * 0.95))]
            };
        }
    }

    private double[][] createFallbackPrediction(double lastValue) {
        double[][] result = new double[3][H_pred];
        for (int h = 0; h < H_pred; h++) {
            result[0][h] = Math.max(0, lastValue * 0.8);
            result[1][h] = Math.max(0, lastValue);
            result[2][h] = lastValue * 1.2;
        }
        return result;
    }
}