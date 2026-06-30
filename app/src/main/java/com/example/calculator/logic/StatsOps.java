package com.example.calculator.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class StatsOps {
    public double[] values;

    public StatsOps(String s) {
        String[] parts = s.split(",");
        List<Double> list = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim().replace("{", "").replace("}", "");
            if (!trimmed.isEmpty()) {
                try {
                    list.add(Double.parseDouble(trimmed));
                } catch (NumberFormatException ignored) {}
            }
        }
        values = new double[list.size()];
        for (int i = 0; i < list.size(); i++) values[i] = list.get(i);
    }

    public double sum() {
        double s = 0;
        for (double v : values) s += v;
        return s;
    }

    public double mean() {
        if (values.length == 0) return 0;
        return sum() / values.length;
    }

    public double var() {
        if (values.length == 0) return 0;
        double m = mean(), v = 0;
        for (double x : values) v += Math.pow(x - m, 2);
        return v / values.length;
    }

    public double stdDev() {
        return Math.sqrt(var());
    }

    public double min() {
        if (values.length == 0) return 0;
        double m = values[0];
        for (double v : values) if (v < m) m = v;
        return m;
    }

    public double max() {
        if (values.length == 0) return 0;
        double m = values[0];
        for (double v : values) if (v > m) m = v;
        return m;
    }

    public int count() {
        return values.length;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("{ ");
        for (int i = 0; i < values.length; i++) {
            double v = values[i];
            if (v == (long)v) sb.append((long)v);
            else sb.append(String.format(Locale.US, "%.2f", v));
            if (i < values.length - 1) sb.append(", ");
        }
        return sb.append(" }").toString();
    }
}
