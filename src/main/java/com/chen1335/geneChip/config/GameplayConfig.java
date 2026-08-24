package com.chen1335.geneChip.config;

import com.chen1335.geneChip.GeneChip;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.util.Mth;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class GameplayConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final String DEFAULT_FORMULA = "100 * lvl";
    private static final int DEFAULT_MAX_LEVEL = 10;
    private static final int MAX_ALLOWED_LEVEL = 1000;
    private static final Path PATH = GeneChip.GAMEPLAY_CONFIG;

    private static int maxLevel = DEFAULT_MAX_LEVEL;
    private static String requiredExperienceFormula = DEFAULT_FORMULA;

    private GameplayConfig() {
    }

    public static void load() {
        if (!Files.exists(PATH)) {
            save();
            return;
        }
        try {
            Data data = GSON.fromJson(Files.readString(PATH), Data.class);
            maxLevel = Mth.clamp(data == null || data.maxLevel == null ? DEFAULT_MAX_LEVEL : data.maxLevel,
                    1, MAX_ALLOWED_LEVEL);
            requiredExperienceFormula = data == null || data.requiredExperience == null
                    ? DEFAULT_FORMULA : data.requiredExperience;
            if (!isValidFormula(requiredExperienceFormula)) {
                GeneChip.LOGGER.warn("Invalid chip experience formula, using default: {}", requiredExperienceFormula);
                requiredExperienceFormula = DEFAULT_FORMULA;
            }
        } catch (Exception e) {
            GeneChip.LOGGER.error("Failed to read gameplay config", e);
            maxLevel = DEFAULT_MAX_LEVEL;
            requiredExperienceFormula = DEFAULT_FORMULA;
        }
    }

    public static void save() {
        try {
            Files.createDirectories(PATH.getParent());
            Files.writeString(PATH, GSON.toJson(new Data(maxLevel, requiredExperienceFormula)));
        } catch (IOException e) {
            GeneChip.LOGGER.error("Failed to save gameplay config", e);
        }
    }

    public static int getMaxLevel() {
        return maxLevel;
    }

    public static String getRequiredExperienceFormula() {
        return requiredExperienceFormula;
    }

    public static void setMaxLevel(int value) {
        maxLevel = Mth.clamp(value, 1, MAX_ALLOWED_LEVEL);
    }

    public static void setRequiredExperienceFormula(String formula) {
        String previous = requiredExperienceFormula;
        requiredExperienceFormula = formula;
        if (!isValidFormula(formula)) {
            requiredExperienceFormula = previous;
        }
    }

    public static int requiredExperience(int level) {
        int safeLevel = Mth.clamp(level, 1, Math.max(1, maxLevel - 1));
        try {
            String expression = requiredExperienceFormula.replace("lvl", Integer.toString(safeLevel));
            double value = Double.parseDouble(GeneChip.JS_ENGINE.eval(expression).toString());
            if (!Double.isFinite(value) || value < 1) return defaultRequiredExperience(safeLevel);
            return (int) Math.min(Integer.MAX_VALUE, Math.ceil(value));
        } catch (Exception e) {
            return defaultRequiredExperience(safeLevel);
        }
    }

    public static boolean isValidFormula(String formula) {
        if (formula == null || formula.isBlank() || !formula.matches("[0-9+*/(). _lLvV-]+")) return false;
        for (int level = 1; level < maxLevel; level++) {
            try {
                String expression = formula.replace("lvl", Integer.toString(level));
                double value = Double.parseDouble(GeneChip.JS_ENGINE.eval(expression).toString());
                if (!Double.isFinite(value) || value < 1) return false;
            } catch (Exception e) {
                return false;
            }
        }
        return true;
    }

    private static int defaultRequiredExperience(int level) {
        return Math.min(Integer.MAX_VALUE, Math.max(1, level * 100));
    }

    private record Data(Integer maxLevel, String requiredExperience) {
    }
}
