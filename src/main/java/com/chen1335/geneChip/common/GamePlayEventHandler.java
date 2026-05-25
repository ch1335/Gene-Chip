package com.chen1335.geneChip.common;

import com.atsuishio.superbwarfare.api.event.ProjectileHitEvent;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.tags.EntityTypeTags;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.chips.combat.*;
import com.chen1335.geneChip.chip.chips.mutation.AdrenalGlandBurst;
import com.chen1335.geneChip.chip.chips.tactics.SilentWalker;
import com.chen1335.geneChip.chip.chips.tactics.SpiderClimb;
import com.chen1335.geneChip.chip.chips.tactics.TacticalRoll;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;

@EventBusSubscriber(modid = GeneChip.MODID)
public class GamePlayEventHandler {

    @SubscribeEvent
    public static void CriticalHitEvent(CriticalHitEvent event) {
        if (event.getTarget() instanceof LivingEntity target && event.getTarget().getType().is(EntityTypeTags.HEAD_SHOT_HUNTER_TARGET)) {
            GeneChipAPI.getPlayerEquippedChip(event.getEntity(), ChipTypes.HEAD_SHOT_HUNTER).ifPresent(chipInstance -> {
                float killChanceValue = chipInstance.getChip().killChance.getValue(chipInstance.getLvl());
                if (event.getEntity().getRandom().nextFloat() < killChanceValue) {
                    target.hurt(target.level().damageSources().playerAttack(event.getEntity()), Float.MAX_VALUE);
                }
            });
        }
    }

