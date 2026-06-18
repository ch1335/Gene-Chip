package com.chen1335.geneChip.attachmentData;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.chips.tactics.TacticalRoll;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;

//运行时数据，均不保存
public class PlayerRunTimeData {
    // 连击热潮芯片 - 记录击杀时间戳（用于判断是否触发连击热潮）
    private final ArrayList<Long> comboFeversTime = new ArrayList<>();

    // 皮糙肉厚芯片 - 是否处于蹲下激活护甲加成状态
    public boolean thickSkinnedActive = false;

    // 光合作用芯片 - 当前光照充能层数（最高5层）
    public int photosynthesisStacks = 0;

    // 光合作用芯片 - 层数变化计时器（每30秒检测一次）
    public int photosynthesisTimer = 0;

    // 免疫值变化检测 - 记录上一tick的免疫值（用于触发免疫值变化事件）
    public int oldImmunity = -1;

    // 战术滑铲芯片 - 是否正在滑铲
    public boolean slidingTackleActive = false;

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

    public void recordKill(long time) {
        if (comboFeversTime.size() >= 3) {
            comboFeversTime.removeFirst();
        }
        comboFeversTime.add(time);
    }

    public boolean isComboFever(int timeRequire) {
        if (comboFeversTime.size() < 3) {
            return false;
        }
        return (comboFeversTime.getLast() - comboFeversTime.getFirst()) < timeRequire;
    }

    public void tick(Player entity) {
        int immunityValue = GeneChipAPI.getImmunityValue(entity);
        if (immunityValue != oldImmunity) {
            GeneChipAPI.onImmunityValueChanged(entity);
            oldImmunity = immunityValue;
        }

        if (slidingTackleActive) {
            slidingTackleTimer--;
            if (slidingTackleTimer<=0) {
                slidingTackleActive = false;
            }
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

        // 更新地面状态
        isOnGround = entity.onGround();
        if (isOnGround) {
            canDoubleJump = true;
        }
    }

}
