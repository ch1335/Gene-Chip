package com.chen1335.geneChip.common;

import com.atsuishio.superbwarfare.api.event.ProjectileHitEvent;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.entity.projectile.ProjectileEntity;
import com.hexagram2021.fiahi.common.event.ApplySpecialEatEffectEvent;
import com.wu_meng.winterscavenge.event.HealingItemUsedEvent;
import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.tags.EntityTypeTags;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
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
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.MAKE_LIVING).ifPresent(chipInstance -> {
                MakeLiving chip = chipInstance.getChip();
                GunData data = GunData.from(player.getMainHandItem());
                float value = chip.recyclingChance.getValue(chipInstance.getLvl());
                int projectileAmount = Math.max(1, data.compute().projectileAmount);
                if (projectileAmount > 1) {
                    value /= projectileAmount;
                }
                int recycled = 0;
                if (player.getRandom().nextFloat() < value) recycled++;
                if (WorldFactorSynergy.isZombieRiot() && player.getRandom().nextFloat() < 0.05F) recycled++;
                if (recycled > 0) {
                    data.ammo.set(data.ammo.get() + recycled);
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
            }
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.MELEE_ATTACK_MASTER).ifPresent(chipInstance -> {
                float value = chipInstance.getChip().damageMul.getValue(chipInstance.getLvl());
                event.setAmount(event.getAmount() * (1 + value));
            });

            // 痛觉封锁 - 近战伤害加成（无防具时生效）
            PlayerRunTimeData attackerData = GeneChipAPI.getPlayerRunTimeData(player);
            if (attackerData.painBlockadeActive && hasNoArmor(player) && !(event.getSource().getDirectEntity() instanceof ProjectileEntity)) {
                GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.PAIN_BLOCKADE).ifPresent(chipInstance -> {
                    float bonus = chipInstance.getChip().meleeDamageBonus.getValue(chipInstance.getLvl());
                    event.setAmount(event.getAmount() * (1 + bonus));
                });
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
                if (player instanceof ServerPlayer serverPlayer) CardHudSyncService.sync(serverPlayer);
            });

        }
    }

    @SubscribeEvent
    public static void LivingDeathEvent(LivingDeathEvent event) {
        if (event.getEntity().level().isClientSide) return;
        if (event.getSource().getEntity() instanceof Player player) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.COMBO_FEVER).ifPresent(chipInstance -> {
                PlayerRunTimeData playerRunTimeData = GeneChipAPI.getPlayerRunTimeData(player);
                ComboFever chip = chipInstance.getChip();
                int window = (int) (chip.maxTime.getValue(chipInstance.getLvl()) * 20);
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
                for (LivingEntity entity : player.level().getEntitiesOfClass(LivingEntity.class,
                        player.getBoundingBox().inflate(radius))) {
                    if (entity != player && entity.isAlive()) {
                        entity.setRemainingFireTicks(burnDuration);
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
                    }
                });
            }
        }
    }

    @SubscribeEvent
    public static void ApplySpecialEatEffectEvent(ApplySpecialEatEffectEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide) return;
        if (event.getFoodData().getRottenLevel() <= 0) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.ROTTEN_FLESH_TOLERANCE).ifPresent(chipInstance -> {
            event.setCancelled(true);
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
                .ifPresent(chipInstance -> player.heal(totalHealing));
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
