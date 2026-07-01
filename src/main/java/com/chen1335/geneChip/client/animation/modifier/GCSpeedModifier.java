package com.chen1335.geneChip.client.animation.modifier;

import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GCSpeedModifier extends SpeedModifier {
    @NotNull
    private final Player player;
    private float oldSpeed;
    public GCSpeedModifier(@NotNull Player player, int i) {
        super(i);
        this.player = player;
    }

    @Override
    public void tick() {
        super.tick();
    }
}
