package com.chen1335.geneChip.chip.chipConfig;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.gui.GuiUtil;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;

import javax.script.ScriptException;
import javax.script.SimpleBindings;
import java.util.HashMap;

public class JsValueCalculator {
    private final Int2FloatOpenHashMap CapturedValue = new Int2FloatOpenHashMap();
    private final String defaultCalculator;

    private String calculator;
    private final boolean isPercentage;

    public JsValueCalculator(String calculator, boolean isPercentage) {
        this.defaultCalculator = calculator;
        this.calculator = calculator;
        this.isPercentage = isPercentage;
    }


    public JsValueCalculator(String calculator) {
        this(calculator, false);
    }

    public String getDefaultCalculator() {
        return defaultCalculator;
    }

    public String getCalculator() {
        return calculator;
    }

    public void setCalculator(String calculator) {
        this.calculator = calculator;
    }

    public void restCalculator() {
        calculator = defaultCalculator;
    }

    public float getValue(int lvl) {
        return CapturedValue.computeIfAbsent(lvl, lvl1 -> {
            try {
                HashMap<String, Object> map = new HashMap<>();
                map.put("lvl", lvl);
                Number value = (Number) GeneChip.JS_ENGINE.eval(calculator, new SimpleBindings(map));
                return value.floatValue();
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }
        });
    }

    public boolean changesBetweenLevels(int firstLevel, int secondLevel) {
        float firstValue = getValue(firstLevel);
        float secondValue = getValue(secondLevel);
        return Float.isFinite(firstValue)
                && Float.isFinite(secondValue)
                && Math.abs(secondValue - firstValue) > 1.0E-6F;
    }

    public String getArgValue(int lvl) {
        float value = getValue(lvl);
        if (isPercentage) {
            value = value * 100;
            return GuiUtil.format(value, 1)+"%";
        }
        return GuiUtil.format(value, 1);
    }

    public void cleanCapturedValue() {
        CapturedValue.clear();
    }
}
