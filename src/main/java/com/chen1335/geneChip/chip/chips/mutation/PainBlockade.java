package com.chen1335.geneChip.chip.chips.mutation;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.attachmentData.PlayerRunTimeData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.chip.chipConfig.JsValueCalculator;
import com.immunity.util.ImmunityServerUtil;
import net.minecraft.world.entity.player.Player;

public class PainBlockade extends Chip {
    public final JsValueCalculator damageReduction = new JsValueCalculator("0.3", true);
    public final JsValueCalculator meleeDamageBonus = new JsValueCalculator("0.5", true);
    public final JsValueCalculator healOnKill = new JsValueCalculator("2");
    public final JsValueCalculator immunityCost = new JsValueCalculator("0.05", true);

    public PainBlockade() {
        super(makeTexture("pain_blockade"));
        registerConfigValue("damage_reduction", damageReduction);
        registerConfigValue("melee_damage_bonus", meleeDamageBonus);
        registerConfigValue("heal_on_kill", healOnKill);
        registerConfigValue("immunity_cost", immunityCost);
    }

    @Override
    public ChipType getType() {
        return ChipType.MUTATION;
    }

    @Override
    public void onEquipped(Player player, ChipInstance<?> instance) {
        // 扣除5%当前免疫力上限
        int maxImmunity = ImmunityServerUtil.getMaxImmunity();
        int reduction = (int) (maxImmunity * immunityCost.getValue(instance.getLvl()));
        ImmunityServerUtil.addImmunity((net.minecraft.server.level.ServerPlayer) player, -reduction);

        // 标记激活
        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        runtimeData.painBlockadeActive = true;
    }

    @Override
    public void onUnEquipped(Player player, ChipInstance<?> instance) {
        PlayerRunTimeData runtimeData = GeneChipAPI.getPlayerRunTimeData(player);
        runtimeData.painBlockadeActive = false;
    }
}
