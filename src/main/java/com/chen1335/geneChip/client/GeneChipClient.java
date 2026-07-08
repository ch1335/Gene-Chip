package com.chen1335.geneChip.client;

import com.chen1335.geneChip.attachmentData.PlayerChipData;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.animation.AnimationHandler;
import com.chen1335.geneChip.client.animation.GCAdjustmentModifier;
import com.chen1335.geneChip.network.AnimationPack;
import com.zigythebird.playeranim.api.PlayerAnimationAccess;
import com.zigythebird.playeranim.api.PlayerAnimationFactory;
import com.zigythebird.playeranimcore.animation.RawAnimation;
import com.zigythebird.playeranimcore.animation.layered.IAnimation;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractFadeModifier;
import com.zigythebird.playeranimcore.api.firstPerson.FirstPersonMode;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.easing.EasingType;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.apache.logging.log4j.util.Cast;

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

    public static void init() {
        PlayerAnimationFactory.ANIMATION_DATA_FACTORY.registerFactory(AnimationHandler.ANIMATION_RESOURCE, 42, player -> {
            GCModifierLayer controller = new GCModifierLayer(player);
            controller.setFirstPersonMode(FirstPersonMode.THIRD_PERSON_MODEL);
            controller.addModifierBefore(new GCAdjustmentModifier((partName, partialTick) -> {
                boolean handleHead = controller.getCurrentAnimation() != null
                        && !controller.get3DTransformRaw(new PlayerAnimBone("head")).getRotationVector().equals(Vec3f.ZERO);

                if ("head".equals(partName)) {
                    if (handleHead) {
                        return Optional.of(new GCAdjustmentModifier.PartModifier(
                                new Vec3f(
                                        Mth.lerp(partialTick, player.xRotO, player.getXRot()) * Mth.DEG_TO_RAD,
                                        Mth.lerp(partialTick, (player.yHeadRotO - player.yBodyRotO), (player.yHeadRot - player.yBodyRot)) * Mth.DEG_TO_RAD,
                                        0),
                                Vec3f.ZERO));
                    }
                    return Optional.empty();
                }
                return Optional.empty();
            }));
            return controller;
        });
    }

    public static void setup() {
        PLAYER_CHIP_DATA = new PlayerChipData();
    }

    public static void handlePlayerAnimation(int entityId, ResourceLocation animationLocation, IPayloadContext iPayloadContext) {
        iPayloadContext.enqueueWork(() -> {
            Entity entity = iPayloadContext.player().level().getEntity(entityId);
            if (entity instanceof LocalPlayer player) {
                IAnimation animation = PlayerAnimationAccess.getPlayerAnimationLayer(player, AnimationHandler.ANIMATION_RESOURCE);
                if (animation instanceof GCModifierLayer controller) {
                    if (animationLocation.equals(AnimationPack.EMPTY_ANIMATION)) {
                        controller.replaceAnimationWithFade(
                                AbstractFadeModifier.standardFadeIn(2, EasingType.EASE_IN_OUT_SINE), (RawAnimation) null);
                    } else {
                        controller.replaceAnimationWithFade(
                                AbstractFadeModifier.standardFadeIn(2, EasingType.EASE_IN_OUT_SINE), animationLocation);
                    }
                }
            }
        });
    }
}
