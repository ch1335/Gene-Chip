package com.chen1335.geneChip.client.animation;

import com.zigythebird.playeranimcore.animation.AnimationController;
import com.zigythebird.playeranimcore.animation.AnimationData;
import com.zigythebird.playeranimcore.animation.layered.modifier.AbstractModifier;
import com.zigythebird.playeranimcore.bones.PlayerAnimBone;
import com.zigythebird.playeranimcore.math.Vec3f;
import net.minecraft.MethodsReturnNonnullByDefault;
import org.jetbrains.annotations.NotNull;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class GCAdjustmentModifier extends AbstractModifier {
    public static final class PartModifier {
        private final Vec3f rotation;
        private final Vec3f scale;
        private final Vec3f offset;

        public PartModifier(Vec3f rotation, Vec3f offset) {
            this(rotation, Vec3f.ZERO, offset);
        }

        public PartModifier(Vec3f rotation, Vec3f scale, Vec3f offset) {
            this.rotation = rotation;
            this.scale = scale;
            this.offset = offset;
        }

        public Vec3f rotation() {
            return rotation;
        }

        public Vec3f scale() {
            return scale;
        }

        public Vec3f offset() {
            return offset;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == this) return true;
            if (obj == null || obj.getClass() != this.getClass()) return false;
            PartModifier that = (PartModifier) obj;
            return Objects.equals(this.rotation, that.rotation) &&
                    Objects.equals(this.scale, that.scale) &&
                    Objects.equals(this.offset, that.offset);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rotation, scale, offset);
        }

        @Override
        public String toString() {
            return "PartModifier[rotation=" + rotation + ", scale=" + scale + ", offset=" + offset + ']';
        }
    }

    public boolean enabled = true;

    protected BiFunction<String, Float, Optional<PartModifier>> source;

    private float tickDelta;

    public GCAdjustmentModifier(BiFunction<String, Float, Optional<PartModifier>> source) {
        this.source = source;
    }

    @Override
    public void tick(AnimationData state) {
        super.tick(state);
        if (remainingFadeout > 0) {
            remainingFadeout -= 1;
            if (remainingFadeout <= 0) {
                instructedFadeout = 0;
            }
        }
    }

    @Override
    public void setupAnim(AnimationData state) {
        super.setupAnim(state);
        this.tickDelta = state.getPartialTick();
    }

    protected int instructedFadeout = 0;
    private int remainingFadeout = 0;

    public void fadeOut(int fadeOut) {
        instructedFadeout = fadeOut;
        remainingFadeout = fadeOut + 1;
    }

    /**
     * 计算淡入系数，在动画的 beginTick 之前从 0 线性增长到 1，超过后保持 1。
     */
    protected float getFadeIn() {
        float fadeIn = 1;
        if (getController() instanceof AnimationController controller && controller.getCurrentAnimation() != null) {
            float beginTick = controller.getCurrentAnimation().animation().data().<Float>get("beginTick").orElse(0F);
            fadeIn = beginTick > 0 ? controller.getAnimationTicks() / beginTick : 1F;
            fadeIn = Math.min(fadeIn, 1F);
        }
        return fadeIn;
    }

    /**
     * 计算淡出系数：若手动触发 fadeOut(int) 则在 instructedFadeout 时间内从 1 线性降到 0；
     * 否则在动画的 endTick~stop 区间从 1 线性降到 0。
     */
    protected float getFadeOut(float delta) {
        float fadeOut = 1;
        if (remainingFadeout > 0 && instructedFadeout > 0) {
            float current = Math.max(remainingFadeout - delta, 0);
            fadeOut = current / instructedFadeout;
            return Math.min(fadeOut, 1F);
        }
        if (getController() instanceof AnimationController controller && controller.getCurrentAnimation() != null) {
            float stopTick = controller.getCurrentAnimation().animation().length();
            float endTick = controller.getCurrentAnimation().animation().data().<Float>get("endTick").orElse(stopTick);
            float position = (-1F) * (controller.getAnimationTicks() - stopTick);
            float length = stopTick - endTick;
            if (length > 0) {
                fadeOut = position / length;
                fadeOut = Math.min(fadeOut, 1F);
            }
        }
        return fadeOut;
    }

    @Override
    public PlayerAnimBone get3DTransform(@NotNull PlayerAnimBone bone) {
        if (!enabled) {
            return super.get3DTransform(bone);
        }

        Optional<PartModifier> partModifier = source.apply(bone.getName(), tickDelta);
        float fade = getFadeIn() * getFadeOut(tickDelta);
        if (partModifier.isPresent()) {
            super.get3DTransform(bone);
            transformBone(bone, partModifier.get(), fade);
            return bone;
        }
        return super.get3DTransform(bone);
    }

    protected void transformBone(PlayerAnimBone bone, PartModifier partModifier, float fade) {
        Vec3f pos = partModifier.offset().mul(fade);
        Vec3f rot = partModifier.rotation().mul(fade);
        Vec3f scale = partModifier.scale().mul(fade);
        bone.updatePosition(pos.x() + bone.getPosX(), pos.y() + bone.getPosY(), pos.z() + bone.getPosZ());
        bone.updateRotation(rot.x() + bone.getRotX(), rot.y() + bone.getRotY(), rot.z() + bone.getRotZ());
        bone.updateScale(scale.x() + bone.getScaleX(), scale.y() + bone.getScaleY(), scale.z() + bone.getScaleZ());
    }
}
