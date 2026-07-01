package com.chen1335.geneChip.client;

import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.animation.AnimationHandler;
import com.chen1335.geneChip.client.animation.GCAdjustmentModifier;
import com.chen1335.geneChip.network.AnimationPack;
import dev.kosmx.playerAnim.api.TransformType;
import dev.kosmx.playerAnim.api.layered.IActualAnimation;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.api.layered.modifier.AbstractFadeModifier;
import dev.kosmx.playerAnim.core.util.Ease;
import dev.kosmx.playerAnim.core.util.Vec3f;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationFactory;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.util.Cast;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

public class GeneChipClient {
    public static Player getClientPlayer() {
        return Minecraft.getInstance().player;
    }

    private static PlayerChipData PLAYER_CHIP_DATA;

    public static <T extends Chip> ChipInstance<T> getPlayerChip(T chip) {
        return Cast.cast(getPlayerChipData().getChipInfos().getChips().getOrDefault(chip.getType(), Map.of()).get(chip));
    }


    public static <T extends Chip> Optional<ChipInstance<T>> getPlayerEquippedChip(Supplier<T> chip) {
        PlayerChipData data = getPlayerChipData();
        return Optional.ofNullable(Cast.cast(data.getSlotInfos().getCurrent().get(chip.get())));
    }

    public static PlayerChipData getPlayerChipData() {
        return PLAYER_CHIP_DATA;
    }

    public static final ModifierLayer<IAnimation> LAYER = new ModifierLayer<>();

    public static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(AnimationHandler.ANIMATION_RESOURCE, 42, player -> {
            ModifierLayer<IAnimation> layer = new ModifierLayer<>();
            layer.addModifierBefore(new GCAdjustmentModifier((partName, partialTick) -> {
                boolean handleHead = layer.getAnimation() != null && !layer.getAnimation().get3DTransform("head", TransformType.ROTATION, 0.5F, Vec3f.ZERO).equals(Vec3f.ZERO);

                switch (partName) {
                    case "head" -> {
                        if (handleHead) {
                            return Optional.of(new GCAdjustmentModifier.PartModifier(new Vec3f(Mth.lerp(partialTick, player.xRotO, player.getXRot()) * Mth.DEG_TO_RAD, Mth.lerp(partialTick, (player.yHeadRotO - player.yBodyRotO), (player.yHeadRot - player.yBodyRot)) * Mth.DEG_TO_RAD, 0), Vec3f.ZERO));
                        }else {
                            return Optional.empty();
                        }
                    }
                    case "rightLeg" -> {

                        return Optional.empty();
                    }
                    case "leftLeg" -> {
                        return Optional.empty();
                    }
                    default -> {
                        return Optional.empty();
                    }
                }
            }));


            return layer;
        });
    }

    public static void setup() {
        PLAYER_CHIP_DATA = new PlayerChipData();
    }

    public static void handlePlayerAnimation(int entityId, ResourceLocation animationLocation, IPayloadContext iPayloadContext) {
        iPayloadContext.enqueueWork(() -> {

            Entity entity = iPayloadContext.player().level().getEntity(entityId);
            if (entity instanceof LocalPlayer player) {
                @Nullable IAnimation animation = PlayerAnimationAccess.getPlayerAssociatedData(player).get(AnimationHandler.ANIMATION_RESOURCE);

                ModifierLayer<IAnimation> modifierLayer = (ModifierLayer<IAnimation>) animation;
                if (modifierLayer != null) {
                    if (animationLocation.equals(AnimationPack.EMPTY_ANIMATION)) {
                        modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), null);

                    } else {
                        IActualAnimation<?> iActualAnimation = PlayerAnimationRegistry
                                .getAnimation(animationLocation)
                                .playAnimation();
                        modifierLayer.replaceAnimationWithFade(AbstractFadeModifier.standardFadeIn(2, Ease.INOUTSINE), iActualAnimation);
                    }
                }
            }
        });
    }
}
