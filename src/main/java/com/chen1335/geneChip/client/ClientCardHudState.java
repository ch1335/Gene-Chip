package com.chen1335.geneChip.client;

import com.chen1335.geneChip.network.CardFeedbackPacket;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public final class ClientCardHudState {
    private static final List<CooldownEntry> COOLDOWNS = new ArrayList<>();
    public static boolean canDoubleJump = true;
    public static boolean tacticalRolling;
    public static int photosynthesisStacks;
    public static int photosynthesisTimer;
    public static int photosynthesisInterval = 600;
    public static int photosynthesisMaxStacks = 5;
    public static boolean photosynthesisCharging;
    public static int counterStormTicks;
    public static float counterStormDamage;
    public static int comboCount;
    public static int comboWindowTicks;
    public static int comboWindowDuration;
    public static int comboFeverTicks;
    public static boolean adrenalEquipped;
    public static boolean adrenalReady;
    public static boolean growingFervorEquipped;
    public static int growingFervorStage;
    public static boolean thickSkinnedActive;
    public static boolean infectedInZone;
    public static int statusPulseTicks;
    private static boolean locksmithScreenActive;
    public static int feedbackTicks;
    public static int feedbackValue;
    public static CardFeedbackPacket.FeedbackType feedbackType;

    private ClientCardHudState() {
    }

    public static void applySnapshot(CompoundTag state) {
        COOLDOWNS.clear();
        ListTag list = state.getList("cooldowns", Tag.TAG_COMPOUND);
        for (Tag value : list) {
            CompoundTag entry = (CompoundTag) value;
            COOLDOWNS.add(new CooldownEntry(ResourceLocation.parse(entry.getString("chip")), entry.getInt("remaining"), entry.getInt("total")));
        }
        canDoubleJump = state.getBoolean("canDoubleJump");
        tacticalRolling = state.getBoolean("tacticalRolling");
        photosynthesisStacks = state.getInt("photosynthesisStacks");
        photosynthesisTimer = state.getInt("photosynthesisTimer");
        photosynthesisInterval = Math.max(1, state.getInt("photosynthesisInterval"));
        photosynthesisMaxStacks = Math.max(1, state.getInt("photosynthesisMaxStacks"));
        photosynthesisCharging = state.getBoolean("photosynthesisCharging");
        counterStormTicks = state.getInt("counterStormTicks");
        counterStormDamage = state.getFloat("counterStormDamage");
        comboCount = state.getInt("comboCount");
        comboWindowTicks = state.getInt("comboWindowTicks");
        comboWindowDuration = state.getInt("comboWindowDuration");
        comboFeverTicks = state.getInt("comboFeverTicks");
        adrenalEquipped = state.getBoolean("adrenalEquipped");
        adrenalReady = state.getBoolean("adrenalReady");
        growingFervorEquipped = state.getBoolean("growingFervorEquipped");
        growingFervorStage = state.getInt("growingFervorStage");
        thickSkinnedActive = state.getBoolean("thickSkinnedActive");
        infectedInZone = state.getBoolean("infectedInZone");
    }

    public static void tick() {
        COOLDOWNS.replaceAll(entry -> entry.remainingTicks > 0 ? new CooldownEntry(entry.chip, entry.remainingTicks - 1, entry.totalTicks) : entry);
        COOLDOWNS.removeIf(entry -> entry.remainingTicks <= 0);
        if (counterStormTicks > 0) counterStormTicks--;
        if (comboWindowTicks > 0) comboWindowTicks--;
        if (comboFeverTicks > 0) comboFeverTicks--;
        if (statusPulseTicks > 0) statusPulseTicks--;
        if (feedbackTicks > 0) feedbackTicks--;
    }

    public static void onFeedback(CardFeedbackPacket.FeedbackType type, int value) {
        feedbackType = type;
        feedbackValue = value;
        feedbackTicks = type == CardFeedbackPacket.FeedbackType.COMBO_TRIGGERED ? 30 : 20;
        if (type == CardFeedbackPacket.FeedbackType.GROWING_FERVOR_STAGE) {
            growingFervorStage = value;
            statusPulseTicks = 20;
        }
    }

    public static void markLocksmithScreen() {
        locksmithScreenActive = true;
    }

    public static boolean shouldRenderLocksmithScreenIcon() {
        return locksmithScreenActive;
    }

    public static void clearLocksmithScreen() {
        locksmithScreenActive = false;
    }

    public static List<CooldownEntry> cooldowns() {
        return List.copyOf(COOLDOWNS);
    }

    public record CooldownEntry(ResourceLocation chip, int remainingTicks, int totalTicks) {
    }
}
