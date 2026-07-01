package com.chen1335.geneChip.client;

import com.chen1335.geneChip.client.animation.modifier.GCSpeedModifier;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.SpeedModifier;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

public class GCModifierLayer<T extends IAnimation> extends ModifierLayer<T> {
    public final GCSpeedModifier speedModifier;
    @NotNull
    private final Player owner;

    @Override
    public void tick() {
        super.tick();
    }

    public GCModifierLayer(@NotNull Player player){
        this.owner = player;
        speedModifier = new GCSpeedModifier(player,1);
        addModifierBefore(speedModifier);
    }
}
