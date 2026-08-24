package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.config.GameplayConfig;
import net.minecraft.util.Mth;

public final class ChipProgression {
    private ChipProgression() {
    }

    public static int requiredExperience(int level) {
        return GameplayConfig.requiredExperience(level);
    }

    public static int addExperience(ChipInstance<?> instance, int amount) {
        if (amount <= 0 || instance.getLvl() >= GameplayConfig.getMaxLevel()) return 0;
        long remaining = (long) Math.max(0, instance.getExp()) + amount;
        int oldLevel = Mth.clamp(instance.getLvl(), 1, GameplayConfig.getMaxLevel());
        int level = oldLevel;
        while (level < GameplayConfig.getMaxLevel() && remaining >= requiredExperience(level)) {
            remaining -= requiredExperience(level);
            level++;
        }
        instance.setProgress(level, (int) Math.min(Integer.MAX_VALUE, remaining));
        return level - oldLevel;
    }

    public static void normalize(ChipInstance<?> instance) {
        int level = Mth.clamp(instance.getLvl(), 1, GameplayConfig.getMaxLevel());
        int exp = Math.max(0, instance.getExp());
        if (level >= GameplayConfig.getMaxLevel()) {
            instance.setProgress(GameplayConfig.getMaxLevel(), 0);
            return;
        }
        long remaining = exp;
        while (level < GameplayConfig.getMaxLevel() && remaining >= requiredExperience(level)) {
            remaining -= requiredExperience(level);
            level++;
        }
        instance.setProgress(level, (int) Math.min(Integer.MAX_VALUE, remaining));
    }
}
