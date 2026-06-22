package com.chen1335.geneChip.compat.worldfactor;

import com.mastermarisa.world_factor.client.data.ClientFactorInfoHolder;
import com.mastermarisa.world_factor.core.GlobalFactorManager;
import net.minecraft.server.MinecraftServer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class WorldFactorSynergy {

    public static boolean isColdWave() { return isFactorActive("cold_wave"); }
    public static boolean isZombieRiot() { return isFactorActive("zombie_riot"); }
    public static boolean isBloodMoon() { return isFactorActive("blood_moon"); }
    public static boolean isSignsOfFamine() { return isFactorActive("signs_of_famine"); }
    public static boolean isInfectionOverflow() { return isFactorActive("infection_overflow"); }
    public static boolean isRaysOfSunlight() { return isFactorActive("rays_of_sunlight"); }
    public static boolean isFineWeather() { return isFactorActive("fine_weather"); }
    public static boolean isAirdrop() { return isFactorActive("airdrop"); }
    public static boolean isImmunitySurge() { return isFactorActive("immunity_surge"); }

    private static boolean isFactorActive(String uid) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server != null) {
            try {
                return GlobalFactorManager.getFactorData().basicInformation().uid().equals(uid);
            } catch (Exception ignored) {}
        }
        // 客户端回退：使用 ClientFactorInfoHolder
        return ClientFactorInfoHolder.getUID().equals(uid);
    }
}
