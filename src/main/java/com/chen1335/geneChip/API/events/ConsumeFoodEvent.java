package com.chen1335.geneChip.API.events;

import com.chen1335.geneChip.chip.ChipInstance;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class ConsumeFoodEvent extends Event implements ICancellableEvent {
    private final Player player;
    //消耗的饱食度，正整数
    private final int food;
    private final ChipInstance<?> instance;

    public ConsumeFoodEvent(Player player, int food, ChipInstance<?> instance) {
        this.player = player;
        this.food = food;
        this.instance = instance;
    }

    public Player getPlayer() {
        return player;
    }

    public int getFood() {
        return food;
    }

    public ChipInstance<?> getInstance() {
        return instance;
    }
}
