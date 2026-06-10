package com.chen1335.geneChip.mixins.clavis;

import com.chen1335.geneChip.API.GeneChipAPI;
import com.chen1335.geneChip.API.ILockExtension;
import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.chip.chips.special.LocksmithIntuition;
import it.hurts.shatterbyte.clavis.common.data.Lock;
import it.hurts.shatterbyte.clavis.common.network.packet.OpenLockpickingPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(OpenLockpickingPacket.class)
public abstract class ClavisLockMixin {

    @Shadow
    Lock lock;

    @Inject(method = "handleClient", at = @At("HEAD"))
    private void onHandleClient(CallbackInfo ci) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        GeneChipAPI.getPlayerEquippedChip(player, ChipTypes.LOCKSMITH_INTUITION).ifPresent(chipInstance -> {
            LocksmithIntuition chip = chipInstance.getChip();
            float reduction = chip.difficultyReduction.getValue(chipInstance.getLvl());
            ILockExtension lockExtension = (ILockExtension) this.lock;
            lockExtension.GC$setDifficulty(lockExtension.GC$getDifficulty() * reduction);
        });
    }
}
