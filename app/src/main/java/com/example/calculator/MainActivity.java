package com.example.calculator;

import android.os.Bundle;
import android.text.Editable;
import android.transition.ChangeBounds;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.transition.TransitionSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.calculator.R;
import com.example.calculator.logic.CalculatorEngine;
import com.example.calculator.logic.MatrixOps;
import com.example.calculator.logic.StatsOps;
import com.google.android.material.tabs.TabLayout;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private EditText etExpression;
    private TextView tvResult;
    private GridLayout gridBasic, gridMatrix, gridStats, matrixInputGrid;
    private View gridScientific, layoutMatrixEditor;
    private ViewGroup mainRoot;
    private boolean isFinalResult = false;
    private boolean is2ndMode = false;

    private TextView menuBasic, menuScientific, menuMatrix, menuStats;
    private View menuSelectorHighlight;
    private int activeMode = 0; // 0: Basic, 1: Scientific, 2: Matrix, 3: Stats
    private int matrixRows = 2, matrixCols = 2;

    private Button btn2nd, btnRadDeg, btnSin, btnCos, btnTan, btnSinh, btnCosh, btnTanh, btnSq, btnInv, btnPow, btnLog, btnLn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etExpression = findViewById(R.id.tvExpression);
        etExpression.setShowSoftInputOnFocus(false);
        tvResult = findViewById(R.id.tvResult);
        mainRoot = findViewById(R.id.mainRoot);

        gridBasic = findViewById(R.id.gridBasic);
        gridScientific = findViewById(R.id.gridScientific);
        gridMatrix = findViewById(R.id.gridMatrix);
        gridStats = findViewById(R.id.gridStats);

        layoutMatrixEditor = findViewById(R.id.layoutMatrixEditor);
        matrixInputGrid = findViewById(R.id.matrixInputGrid);

        menuBasic = findViewById(R.id.menuBasic);
        menuScientific = findViewById(R.id.menuScientific);
        menuMatrix = findViewById(R.id.menuMatrix);
        menuStats = findViewById(R.id.menuStats);
        menuSelectorHighlight = findViewById(R.id.menuSelectorHighlight);

        btn2nd = findViewById(R.id.btn2nd);
        btnRadDeg = findViewById(R.id.btnRadDeg);
        btnSin = findViewById(R.id.btnSin);
        btnCos = findViewById(R.id.btnCos);
        btnTan = findViewById(R.id.btnTan);
        btnSinh = findViewById(R.id.btnSinh);
        btnCosh = findViewById(R.id.btnCosh);
        btnTanh = findViewById(R.id.btnTanh);
        btnSq = findViewById(R.id.btnSq);
        btnInv = findViewById(R.id.btnInv);
        btnPow = findViewById(R.id.btnPow);
        btnLog = findViewById(R.id.btnLog);
        btnLn = findViewById(R.id.btnLn);

        setupMenuListeners();
        applySpringTouchListener(mainRoot);
    }

    private void setupMenuListeners() {
        View.OnClickListener listener = v -> {
            int pos = 0;
            if (v == menuBasic) pos = 0;
            else if (v == menuScientific) pos = 1;
            else if (v == menuMatrix) pos = 2;
            else if (v == menuStats) pos = 3;
            updateMode(pos);
        };
        menuBasic.setOnClickListener(listener);
        menuScientific.setOnClickListener(listener);
        menuMatrix.setOnClickListener(listener);
        menuStats.setOnClickListener(listener);
    }

    private void updateMode(int position) {
        if (activeMode == position) return;
        activeMode = position;

        // Animate highlight
        menuSelectorHighlight.post(() -> {
            View container = findViewById(R.id.menuContainer);
            int itemWidth = container.getWidth() / 4;
            ViewGroup.LayoutParams lp = menuSelectorHighlight.getLayoutParams();
            if (lp.width != itemWidth) {
                lp.width = itemWidth;
                menuSelectorHighlight.setLayoutParams(lp);
            }
            menuSelectorHighlight.animate()
                    .translationX(position * itemWidth)
                    .setDuration(300)
                    .setInterpolator(new AnticipateOvershootInterpolator(1.0f))
                    .start();
        });

        // Update Text Colors
        menuBasic.setTextColor(position == 0 ? getColor(R.color.bg_sapphire_deep) : getColor(R.color.calc_text_secondary));
        menuScientific.setTextColor(position == 1 ? getColor(R.color.bg_sapphire_deep) : getColor(R.color.calc_text_secondary));
        menuMatrix.setTextColor(position == 2 ? getColor(R.color.bg_sapphire_deep) : getColor(R.color.calc_text_secondary));
        menuStats.setTextColor(position == 3 ? getColor(R.color.bg_sapphire_deep) : getColor(R.color.calc_text_secondary));

        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .setDuration(400);
        TransitionManager.beginDelayedTransition((ViewGroup) gridBasic.getParent(), set);

        gridScientific.setVisibility(position == 1 ? View.VISIBLE : View.GONE);
        gridMatrix.setVisibility(position == 2 ? View.VISIBLE : View.GONE);
        gridStats.setVisibility(position == 3 ? View.VISIBLE : View.GONE);
        
        if (position != 2) layoutMatrixEditor.setVisibility(View.GONE);
    }

    private void insertText(String text) {
        int start = etExpression.getSelectionStart();
        int end = etExpression.getSelectionEnd();
        if (isFinalResult) {
            etExpression.setText(text);
            isFinalResult = false;
            etExpression.setSelection(etExpression.getText().length());
        } else {
            etExpression.getText().replace(Math.min(start, end), Math.max(start, end), text);
            etExpression.setSelection(Math.min(start, end) + text.length());
        }
    }

    public void onNumberClick(View view) {
        Button button = (Button) view;
        String text = button.getText().toString();
        insertText(text.equals("π") ? "π" : text);
    }

    public void onOperatorClick(View view) {
        Button button = (Button) view;
        String op = button.getText().toString();
        if (op.equals("±")) insertText("-");
        else if (op.equals("!")) insertText("!");
        else if (op.equals("n!")) insertText("!");
        else if (op.equals("x²")) insertText("^2");
        else if (op.equals("xʸ")) insertText("^");
        else if (op.equals("yˣ")) insertText("^");
        else insertText(op);
    }

    public void onClearClick(View view) {
        etExpression.setText("");
        tvResult.setText("0");
        isFinalResult = false;
    }

    public void onDeleteClick(View view) {
        int start = etExpression.getSelectionStart();
        int end = etExpression.getSelectionEnd();
        Editable editable = etExpression.getText();
        if (start != end) {
            editable.delete(Math.min(start, end), Math.max(start, end));
        } else if (start > 0) {
            editable.delete(start - 1, start);
        }
    }

    public void onBracketClick(View view) {
        Button button = (Button) view;
        insertText(button.getText().toString());
    }

    public void onMatrixKeyClick(View view) {
        Button button = (Button) view;
        insertText(button.getText().toString());
    }

    public void onScientificClick(View view) {
        Button button = (Button) view;
        String func = button.getText().toString();
        if (func.equals("1/x")) insertText("1/(");
        else if (func.equals("id")) insertText("id(");
        else insertText(func + "(");
    }

    public void onMatrixTemplateClick(View view) {
        Button button = (Button) view;
        String template = button.getText().toString();
        String[] dims = template.split("x");
        matrixRows = Integer.parseInt(dims[0]);
        matrixCols = Integer.parseInt(dims[1]);

        setupMatrixEditor();
    }

    private void setupMatrixEditor() {
        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .setDuration(400);
        TransitionManager.beginDelayedTransition(mainRoot, set);

        gridMatrix.setVisibility(View.GONE);
        layoutMatrixEditor.setVisibility(View.VISIBLE);

        matrixInputGrid.removeAllViews();
        matrixInputGrid.setRowCount(matrixRows);
        matrixInputGrid.setColumnCount(matrixCols);

        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int cellWidth = (screenWidth - 100) / Math.max(matrixCols, 4);

        for (int i = 0; i < matrixRows * matrixCols; i++) {
            EditText cell = new EditText(this);
            cell.setHint("0");
            cell.setHintTextColor(getColor(R.color.calc_text_secondary));
            cell.setTextColor(getColor(R.color.calc_text_primary));
            cell.setBackgroundResource(R.drawable.display_background);
            cell.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#2D3A4F")));
            cell.setGravity(android.view.Gravity.CENTER);
            cell.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL | android.text.InputType.TYPE_NUMBER_FLAG_SIGNED);
            cell.setMinWidth(cellWidth);
            cell.setMaxWidth(cellWidth);
            cell.setTextSize(14);
            cell.setPadding(8, 8, 8, 8);
            matrixInputGrid.addView(cell);
        }
    }

    public void onCancelMatrix(View view) {
        TransitionSet set = new TransitionSet()
                .addTransition(new Fade())
                .addTransition(new ChangeBounds())
                .setDuration(400)
                .setInterpolator(new DecelerateInterpolator());
        TransitionManager.beginDelayedTransition(mainRoot, set);

        layoutMatrixEditor.setVisibility(View.GONE);
        gridMatrix.setVisibility(View.VISIBLE);
    }

    private void applySpringTouchListener(View view) {
        if (view instanceof Button || view instanceof ImageButton) {
            view.setOnTouchListener((v, event) -> {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        v.animate().scaleX(0.94f).scaleY(0.94f).alpha(0.8f)
                                .setDuration(100).setInterpolator(new DecelerateInterpolator()).start();
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        v.animate().scaleX(1.0f).scaleY(1.0f).alpha(1.0f)
                                .setDuration(400).setInterpolator(new AnticipateOvershootInterpolator(4.0f)).start();
                        break;
                }
                return false;
            });
        } else if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                applySpringTouchListener(group.getChildAt(i));
            }
        }
    }

    public void onApplyMatrix(View view) {
        StringBuilder sb = new StringBuilder("[ ");
        for (int i = 0; i < matrixRows; i++) {
            for (int j = 0; j < matrixCols; j++) {
                EditText cell = (EditText) matrixInputGrid.getChildAt(i * matrixCols + j);
                String val = cell.getText().toString();
                if (val.isEmpty()) val = "0";
                sb.append(val);
                if (j < matrixCols - 1) sb.append(" ");
            }
            if (i < matrixRows - 1) sb.append("; ");
        }
        sb.append(" ]");
        insertText(sb.toString());

        onCancelMatrix(null);
    }

    public void on2ndToggle(View view) {
        is2ndMode = !is2ndMode;
        if (is2ndMode) {
            btn2nd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#B45309")));
            btnSin.setText("asin"); btnCos.setText("acos"); btnTan.setText("atan");
            btnSinh.setText("asinh"); btnCosh.setText("acosh"); btnTanh.setText("atanh");
            btnSq.setText("√"); btnInv.setText("abs"); btnPow.setText("yˣ");
            btnLog.setText("10ˣ"); btnLn.setText("eˣ");
        } else {
            btn2nd.setBackgroundTintList(android.content.res.ColorStateList.valueOf(android.graphics.Color.parseColor("#334155")));
            btnSin.setText("sin"); btnCos.setText("cos"); btnTan.setText("tan");
            btnSinh.setText("sinh"); btnCosh.setText("cosh"); btnTanh.setText("tanh");
            btnSq.setText("x²"); btnInv.setText("1/x"); btnPow.setText("xʸ");
            btnLog.setText("log"); btnLn.setText("ln");
        }
    }

    public void onRadDegToggle(View view) {
        CalculatorEngine.isDegree = !CalculatorEngine.isDegree;
        btnRadDeg.setText(CalculatorEngine.isDegree ? "DEG" : "RAD");
    }

    public void onThemeToggle(View view) {
    }

    private void updateThemeIcon() {
    }

    public void onEqualClick(View view) {
        try {
            String expr = etExpression.getText().toString();
            // Basic auto-close
            int open = count(expr, '('); int close = count(expr, ')');
            while (open > close) { expr += ")"; close++; }
            int openM = count(expr, '['); int closeM = count(expr, ']');
            while (openM > closeM) { expr += "]"; closeM++; }

            Object result = CalculatorEngine.evaluate(expr);
            
            String formatted = formatResult(result);
            tvResult.setText(formatted);
            isFinalResult = true;
        } catch (Exception e) {
            tvResult.setText("Error");
        }
    }

    private int count(String s, char c) {
        int n = 0; for (char ch : s.toCharArray()) if (ch == c) n++; return n;
    }

    private String formatResult(Object r) {
        if (r instanceof Double) {
            double d = (Double) r;
            if (Double.isInfinite(d) || Double.isNaN(d)) return "Math Error";
            if (d == (long) d) return String.valueOf((long) d);
            String res = String.format(Locale.US, "%.6f", d).replaceAll("0*$", "").replaceAll("\\.$", "");
            return res;
        }
        if (r instanceof MatrixOps || r instanceof StatsOps) {
            tvResult.setTextSize(20); // Smaller font for matrices/stats
            return r.toString();
        }
        tvResult.setTextSize(36);
        return r.toString();
    }
}
