package com.chen1335.geneChip.attachmentData;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.chips.tactics.FlyingKick;
import com.chen1335.geneChip.chip.chips.tactics.TacticalRoll;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;

//运行时数据，均不保存
public class PlayerRunTimeData {
    // 连击热潮芯片 - 记录击杀时间戳（用于判断是否触发连击热潮）
    private final ArrayList<Long> comboFeversTime = new ArrayList<>();
    public int comboWindowTicks = 0;
    public int comboWindowDuration = 0;
    public int comboFeverTicks = 0;

    public int fallingAnimationTick = 0;

    public int cardHudSyncTicker = 0;

    // 皮糙肉厚芯片 - 是否处于蹲下激活护甲加成状态
    public boolean thickSkinnedActive = false;

    // 光合作用芯片 - 当前光照充能层数（最高5层）
    public int photosynthesisStacks = 0;

    // 光合作用芯片 - 层数变化计时器（每30秒检测一次）
    public int photosynthesisTimer = 0;
    public int photosynthesisInterval = 30 * 20;
    public int photosynthesisMaxStacks = 5;
    public boolean photosynthesisCharging = false;

    // 免疫值变化检测 - 记录上一tick的免疫值（用于触发免疫值变化事件）
    public int oldImmunity = -1;

    // 愈发狂热芯片 - 上一次服务端档位（用于避免每 tick 重复反馈）
    public int lastGrowingFervorStage = -1;

    // 战术滑铲芯片 - 是否正在滑铲
    public boolean slidingTackleActive = false;

    // 飞身踢芯片 - 服务端权威的飞踢状态
    public boolean flyingKickActive = false;
    public int flyingKickTimer = 0;
    public int flyingKickHitEntityId = -1;
    public LivingEntity flyingKickKnockbackTarget;
    public int flyingKickImpactTimer = 0;
    public boolean flyingKickImpactHandled = false;

    // 战术滑铲芯片 - 滑铲持续时间计时器
    public int slidingTackleTimer = 0;

    // 二段跳芯片 - 是否可以使用二段跳（落地后重置）
    public boolean canDoubleJump = true;

    // 二段跳芯片 - 上一tick是否在地面（用于检测离地）
    public boolean isOnGround = true;

    // 蛛行芯片 - 是否正在攀爬垂直表面
    public boolean spiderClimbing = false;

    // 战术翻滚芯片 - 是否正在翻滚
    public boolean tacticalRolling = false;

    // 战术翻滚芯片 - 无敌状态计时器
    public int tacticalRollTimer = 0;

    // 战术翻滚芯片 - 是否处于无敌状态
    public boolean tacticalRollInvincible = false;

    // 战术翻滚芯片 - 失衡状态计时器（无敌结束后进入失衡，移速降低）
    public int tacticalRollOffBalanceTimer = 0;

    // 反击风暴芯片 - 累积受到的伤害（用于下次攻击附加）
    public float counterStormAccumulatedDamage = 0;

    // 反击风暴芯片 - 反击窗口计时器（受伤害后开始计时，超时清除累积伤害）
    public int counterStormTimer = 0;

    // 痛觉封锁芯片 - 是否激活（用于伤害减免和近战加成判断）
    public boolean painBlockadeActive = false;

    // 感染者芯片 - 是否处于感染区（用于效果延长判断）
    public boolean infectedInZone = false;

    public void recordKill(long time, int windowTicks) {
        comboFeversTime.removeIf(killTime -> time - killTime >= windowTicks);
        if (comboFeversTime.size() >= 3) {
            comboFeversTime.removeFirst();
        }
        comboFeversTime.add(time);
        comboWindowDuration = windowTicks;
        comboWindowTicks = windowTicks;
    }

    public boolean isComboFever(int timeRequire) {
        if (comboFeversTime.size() < 3) {
            return false;
        }
        return (comboFeversTime.getLast() - comboFeversTime.getFirst()) < timeRequire;
    }

    public int getComboCount() {
        return comboFeversTime.size();
    }

    public void triggerComboFever(int ticks) {
        comboFeverTicks = ticks;
        comboWindowTicks = 0;
        comboFeversTime.clear();
    }

