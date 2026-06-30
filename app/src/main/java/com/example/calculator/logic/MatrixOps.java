package com.example.calculator.logic;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MatrixOps {
    public double[][] data;
    public int r, c;

    public MatrixOps(String s) {
        s = s.replace("[", "").replace("]", "").trim();
        if (s.isEmpty()) throw new RuntimeException("Empty matrix");
        
        String[] rows = s.split("[;\n]");
        List<String[]> filteredData = new ArrayList<>();
        
        for (String row : rows) {
            String trimmedRow = row.trim();
            if (trimmedRow.isEmpty()) continue;
            String[] cols = trimmedRow.split("[,\\s]+");
            List<String> filteredCols = new ArrayList<>();
            for (String col : cols) {
                if (!col.trim().isEmpty()) filteredCols.add(col.trim());
            }
            if (!filteredCols.isEmpty()) {
                filteredData.add(filteredCols.toArray(new String[0]));
            }
        }

        r = filteredData.size();
        if (r == 0) throw new RuntimeException("Empty matrix");
        c = filteredData.get(0).length;
        data = new double[r][c];
        
        for (int i = 0; i < r; i++) {
            String[] cols = filteredData.get(i);
            if (cols.length != c) throw new RuntimeException("Matrix rows must have same length");
            for (int j = 0; j < c; j++) {
                data[i][j] = Double.parseDouble(cols[j]);
            }
        }
    }

    public MatrixOps(double[][] d) {
        data = d;
        r = d.length;
        c = d[0].length;
    }

    public static MatrixOps identity(int n) {
        double[][] d = new double[n][n];
        for (int i = 0; i < n; i++) d[i][i] = 1;
        return new MatrixOps(d);
    }

    public MatrixOps add(MatrixOps o) {
        if (r != o.r || c != o.c) throw new RuntimeException("Dimension mismatch");
        double[][] res = new double[r][c];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) res[i][j] = data[i][j] + o.data[i][j];
        return new MatrixOps(res);
    }

    public MatrixOps sub(MatrixOps o) {
        if (r != o.r || c != o.c) throw new RuntimeException("Dimension mismatch");
        double[][] res = new double[r][c];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) res[i][j] = data[i][j] - o.data[i][j];
        return new MatrixOps(res);
    }

    public MatrixOps mul(MatrixOps o) {
        if (c != o.r) throw new RuntimeException("Dimension mismatch");
        double[][] res = new double[r][o.c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < o.c; j++)
                for (int k = 0; k < c; k++) res[i][j] += data[i][k] * o.data[k][j];
        return new MatrixOps(res);
    }

    public MatrixOps scalar(double s) {
        double[][] res = new double[r][c];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) res[i][j] = data[i][j] * s;
        return new MatrixOps(res);
    }

    public MatrixOps transpose() {
        double[][] res = new double[c][r];
        for (int i = 0; i < r; i++) for (int j = 0; j < c; j++) res[j][i] = data[i][j];
        return new MatrixOps(res);
    }

    public double det() {
        if (r != c) throw new RuntimeException("Matrix must be square");
        return det(data);
    }

    private double det(double[][] m) {
        if (m.length == 1) return m[0][0];
        if (m.length == 2) return m[0][0] * m[1][1] - m[0][1] * m[1][0];
        double d = 0;
        for (int j = 0; j < m.length; j++) d += Math.pow(-1, j) * m[0][j] * det(subM(m, 0, j));
        return d;
    }

    private double[][] subM(double[][] m, int row, int col) {
        double[][] res = new double[m.length - 1][m.length - 1];
        for (int i = 0, ri = 0; i < m.length; i++) {
            if (i == row) continue;
            for (int j = 0, ci = 0; j < m.length; j++) {
                if (j == col) continue;
                res[ri][ci++] = m[i][j];
            }
            ri++;
        }
        return res;
    }

    public MatrixOps inv() {
        double d = det();
        if (Math.abs(d) < 1e-9) throw new RuntimeException("Matrix is singular");
        if (r == 1) return new MatrixOps(new double[][]{{1 / data[0][0]}});
        double[][] res = new double[r][c];
        for (int i = 0; i < r; i++)
            for (int j = 0; j < c; j++)
                res[j][i] = Math.pow(-1, i + j) * det(subM(data, i, j)) / d;
        return new MatrixOps(res);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < r; i++) {
            sb.append("[ ");
            for (int j = 0; j < c; j++) {
                double val = data[i][j];
                String s;
                if (val == (long)val) s = String.valueOf((long)val);
                else s = String.format(Locale.US, "%.2f", val);
                
                sb.append(s);
                if (j < c - 1) sb.append("  ");
            }
            sb.append(" ]");
            if (i < r - 1) sb.append("\n");
        }
        return sb.toString();
    }
}