    @SubscribeEvent
    public static void ProjectileHitEvent(ProjectileHitEvent.HitEntity event) {
        if (event.getOwner() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.MAKE_LIVING).ifPresent(chipInstance -> {
                MakeLiving chip = chipInstance.getChip();
                float value = chip.recyclingChance.getValue(chipInstance.getLvl());
                if (player.getRandom().nextFloat() < value) {
                    GunData data = GunData.from(player.getMainHandItem());
                    data.ammo.set(data.ammo.get() + 1);
                }
            });
        }
    }

    @SubscribeEvent
    public static void LivingFallEvent(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SILENT_WALKER).ifPresent(chipInstance -> {
                SilentWalker chip = chipInstance.getChip();

                // 获取坠落距离（以方块为单位，1方块=1米）
                float fallDistance = event.getDistance();

                // 计算伤害减免比例：与坠落距离成反比
                // 示例：10米减免50%，100米减免10%
                // 使用插值算法：在referenceDistance处为maxReduction，在referenceDistance*10处为minReduction
                float maxReduction = chip.maxDamageReduction.getValue(chipInstance.getLvl());
                float minReduction = chip.minDamageReduction.getValue(chipInstance.getLvl());
                float referenceDistance = chip.referenceDistance.getValue(chipInstance.getLvl());

                // 计算实际的伤害减免比例（反比关系）
                float reductionRatio;
                if (fallDistance <= 0) {
                    // 避免除以零
                    reductionRatio = maxReduction;
                } else if (fallDistance <= referenceDistance) {
                    // 坠落距离小于等于参考距离时，使用最大减免
                    reductionRatio = maxReduction;
                } else {
                    // 坠落距离大于参考距离时，使用对数衰减
                    // 当 fallDistance = referenceDistance 时，ratio = maxReduction
                    // 当 fallDistance = referenceDistance * 10 时，ratio ≈ minReduction
                    float logFactor = (float) (Math.log10(fallDistance / referenceDistance));
                    reductionRatio = maxReduction - (maxReduction - minReduction) * logFactor;

                    // 限制在最小和最大减免之间
                    reductionRatio = Math.max(minReduction, Math.min(maxReduction, reductionRatio));
                }

                // 应用伤害减免：使用伤害倍数
                // reductionRatio为减免比例，所以damageMultiplier = 1 - reductionRatio
                event.setDamageMultiplier(event.getDamageMultiplier() * 1 - reductionRatio);
            });


            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SPIDER_CLIMB).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                if (playerRunTimeData.spiderClimbing || player.horizontalCollision) {
                    SpiderClimb chip = chipInstance.getChip();
                    float reduction = chip.fallDamageReduction.getValue(chipInstance.getLvl());
                    event.setDamageMultiplier(event.getDamageMultiplier() * (1 - reduction));
                }
            });
        }
    }

    @SubscribeEvent
    public static void LivingIncomingDamageEvent(LivingIncomingDamageEvent event) {

        if (event.getSource().getEntity() instanceof Player player) {
            if (event.getSource().getDirectEntity() instanceof ProjectileEntity) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PRECISION_SHOOTING).ifPresent(chipInstance -> {
                    float value = chipInstance.getChip().damageMul.getValue(chipInstance.getLvl());
                    event.setAmount(event.getAmount() * (1 + value));
                });
            }
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.MELEE_ATTACK_MASTER).ifPresent(chipInstance -> {
                float value = chipInstance.getChip().damageMul.getValue(chipInstance.getLvl());
                event.setAmount(event.getAmount() * (1 + value));
            });
        }
        if (event.getEntity() instanceof Player player) {
            PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.SLIDING_TACKLE).ifPresent(chipInstance -> {
                if (playerRunTimeData.slidingTackleActive) {
                    event.setAmount(event.getAmount() * (1 - chipInstance.getChip().damageReduction.getValue(chipInstance.getLvl())));
                }
            });

            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.TACTICAL_ROLL).ifPresent(chipInstance -> {
                if (playerRunTimeData.tacticalRollInvincible) {
                    event.setCanceled(true);
                }
            });
        }
    }

    @SubscribeEvent
    public static void LivingDamageEvent$Post(LivingDamageEvent.Post event) {
        if (event.getEntity() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.ADRENAL_GLAND_BURST).ifPresent(chipInstance -> {
                int lvl = chipInstance.getLvl();
                AdrenalGlandBurst chip = chipInstance.getChip();
                if (player.getHealth() <= player.getMaxHealth() * chip.threshold.getValue(lvl)) {
                    if (!GeneChipAPI.isChipCooldown(player, chip)) {

                        float timeValue = chip.effectTime.getValue(lvl);
                        player.addEffect(new MobEffectInstance(
                                MobEffects.MOVEMENT_SPEED,
                                (int) (20 * timeValue),
                                1,
                                false,
                                false
                        ));
                        player.addEffect(new MobEffectInstance(
                                MobEffects.DAMAGE_BOOST,
                                (int) (20 * timeValue),
                                1,
                                false,
                                false
                        ));
                        GeneChipAPI.addChipCooldown(player, chipInstance.getChip(), (int) (chip.cooldown.getValue(lvl) * 60));
                    }
                }
            });

        }
    }

    @SubscribeEvent
    public static void LivingDeathEvent(LivingDeathEvent event) {
        if (event.getSource().getEntity() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.COMBO_FEVER).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                playerRunTimeData.recordKill(player.level().getGameTime());
                ComboFever chip = chipInstance.getChip();
                if (playerRunTimeData.isComboFever((int) (chip.maxTime.getValue(chipInstance.getLvl()) * 20))) {
                    int time = (int) (chip.effectTime.getValue(chipInstance.getLvl()) * 20);
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, time));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, time));
                }
            });

            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.BLOODTHIRSTY).ifPresent(chipInstance -> {
                Bloodthirsty chip = chipInstance.getChip();
                float chanceValue = chip.healChance.getValue(chipInstance.getLvl());
                if (player.getRandom().nextFloat() < chanceValue) {
                    player.heal(chip.healAmount.getValue(chipInstance.getLvl()));
                }
            });

            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.QUICK_ADJUSTMENT).ifPresent(chipInstance -> {
                QuickAdjustment chip = chipInstance.getChip();
                float timeValue = chip.effectTime.getValue(chipInstance.getLvl());
                Entity directEntity = event.getSource().getDirectEntity();
                if (directEntity instanceof Arrow || directEntity instanceof ProjectileEntity) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            (int) (20 * timeValue),
                            0,
                            false,
                            false
                    ));
                }
            });


            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.DESIRE_FOR_SLAUGHTER).ifPresent(chipInstance -> {
                DesireForSlaughter chip = chipInstance.getChip();
                float timeValue = chip.effectTime.getValue(chipInstance.getLvl());
                if (event.getSource().isDirect()) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.DAMAGE_BOOST,
                            (int) (20 * timeValue),
                            0,
                            false,
                            false
                    ));
                }
            });

        }
    }

    @SubscribeEvent
    public static void LivingKnockBackEvent(LivingKnockBackEvent event) {
        if (event.getEntity() instanceof Player player) {
            PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
            if (playerRunTimeData.tacticalRollInvincible) {
                event.setCanceled(true);
            }
        }
    }
}