    public void tick(Player entity) {
        if (comboWindowTicks > 0 && --comboWindowTicks <= 0) {
            comboFeversTime.clear();
        }
        if (comboFeverTicks > 0) {
            comboFeverTicks--;
        }

        if (!entity.level().isClientSide) {
            int immunityValue = GeneChipAPI.getImmunityValue(entity);
            if (immunityValue != oldImmunity) {
                GeneChipAPI.onImmunityValueChanged(entity);
                oldImmunity = immunityValue;
            }
        }

        if (slidingTackleActive) {
            slidingTackleTimer--;
            if (slidingTackleTimer <= 0) {
                slidingTackleActive = false;
            }
        }

        if (!entity.level().isClientSide) {
            tickFlyingKick(entity);
        }

        // 战术翻滚计时
        if (tacticalRolling) {
            tacticalRollTimer--;
            if (tacticalRollTimer <= 0 && tacticalRollInvincible) {
                tacticalRollInvincible = false;
                // 无敌结束，添加失衡效果
                GeneChipAPI.getPlayerEquippedChip(entity, ChipTypes.TACTICAL_ROLL).ifPresent(chipInstance -> {
                    TacticalRoll chip = chipInstance.getChip();
                    float slowAmount = chip.offBalanceSlow.getValue(chipInstance.getLvl());
                    AttributeInstance speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speedAttr != null) {
                        speedAttr.addTransientModifier(new AttributeModifier(
                                GeneChip.id("tactical_roll_off_balance"),
                                -slowAmount,
                                AttributeModifier.Operation.ADD_MULTIPLIED_BASE
                        ));
                    }
                    tacticalRollOffBalanceTimer = (int) (chip.offBalanceTime.getValue(chipInstance.getLvl()) * 20);
                });
            }

            if (tacticalRollOffBalanceTimer > 0) {
                tacticalRollOffBalanceTimer--;
                if (tacticalRollOffBalanceTimer <= 0) {
                    tacticalRolling = false;
                    // 失衡结束，移除效果
                    AttributeInstance speedAttr = entity.getAttribute(Attributes.MOVEMENT_SPEED);
                    if (speedAttr != null) {
                        speedAttr.removeModifier(GeneChip.id("tactical_roll_off_balance"));
                    }
                }
            }
        }

        // 反击风暴计时
        if (counterStormTimer > 0) {
            counterStormTimer--;
            if (counterStormTimer <= 0) {
                counterStormAccumulatedDamage = 0;
            }
        }

