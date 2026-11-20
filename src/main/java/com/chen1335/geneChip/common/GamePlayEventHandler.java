package com.chen1335.geneChip.common;

import com.atsuishio.superbwarfare.api.event.ProjectileHitEvent;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.data.gun.GunProp;
import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent;
import com.immunity.ImmunityTags;
import com.immunity.data.InfectionZoneManager;
import com.wu_meng.winterscavenge.event.HealingItemUsedEvent;
import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.tags.EntityTypeTags;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.ChipExperience;
import com.chen1335.geneChip.chip.chips.combat.*;
import com.chen1335.geneChip.chip.chips.mutation.AdrenalGlandBurst;
import com.chen1335.geneChip.chip.chips.tactics.SilentWalker;
import com.chen1335.geneChip.chip.chips.tactics.SpiderClimb;
import com.chen1335.geneChip.chip.chips.special.VengefulFlame;
import com.chen1335.geneChip.compat.worldfactor.WorldFactorSynergy;
import com.chen1335.geneChip.chip.chips.special.IronHeart;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingKnockBackEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import com.chen1335.geneChip.network.HeadShotIconPacket;
import com.chen1335.geneChip.network.CardFeedbackPacket;
import net.minecraft.server.level.ServerPlayer;

import java.util.*;

@EventBusSubscriber(modid = GeneChip.MODID)
public class GamePlayEventHandler {

    // 猎手本能芯片 - 被标记的实体（实体UUID -> {伤害提升值, 过期tick}）
    private static final Map<UUID, float[]> HUNTER_MARKED = new HashMap<>();

    public static void markEntity(UUID entityId, float damageBoost, long expireTick) {
        HUNTER_MARKED.put(entityId, new float[]{damageBoost, (float) expireTick});
    }

