package com.chen1335.geneChip.client.animation.modifier;

import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.SpeedModifier;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GCSpeedModifier extends SpeedModifier {
    @NotNull
    private final Player player;

    public GCSpeedModifier(@NotNull Player player, float speed) {
        super(speed);
        this.player = player;
    }

    @Override
    public void tick(AnimationData state) {
        super.tick(state);
    }
}