        // 仅在真正落地的瞬间重置二段跳，避免动作包到达时服务端仍判定在地面而提前重置
        boolean wasOnGround = isOnGround;
        isOnGround = entity.onGround();
        if (isOnGround && !wasOnGround) {
            canDoubleJump = true;
        }
    }

    public void startFlyingKick(Player player) {
        flyingKickActive = true;
        flyingKickTimer = 12;
        flyingKickHitEntityId = -1;
        flyingKickKnockbackTarget = null;
        flyingKickImpactTimer = 0;
        flyingKickImpactHandled = false;
        player.fallDistance = 0;

        Vec3 direction = player.getLookAngle();
        Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
        if (horizontal.lengthSqr() < 1.0E-6) {
            horizontal = new Vec3(0, 0, 1);
        }
        horizontal = horizontal.normalize();
        player.setDeltaMovement(horizontal.scale(1.25).add(0, Math.max(0, player.getDeltaMovement().y) * 0.25, 0));
        player.hurtMarked = true;
    }

    private void tickFlyingKick(Player player) {
        if (flyingKickActive) {
            player.fallDistance = 0;
            if (!player.isAlive() || player.isPassenger() || player.isInWater() || player.isInLava()) {
                stopFlyingKick(player);
            } else {
                Vec3 direction = player.getLookAngle();
                Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
                if (horizontal.lengthSqr() >= 1.0E-6) {
                    Vec3 velocity = horizontal.normalize().scale(1.25);
                    player.setDeltaMovement(velocity.x, Math.max(-0.08, player.getDeltaMovement().y), velocity.z);
                    player.hurtMarked = true;
                }

                if (flyingKickHitEntityId < 0) {
                    AABB hitBox = player.getBoundingBox().expandTowards(player.getDeltaMovement()).inflate(0.45);
                    LivingEntity target = player.level().getEntitiesOfClass(
                                    LivingEntity.class,
                                    hitBox,
                                    candidate -> candidate != player && candidate.isAlive() && !candidate.isSpectator())
                            .stream()
                            .min((left, right) -> Double.compare(player.distanceToSqr(left), player.distanceToSqr(right)))
                            .orElse(null);
                    if (target != null) {
                        hitFlyingKickTarget(player, target);
                    }
                }

                flyingKickTimer--;
                if (flyingKickTimer <= 0 || player.horizontalCollision || player.onGround()) {
                    stopFlyingKick(player);
                }
            }
        }

        tickFlyingKickImpact(player);
    }

    private void hitFlyingKickTarget(Player player, LivingEntity target) {
        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.FLYING_KICK).ifPresent(chipInstance -> {
            FlyingKick chip = chipInstance.getChip();
            float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
            float damage = attackDamage * chip.damageMultiplier.getValue(chipInstance.getLvl());
            if (!target.hurt(target.level().damageSources().playerAttack(player), damage)) {
                return;
            }

            flyingKickHitEntityId = target.getId();
            flyingKickKnockbackTarget = target;
            flyingKickImpactTimer = 20;
            flyingKickImpactHandled = false;

            Vec3 direction = target.position().subtract(player.position());
            Vec3 horizontal = new Vec3(direction.x, 0, direction.z);
            if (horizontal.lengthSqr() < 1.0E-6) {
                Vec3 look = player.getLookAngle();
                horizontal = new Vec3(look.x, 0, look.z);
            }
            horizontal = horizontal.normalize();
            double distance = chip.knockbackDistance.getValue(chipInstance.getLvl());
            double speed = Math.min(2.0, Math.max(0.4, distance / 6.0));
            Vec3 add = horizontal.scale(speed).add(0, 0.35, 0);
            player.addDeltaMovement(add.multiply(-1,0,-1));
            target.setDeltaMovement(add);
            target.hurtMarked = true;
            stopFlyingKick(player);
        });
    }

    private void tickFlyingKickImpact(Player player) {
        if (flyingKickKnockbackTarget == null) {
            return;
        }
        if (flyingKickImpactHandled || flyingKickImpactTimer-- <= 0 || !flyingKickKnockbackTarget.isAlive()) {
            clearFlyingKickTarget();
            return;
        }

        LivingEntity target = flyingKickKnockbackTarget;
        if (target.horizontalCollision) {
            GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.FLYING_KICK).ifPresent(chipInstance -> {
                FlyingKick chip = chipInstance.getChip();
                float attackDamage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE);
                float impactDamage = attackDamage * chip.impactDamageMultiplier.getValue(chipInstance.getLvl());
                target.invulnerableTime = 0;
                target.hurt(target.level().damageSources().playerAttack(player), impactDamage);
                int stunTicks = (int) (chip.stunDuration.getValue(chipInstance.getLvl()) * 20);
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.MOVEMENT_SLOWDOWN,
                        stunTicks,
                        9,
                        false,
                        false,
                        true
                ));
                target.addEffect(new net.minecraft.world.effect.MobEffectInstance(
                        net.minecraft.world.effect.MobEffects.WEAKNESS,
                        stunTicks,
                        9,
                        false,
                        false,
                        true
                ));
                target.setDeltaMovement(Vec3.ZERO);
                target.hurtMarked = true;
            });
            flyingKickImpactHandled = true;
            clearFlyingKickTarget();
        }
    }

    private void stopFlyingKick(Player player) {
        flyingKickActive = false;
        flyingKickTimer = 0;
        player.fallDistance = 0;
    }

    private void clearFlyingKickTarget() {
        flyingKickKnockbackTarget = null;
        flyingKickImpactTimer = 0;
        flyingKickImpactHandled = false;
    }

}
