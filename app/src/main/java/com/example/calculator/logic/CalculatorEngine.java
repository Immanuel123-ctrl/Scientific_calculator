package com.example.calculator.logic;

public class CalculatorEngine {

    public static boolean isDegree = true;

    public static Object evaluate(final String expr) {
        return new Object() {
            int pos = -1, ch;

            void nextChar() {
                ch = (++pos < expr.length()) ? expr.charAt(pos) : -1;
            }

            boolean eat(int charToEat) {
                while (ch == ' ') nextChar();
                if (ch == charToEat) {
                    nextChar();
                    return true;
                }
                return false;
            }

            Object parse() {
                nextChar();
                Object x = parseExpression();
                if (pos < expr.length()) throw new RuntimeException("Unexpected: " + (char) ch);
                return x;
            }

            Object parseExpression() {
                Object x = parseTerm();
                for (;;) {
                    if (eat('+')) x = add(x, parseTerm());
                    else if (eat('-')) x = subtract(x, parseTerm());
                    else return x;
                }
            }

            Object parseTerm() {
                Object x = parseFactor();
                for (;;) {
                    if (eat('*')) x = multiply(x, parseFactor());
                    else if (eat('/')) x = divide(x, parseFactor());
                    else if (eat('%')) x = remainder(x, parseFactor());
                    else if (eat('n')) {
                        if (eat('P') && eat('r')) x = nPr(x, parseFactor());
                        else if (eat('C') && eat('r')) x = nCr(x, parseFactor());
                        else throw new RuntimeException("Unknown");
                    } else return x;
                }
            }

            Object parseFactor() {
                if (eat('+')) return parseFactor();
                if (eat('-')) return negate(parseFactor());
                Object x;
                int startPos = this.pos;
                if (eat('(')) {
                    x = parseExpression();
                    eat(')');
                } else if (eat('[')) {
                    x = parseMatrix('[', ']');
                } else if (eat('{')) {
                    x = parseStats('{', '}');
                } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                    while ((ch >= '0' && ch <= '9') || ch == '.') nextChar();
                    x = Double.parseDouble(expr.substring(startPos, this.pos));
                } else if (Character.isLetter(ch) || ch == '√') {
                    while (Character.isLetter(ch) || ch == '√') nextChar();
                    String func = expr.substring(startPos, this.pos);
                    if (eat('!')) {
                        x = factorial(parseFactor());
                    } else {
                        boolean isStatsFunc = "mean".equals(func) || "stdDev".equals(func) || "var".equals(func) || "sum".equals(func) || "min".equals(func) || "max".equals(func) || "count".equals(func);
                        if (isStatsFunc && ch == '(') {
                            nextChar();
                            x = parseStats('(', ')');
                        } else {
                            x = parseFactor();
                        }

                        if (func.equals("sin")) { double val = td(x); x = Math.sin(isDegree ? Math.toRadians(val) : val); }
                        else if (func.equals("cos")) { double val = td(x); x = Math.cos(isDegree ? Math.toRadians(val) : val); }
                        else if (func.equals("tan")) { double val = td(x); x = Math.tan(isDegree ? Math.toRadians(val) : val); }
                        else if (func.equals("asin")) {
                            double val = td(x);
                            double res = Math.asin(val);
                            x = isDegree ? Math.toDegrees(res) : res;
                        }
                        else if (func.equals("acos")) {
                            double val = td(x);
                            double res = Math.acos(val);
                            x = isDegree ? Math.toDegrees(res) : res;
                        }
                        else if (func.equals("atan")) {
                            double val = td(x);
                            double res = Math.atan(val);
                            x = isDegree ? Math.toDegrees(res) : res;
                        }
                        else if (func.equals("asinh")) { double val = td(x); x = Math.log(val + Math.sqrt(val * val + 1)); }
                        else if (func.equals("acosh")) { double val = td(x); x = Math.log(val + Math.sqrt(val * val - 1)); }
                        else if (func.equals("atanh")) { double val = td(x); x = 0.5 * Math.log((1 + val) / (1 - val)); }
                        else if (func.equals("sinh")) { double val = td(x); x = Math.sinh(val); }
                        else if (func.equals("cosh")) { double val = td(x); x = Math.cosh(val); }
                        else if (func.equals("tanh")) { double val = td(x); x = Math.tanh(val); }
                        else if (func.equals("abs")) { double val = td(x); x = Math.abs(val); }
                        else if (func.equals("√")) { double val = td(x); x = Math.sqrt(val); }
                        else if (func.equals("log")) { double val = td(x); x = Math.log10(val); }
                        else if (func.equals("ln")) { double val = td(x); x = Math.log(val); }
                        else if (func.equals("10ˣ")) { double val = td(x); x = Math.pow(10, val); }
                        else if (func.equals("eˣ")) { double val = td(x); x = Math.exp(val); }
                        else if (func.equals("√x")) { double val = td(x); x = Math.sqrt(val); } // Secondary label for sqrt
                        else if (func.equals("det")) x = tm(x).det();
                        else if (func.equals("inv")) x = tm(x).inv();
                        else if (func.equals("T")) x = tm(x).transpose();
                        else if (func.equals("id")) x = MatrixOps.identity((int) td(x));
                        else if (func.equals("mean")) x = ts(x).mean();
                        else if (func.equals("stdDev")) x = ts(x).stdDev();
                        else if (func.equals("var")) x = ts(x).var();
                        else if (func.equals("sum")) x = ts(x).sum();
                        else if (func.equals("min")) x = ts(x).min();
                        else if (func.equals("max")) x = ts(x).max();
                        else if (func.equals("count")) x = (double) ts(x).count();
                    }
                } else throw new RuntimeException("Unexpected: " + (char) ch);
                if (eat('^')) x = Math.pow(td(x), td(parseFactor()));
                if (eat('!')) x = factorial(x);
                return x;
            }

            double td(Object o) {
                return (Double) o;
            }

            MatrixOps tm(Object o) {
                return (MatrixOps) o;
            }

            StatsOps ts(Object o) {
                return (StatsOps) o;
            }

            Object add(Object a, Object b) {
                if (a instanceof Double && b instanceof Double) return (Double) a + (Double) b;
                if (a instanceof MatrixOps && b instanceof MatrixOps) return ((MatrixOps) a).add((MatrixOps) b);
                throw new RuntimeException("Operation not supported");
            }

            Object subtract(Object a, Object b) {
                if (a instanceof Double && b instanceof Double) return (Double) a - (Double) b;
                if (a instanceof MatrixOps && b instanceof MatrixOps) return ((MatrixOps) a).sub((MatrixOps) b);
                throw new RuntimeException("Operation not supported");
            }

            Object multiply(Object a, Object b) {
                if (a instanceof Double && b instanceof Double) return (Double) a * (Double) b;
                if (a instanceof MatrixOps && b instanceof MatrixOps) return ((MatrixOps) a).mul((MatrixOps) b);
                if (a instanceof Double && b instanceof MatrixOps) return ((MatrixOps) b).scalar((Double) a);
                if (a instanceof MatrixOps && b instanceof Double) return ((MatrixOps) a).scalar((Double) b);
                throw new RuntimeException("Operation not supported");
            }

            Object divide(Object a, Object b) {
                return (Double) a / (Double) b;
            }

            Object remainder(Object a, Object b) {
                return (Double) a % (Double) b;
            }

            Object negate(Object a) {
                if (a instanceof Double) return -(Double) a;
                return ((MatrixOps) a).scalar(-1.0);
            }

            Object factorial(Object a) {
                double n = td(a);
                if (n < 0 || n != (int) n) throw new RuntimeException("Factorial needs integer >= 0");
                double f = 1;
                for (int i = 1; i <= n; i++) f *= i;
                return f;
            }

            Object nPr(Object n, Object r) {
                double nn = td(n), rr = td(r);
                return (Double) factorial(nn) / (Double) factorial(nn - rr);
            }

            Object nCr(Object n, Object r) {
                double nn = td(n), rr = td(r);
                return (Double) nPr(n, r) / (Double) factorial(rr);
            }

            MatrixOps parseMatrix(char start, char end) {
                StringBuilder sb = new StringBuilder();
                int d = 1;
                while (ch != -1) {
                    if (ch == start) d++;
                    if (ch == end) {
                        d--;
                        if (d == 0) {
                            nextChar();
                            break;
                        }
                    }
                    sb.append((char) ch);
                    nextChar();
                }
                return new MatrixOps(sb.toString());
            }

            StatsOps parseStats(char start, char end) {
                StringBuilder sb = new StringBuilder();
                int d = 1;
                while (ch != -1) {
                    if (ch == start) d++;
                    if (ch == end) {
                        d--;
                        if (d == 0) {
                            nextChar();
                            break;
                        }
                    }
                    sb.append((char) ch);
                    nextChar();
                }
                return new StatsOps(sb.toString());
            }
        }.parse();
    }
}
