package com.chen1335.geneChip.client;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.common.EventBusSubscriber;
import org.apache.logging.log4j.util.Cast;

import java.util.Map;

public class GeneChipClient {
    public static Player getClientPlayer(){
        return Minecraft.getInstance().player;
    }

    private static PlayerChipData PLAYER_CHIP_DATA = new PlayerChipData();

    public static <T extends Chip> ChipInstance<T> getPlayerChip(T chip) {
        return Cast.cast(getPlayerChipData().getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip));
    }

    public static PlayerChipData getPlayerChipData() {
        return PLAYER_CHIP_DATA;
    }

}