    @SubscribeEvent
    public static void CriticalHitEvent(CriticalHitEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getTarget() instanceof LivingEntity target && event.getTarget().getType().is(EntityTypeTags.HEAD_SHOT_HUNTER_TARGET)) {
            GeneChipAPI.getPlayerEquippedChip(event.getEntity(), ChipTypes.HEAD_SHOT_HUNTER).ifPresent(chipInstance -> {
                float killChanceValue = chipInstance.getChip().killChance.getValue(chipInstance.getLvl());
                // 爆头猎手经验：对目标生物暴击 +5（同目标 1 秒去重）
                if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                    ChipExperience.grant(serverPlayer, ChipTypes.HEAD_SHOT_HUNTER.get(), 5, "crit:" + target.getUUID(), 20);
                }
                if (event.getEntity().getRandom().nextFloat() < killChanceValue) {
                    target.hurt(target.level().damageSources().playerAttack(event.getEntity()), Float.MAX_VALUE);
                    // 爆头触发：在怪物头顶生成一个爆头 icon 飘浮特效（发给追踪该实体的客户端）
                    if (!target.level().isClientSide) {
                        double iconY = target.getY() + target.getBbHeight() + 0.3;
                        PacketDistributor.sendToPlayersTrackingEntity(target,
                                new HeadShotIconPacket(target.getX(), iconY, target.getZ()));
                        if (event.getEntity() instanceof ServerPlayer serverPlayer) {
                            CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.HEADSHOT, 1);
                        }
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void ProjectileHitEvent(ProjectileHitEvent.HitEntity event) {
        if (event.getOwner() instanceof ServerPlayer player) {
            // 精准射击经验：爆头命中 +5（同目标 10 tick 去重）
            if (event.isHeadshot()) {
                ChipExperience.grant(player, ChipTypes.PRECISION_SHOOTING.get(), 5,
                        "headshot:" + event.getTarget().getUUID(), 10);
            }
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.MAKE_LIVING).ifPresent(chipInstance -> {
                MakeLiving chip = chipInstance.getChip();
                GunData data = GunData.from(player.getMainHandItem());
                float value = chip.recyclingChance.getValue(chipInstance.getLvl());
                // SW 0.8.9 起 GunData#compute() 已弃用（@ApiStatus.ScheduledForRemoval），改用 GunProp 取值
                int projectileAmount = Math.max(1, data.get(GunProp.PROJECTILE_AMOUNT));
                if (projectileAmount > 1) {
                    value /= projectileAmount;
                }
                int recycled = 0;
                if (player.getRandom().nextFloat() < value) recycled++;
                if (WorldFactorSynergy.isZombieRiot() && player.getRandom().nextFloat() < 0.05F) recycled++;
                if (recycled > 0) {
                    data.ammo.set(data.ammo.get() + recycled);
                    // 维持生计经验：成功回收弹药 +3
                    ChipExperience.grant(player, ChipTypes.MAKE_LIVING.get(), 3);
                    CardHudSyncService.feedback(player, CardFeedbackPacket.FeedbackType.AMMO_RECYCLED, recycled);
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
                // 静步者经验：坠落减免实际生效，每 3 格坠落 +2（单次上限 12）
                if (player instanceof ServerPlayer serverPlayer) {
                    int exp = Math.min(12, (int) (event.getDistance() / 3.0F) * 2);
                    if (exp > 0) {
                        ChipExperience.grant(serverPlayer, ChipTypes.SILENT_WALKER.get(), exp);
                    }
                }
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
        if (event.getEntity().level().isClientSide) return;

        // 猎手本能 - 被标记实体受到额外伤害
        UUID entityUuid = event.getEntity().getUUID();
        float[] markData = HUNTER_MARKED.get(entityUuid);
        if (markData != null) {
            if (event.getEntity().level().getGameTime() > (long) markData[1]) {
                HUNTER_MARKED.remove(entityUuid);
            } else {
                event.setAmount(event.getAmount() * (1 + markData[0]));
            }
        }

        if (event.getSource().getEntity() instanceof Player player) {
            if (event.getSource().getDirectEntity() instanceof ProjectileEntity) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PRECISION_SHOOTING).ifPresent(chipInstance -> {
                    float value = chipInstance.getChip().damageMul.getValue(chipInstance.getLvl());
                    // 阳光裂隙联动：枪械伤害+10%
                    if (WorldFactorSynergy.isRaysOfSunlight()) {
                        value += 0.1F;
                    }
                    event.setAmount(event.getAmount() * (1 + value));
                });
                // 快速调整经验（兜底）：远程命中 +1（同目标 10 tick 去重）
                if (player instanceof ServerPlayer serverPlayer && event.getEntity() instanceof LivingEntity hitTarget) {
                    ChipExperience.grant(serverPlayer, ChipTypes.QUICK_ADJUSTMENT.get(), 1,
                            "rangedhit:" + hitTarget.getUUID(), 10);
                }
            }
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.MELEE_ATTACK_MASTER).ifPresent(chipInstance -> {
                float value = chipInstance.getChip().damageMul.getValue(chipInstance.getLvl());
                event.setAmount(event.getAmount() * (1 + value));
            });
            // 近战专家经验：近战命中 +2（同目标 10 tick 去重）
            if (!(event.getSource().getDirectEntity() instanceof ProjectileEntity)
                    && event.getEntity() instanceof LivingEntity hitTarget
                    && player instanceof ServerPlayer serverPlayer) {
                ChipExperience.grant(serverPlayer, ChipTypes.MELEE_ATTACK_MASTER.get(), 2,
                        "melee:" + hitTarget.getUUID(), 10);
            }

            // 痛觉封锁 - 近战伤害加成（无防具时生效）
            PlayerRunTimeData attackerData = GeneChipAPI.getPlayerRunTimeData(player);
            if (attackerData.painBlockadeActive && hasNoArmor(player) && !(event.getSource().getDirectEntity() instanceof ProjectileEntity)) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PAIN_BLOCKADE).ifPresent(chipInstance -> {
                    float bonus = chipInstance.getChip().meleeDamageBonus.getValue(chipInstance.getLvl());
                    event.setAmount(event.getAmount() * (1 + bonus));
                });
                // 痛觉封锁经验：无甲近战命中，每 2 点伤害 +1（单次上限 8）
                if (player instanceof ServerPlayer serverPlayer) {
                    int expAmount = Math.min(8, Math.max(1, Math.round(event.getAmount() / 2.0F)));
                    ChipExperience.grant(serverPlayer, ChipTypes.PAIN_BLOCKADE.get(), expAmount);
                }
            }

            // 反击风暴 - 附加累积受到的伤害
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.COUNTER_STORM).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                if (playerRunTimeData.counterStormTimer > 0 && playerRunTimeData.counterStormAccumulatedDamage > 0) {
                    float ratio = chipInstance.getChip().damageReflectRatio.getValue(chipInstance.getLvl());
                    float bonus = playerRunTimeData.counterStormAccumulatedDamage * ratio;
                    event.setAmount(event.getAmount() + bonus);
                    playerRunTimeData.counterStormAccumulatedDamage = 0;
                    playerRunTimeData.counterStormTimer = 0;
                    if (player instanceof ServerPlayer serverPlayer) {
                        CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.COUNTER_RELEASED, Math.round(bonus));
                        CardHudSyncService.sync(serverPlayer);
                        // 反击风暴经验：反击命中 +12
                        ChipExperience.grant(serverPlayer, ChipTypes.COUNTER_STORM.get(), 12);
                    }
                }
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
                    // 战术翻滚经验：翻滚无敌期间躲过伤害 +12
                    if (player instanceof ServerPlayer serverPlayer) {
                        ChipExperience.grant(serverPlayer, ChipTypes.TACTICAL_ROLL.get(), 12, "roll-dodge", 10);
                    }
                }
            });

            // 痛觉封锁 - 减伤（无防具时生效）
            if (playerRunTimeData.painBlockadeActive && hasNoArmor(player)) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PAIN_BLOCKADE).ifPresent(chipInstance -> {
                    float reduction = chipInstance.getChip().damageReduction.getValue(chipInstance.getLvl());
                    event.setAmount(event.getAmount() * (1 - reduction));
                });
            }
        }
    }

    @SubscribeEvent
    public static void LivingDamageEvent$Post(LivingDamageEvent.Post event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getEntity() instanceof Player player) {
            PlayerRunTimeData hurtRuntime = GeneChipAPI.getPlayerRunTimeData(player);
            if (player instanceof ServerPlayer serverPlayer) {
                // 肾上腺爆发经验：爆发期间受伤 +3
                if (hurtRuntime.adrenalBurstTicks > 0) {
                    ChipExperience.grant(serverPlayer, ChipTypes.ADRENAL_GLAND_BURST.get(), 3, "burst-hurt", 20);
                }
                // 皮糙肉厚经验：受伤每 2 点 +1（单次上限 6），蹲下受伤额外 +2
                int thickSkinnedExp = Math.min(6, Math.max(1, Math.round(event.getNewDamage() / 2.0F)));
                if (player.isShiftKeyDown()) {
                    thickSkinnedExp += 2;
                }
                ChipExperience.grant(serverPlayer, ChipTypes.THICK_SKINNED.get(), thickSkinnedExp);
                // 复仇火焰经验（兜底）：受到伤害每 4 点 +1
                ChipExperience.grant(serverPlayer, ChipTypes.VENGEFUL_FLAME.get(),
                        Math.max(1, Math.round(event.getNewDamage() / 4.0F)), "vengeance-hurt", 20);
            }
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
                        GeneChipAPI.addChipCooldown(player, chipInstance.getChip(), (int) (chip.cooldown.getValue(lvl) * 20));
                        // 记录爆发窗口，用于"爆发期间击杀/受伤"的经验来源
                        GeneChipAPI.getPlayerRunTimeData(player).adrenalBurstTicks = (int) (20 * timeValue);
                        if (player instanceof ServerPlayer serverPlayer) {
                            serverPlayer.serverLevel().sendParticles(ParticleTypes.FIREWORK,
                                    player.getX(), player.getY() + 1.0, player.getZ(), 12, 0.5, 0.6, 0.5, 0.045);
                            CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.ADRENAL_TRIGGERED, 0);
                            CardHudSyncService.sync(serverPlayer);
                        }
                    }
                }
            });

            // 反击风暴 - 记录受到的伤害
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.COUNTER_STORM).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                playerRunTimeData.counterStormAccumulatedDamage += event.getNewDamage();
                playerRunTimeData.counterStormTimer = (int) (chipInstance.getChip().reflectWindow.getValue(chipInstance.getLvl()) * 20);
                if (player instanceof ServerPlayer serverPlayer) {
                    // 反击风暴经验：触发反击窗口 +5
                    ChipExperience.grant(serverPlayer, ChipTypes.COUNTER_STORM.get(), 5, "counter-window", 10);
                    CardHudSyncService.sync(serverPlayer);
                }
            });

        }
    }

    @SubscribeEvent
    public static void LivingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        // 猎手本能经验：被标记目标死亡 +10
        if (HUNTER_MARKED.remove(event.getEntity().getUUID()) != null
                && event.getSource().getEntity() instanceof ServerPlayer hunter) {
            ChipExperience.grant(hunter, ChipTypes.HUNTER_INSTINCT.get(), 10);
        }
        // 野外猎手经验：击杀动物 +5
        if (event.getSource().getEntity() instanceof ServerPlayer animalHunter
                && event.getEntity().getType().is(EntityTypeTags.ANIMALS)) {
            ChipExperience.grant(animalHunter, ChipTypes.WILD_HUNTER.get(), 5);
        }
        if (event.getSource().getEntity() instanceof Player player) {
            PlayerRunTimeData killRuntime = GeneChipAPI.getPlayerRunTimeData(player);
            if (player instanceof ServerPlayer serverPlayer) {
                // 肾上腺爆发经验：爆发期间击杀 +10
                if (killRuntime.adrenalBurstTicks > 0) {
                    ChipExperience.grant(serverPlayer, ChipTypes.ADRENAL_GLAND_BURST.get(), 10);
                }
                // 近战击杀经验：近战专家 +6 / 杀戮渴望 +7
                if (event.getSource().isDirect()) {
                    ChipExperience.grant(serverPlayer, ChipTypes.MELEE_ATTACK_MASTER.get(), 6);
                    ChipExperience.grant(serverPlayer, ChipTypes.DESIRE_FOR_SLAUGHTER.get(), 7);
                }
                // 突变适应经验：感染区内击杀 +3
                InfectionZoneManager zoneManager = InfectionZoneManager.get(serverPlayer.getServer());
                if (zoneManager != null && zoneManager.getDrain(serverPlayer.serverLevel(), serverPlayer.blockPosition()) > 0) {
                    ChipExperience.grant(serverPlayer, ChipTypes.MUTATION_ADAPTATION.get(), 3);
                }
                // 暗夜猎手经验：夜间击杀 +4
                if (serverPlayer.level().isNight()) {
                    ChipExperience.grant(serverPlayer, ChipTypes.NIGHT_HUNTER.get(), 4);
                }
            }
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.COMBO_FEVER).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                ComboFever chip = chipInstance.getChip();
                int window = (int) (chip.maxTime.getValue(chipInstance.getLvl()) * 20);
                // 连击热潮经验：热度窗口内每个击杀 +10
                if (playerRunTimeData.comboFeverTicks > 0 && player instanceof ServerPlayer serverPlayer) {
                    ChipExperience.grant(serverPlayer, ChipTypes.COMBO_FEVER.get(), 10);
                }
                playerRunTimeData.recordKill(player.level().getGameTime(), window);
                if (playerRunTimeData.isComboFever(window)) {
                    int time = (int) (chip.effectTime.getValue(chipInstance.getLvl()) * 20);
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, time));
                    player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, time));
                    player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, time));
                    playerRunTimeData.triggerComboFever(time);
                    if (player instanceof ServerPlayer serverPlayer) {
                        CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.COMBO_TRIGGERED, 3);
                    }
                } else if (player instanceof ServerPlayer serverPlayer) {
                    CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.COMBO_PROGRESS, playerRunTimeData.getComboCount());
                }
                if (player instanceof ServerPlayer serverPlayer) CardHudSyncService.sync(serverPlayer);
            });

            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.BLOODTHIRSTY).ifPresent(chipInstance -> {
                Bloodthirsty chip = chipInstance.getChip();
                float chanceValue = chip.healChance.getValue(chipInstance.getLvl());
                if (player.getRandom().nextFloat() < chanceValue) {
                    player.heal(chip.healAmount.getValue(chipInstance.getLvl()));
                    // 嗜血经验：实际触发回血 +3
                    if (player instanceof ServerPlayer serverPlayer) {
                        ChipExperience.grant(serverPlayer, ChipTypes.BLOODTHIRSTY.get(), 3);
                    }
                }
            });

            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.QUICK_ADJUSTMENT).ifPresent(chipInstance -> {
                QuickAdjustment chip = chipInstance.getChip();
                float timeValue = chip.effectTime.getValue(chipInstance.getLvl());
                // 天气晴朗联动：远程击杀后速度效果+1秒
                if (WorldFactorSynergy.isFineWeather()) {
                    timeValue += 1;
                }
                Entity directEntity = event.getSource().getDirectEntity();
                if (directEntity instanceof Arrow || directEntity instanceof ProjectileEntity) {
                    player.addEffect(new MobEffectInstance(
                            MobEffects.MOVEMENT_SPEED,
                            (int) (20 * timeValue),
                            0,
                            false,
                            false
                    ));
                    // 快速调整经验：枪械/弓箭击杀 +7
                    if (player instanceof ServerPlayer serverPlayer) {
                        ChipExperience.grant(serverPlayer, ChipTypes.QUICK_ADJUSTMENT.get(), 7);
                    }
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

            // 痛觉封锁 - 击杀回复生命值（无防具时生效）
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PAIN_BLOCKADE).ifPresent(chipInstance -> {
                if (hasNoArmor(player)) {
                    float healAmount = chipInstance.getChip().healOnKill.getValue(chipInstance.getLvl());
                    player.heal(healAmount);
                    // 痛觉封锁经验：无甲近战击杀 +10
                    if (event.getSource().isDirect() && player instanceof ServerPlayer serverPlayer) {
                        ChipExperience.grant(serverPlayer, ChipTypes.PAIN_BLOCKADE.get(), 10);
                    }
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

    @SubscribeEvent
    public static void LivingDeathEvent$Player(LivingDeathEvent event) {
        if (event.getEntity() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.VENGEFUL_FLAME).ifPresent(chipInstance -> {

                VengefulFlame chip = chipInstance.getChip();
                float radius = chip.explosionRadius.getValue(chipInstance.getLvl());
                int burnDuration = (int) (chip.burnDuration.getValue(chipInstance.getLvl()) * 20);

                // 在死亡位置生成不破坏地形的爆炸
                player.level().explode(
                        player,
                        player.getX(), player.getY(), player.getZ(),
                        radius,
                        net.minecraft.world.level.Level.ExplosionInteraction.NONE
                );

                // 对周围实体施加燃烧
                int burned = 0;
                for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(radius))) {
                    if (entity != player && entity.isAlive()) {
                        entity.setRemainingFireTicks(burnDuration);
                        burned++;
                    }
                }
                // 复仇火焰经验：死亡触发 +25，每点燃一个敌人 +5
                if (player instanceof ServerPlayer serverPlayer) {
                    ChipExperience.grant(serverPlayer, ChipTypes.VENGEFUL_FLAME.get(), 25);
                    if (burned > 0) {
                        ChipExperience.grant(serverPlayer, ChipTypes.VENGEFUL_FLAME.get(), burned * 5);
                    }
                }

                // 生成粒子火焰效果
                if (!player.level().isClientSide()) {
                    net.minecraft.server.level.ServerLevel serverLevel = (net.minecraft.server.level.ServerLevel) player.level();
                    for (int i = 0; i < 80; i++) {
                        double offsetX = (player.getRandom().nextDouble() - 0.5) * radius * 2;
                        double offsetY = player.getRandom().nextDouble() * radius;
                        double offsetZ = (player.getRandom().nextDouble() - 0.5) * radius * 2;
                        serverLevel.sendParticles(ParticleTypes.FLAME,
                                player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                                1, 0, 0.1, 0, 0);
                    }
                    for (int i = 0; i < 40; i++) {
                        double offsetX = (player.getRandom().nextDouble() - 0.5) * radius * 2;
                        double offsetY = player.getRandom().nextDouble() * radius;
                        double offsetZ = (player.getRandom().nextDouble() - 0.5) * radius * 2;
                        serverLevel.sendParticles(ParticleTypes.LAVA,
                                player.getX() + offsetX, player.getY() + offsetY, player.getZ() + offsetZ,
                                1, 0, 0.05, 0, 0);
                    }
                }
            });
        }
    }

    @SubscribeEvent
    public static void MobEffectEvent$Added(MobEffectEvent.Added event) {
        if (event.getEntity() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.IRON_HEART).ifPresent(chipInstance -> {
                MobEffectInstance effectInstance = event.getEffectInstance();
                if (!effectInstance.getEffect().value().isBeneficial()) {
                    IronHeart chip = chipInstance.getChip();
                    float reduction = chip.durationReduction.getValue(chipInstance.getLvl());
                    effectInstance.duration = (int) (effectInstance.getDuration() * (1 - reduction));
                    // 钢铁之心经验：负面效果被实际缩短 +3
                    if (player instanceof ServerPlayer serverPlayer) {
                        ChipExperience.grant(serverPlayer, ChipTypes.IRON_HEART.get(), 3,
                                "debuff:" + effectInstance.getEffect(), 10);
                    }
                }
            });

            // 感染者 - 处于感染区时正面效果持续时间延长
            PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
            if (runtimeData.infectedInZone) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.INFECTED).ifPresent(chipInstance -> {
                    MobEffectInstance effectInstance = event.getEffectInstance();
                    if (effectInstance.getEffect().value().isBeneficial()) {
                        float extension = chipInstance.getChip().effectExtension.getValue(chipInstance.getLvl());
                        effectInstance.duration = (int) (effectInstance.getDuration() * (1 + extension));
                        // 感染者经验：正面效果被延长实际生效 +2
                        if (player instanceof ServerPlayer serverPlayer) {
                            ChipExperience.grant(serverPlayer, ChipTypes.INFECTED.get(), 2, "buff-extend", 10);
                        }
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void ApplySpecialEatEffectEvent(ApplySpecialEatEffectEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        ItemStack stack = event.getItemStack();
        // 触发条件是「物品本身属于腐坏食物」，而不是 fiahi 的腐烂等级：
        // 原版腐肉在 fiahi 里温度恒为 0、腐烂等级也恒为 0（>50 才算 1 级），只看等级的话对腐肉永远不触发。
        // 腐坏食物统一用 Immunity 的 immunity:spoiled_food 标签判定（整合包把 rotten_flesh 加进去了），
        // 同时保留「腐烂等级 > 0」这条，兼容在背包/地上被 fiahi 捂到腐烂的食物。
        if (!stack.is(ImmunityTags.SPOILED_FOOD) && event.getFoodData().getRottenLevel() <= 0) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.ROTTEN_FLESH_TOLERANCE).ifPresent(chipInstance -> {
            event.setCancelled(true);
            // 腐肉耐受经验：食用腐坏食物 +8
            if (player instanceof ServerPlayer serverPlayer) {
                ChipExperience.grant(serverPlayer, ChipTypes.ROTTEN_FLESH_TOLERANCE.get(), 8);
            }
            if (player.getRandom().nextFloat() < 0.1F) {
                player.heal(1.0F);
            }
        });
    }

    @SubscribeEvent
    public static void HealingItemUsed(HealingItemUsedEvent event) {
        Player player = event.getPlayer();
        if (player.level().isClientSide) return;

        float totalHealing = event.getTotalHealing();
        if (totalHealing <= 0.0F) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.TRAUMA_FIRST_AID)
                .ifPresent(chipInstance -> {
                    player.heal(totalHealing);
                    // 外伤急救经验：使用治疗类物品 +5
                    if (player instanceof ServerPlayer serverPlayer) {
                        ChipExperience.grant(serverPlayer, ChipTypes.TRAUMA_FIRST_AID.get(), 5, "heal-item", 5);
                    }
                });
    }

    @SubscribeEvent
    public static void RightClickItem(net.neoforged.neoforge.event.entity.player.PlayerInteractEvent.RightClickItem event) {
        Player player = event.getEntity();
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.INFECTED).ifPresent(chipInstance -> {
            net.minecraft.world.item.ItemStack stack = event.getItemStack();
            boolean blocked = stack.getItem() instanceof com.immunity.item.InhibitorItem;
            // 感染溢出联动：禁用治疗物品
            blocked |= WorldFactorSynergy.isInfectionOverflow()
                    && stack.is(net.minecraft.tags.ItemTags.create(net.minecraft.resources.ResourceLocation.parse("c:foods/golden")));
            if (blocked) {
                event.setCanceled(true);
                if (player instanceof ServerPlayer serverPlayer) {
                    CardHudSyncService.feedback(serverPlayer, CardFeedbackPacket.FeedbackType.INFECTED_ITEM_BLOCKED, 0);
                }
            }
        });
    }

    private static boolean hasNoArmor(Player player) {
        for (net.minecraft.world.item.ItemStack armor : player.getInventory().armor) {
            if (!armor.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
