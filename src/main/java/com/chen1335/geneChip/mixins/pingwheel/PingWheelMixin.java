package com.chen1335.geneChip.mixins.pingwheel;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.chips.special.HunterInstinct;
import com.chen1335.geneChip.common.GamePlayEventHandler;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import nx.pingwheel.common.core.PingType;
import nx.pingwheel.common.core.ServerCore;
import nx.pingwheel.common.network.PingLocationC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerCore.class)
public abstract class PingWheelMixin {

    @Inject(method = "onPingLocation", at = @At("HEAD"),  remap = false)
    private static void onPingLocation(MinecraftServer server, ServerPlayer player, PingLocationC2SPacket packet, CallbackInfo ci) {
        if (packet.pingType() != PingType.ATTACK || packet.entity() == null) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.HUNTER_INSTINCT).ifPresent(chipInstance -> {
            if (GeneChipAPI.isChipCooldown(player, chipInstance.getChip())) {
                return;
            }

            HunterInstinct chip = chipInstance.getChip();
            float hungerCost = chip.hungerCost.getValue(chipInstance.getLvl());
            if (player.getFoodData().getFoodLevel() < hungerCost) {
                return;
            }

            player.getFoodData().eat((int) -hungerCost, 0);

            float duration = chip.markDuration.getValue(chipInstance.getLvl());
            float damageBoost = chip.damageBoost.getValue(chipInstance.getLvl());
            long expireTick = player.level().getGameTime() + (long) (duration * 20);
            GamePlayEventHandler.markEntity(packet.entity(), damageBoost, expireTick);

            // 给目标实体添加发光效果
            Entity targetEntity = player.serverLevel().getEntity(packet.entity());
            if (targetEntity instanceof net.minecraft.world.entity.LivingEntity living) {
                int glowDuration = (int) (duration * 20);
                living.addEffect(new MobEffectInstance(MobEffects.GLOWING, glowDuration, 0, false, false));
            }

            int cooldown = (int) (chip.cooldown.getValue(chipInstance.getLvl()) * 20);
            GeneChipAPI.addChipCooldown(player, chipInstance.getChip(), cooldown);
        });
    }
}
