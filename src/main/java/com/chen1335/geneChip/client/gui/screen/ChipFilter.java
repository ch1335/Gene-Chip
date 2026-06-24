package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.client.GeneChipClient;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class ChipFilter {
    private String searchText = "";
    private ChipType typeFilter = null;
    private boolean showAll = false;

    public void setSearchText(String searchText) {
        this.searchText = searchText == null ? "" : searchText;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setTypeFilter(ChipType typeFilter) {
        this.typeFilter = typeFilter;
    }

    public ChipType getTypeFilter() {
        return typeFilter;
    }

    public void setShowAll(boolean showAll) {
        this.showAll = showAll;
    }

    public boolean isShowAll() {
        return showAll;
    }

    public List<Chip> getAvailableChips(Player player, EnumMap<ChipType, List<Chip>> map) {
        List<Chip> chips = new ArrayList<>();
        String search = searchText.toLowerCase().trim();
        for (Map.Entry<ChipType, List<Chip>> entry : map.entrySet()) {
            if (typeFilter != null && entry.getKey() != typeFilter) continue;
            for (Chip chip : entry.getValue()) {
                boolean unlocked = isUnlocked(chip);
                if (!showAll && !unlocked) continue;
                if (!search.isEmpty()) {
                    String name = chip.getDisplayName().getString().toLowerCase();
                    if (!name.contains(search)) continue;
                }
                chips.add(chip);
            }
        }
        return chips;
    }

    private static boolean isUnlocked(Chip chip) {
        return GeneChipClient.getPlayerChipData().getChipInfos().getChips()
                .getOrDefault(chip.getType(), Map.of()).containsKey(chip);
    }
}
