package com.chen1335.geneChip.chip;

import com.chen1335.geneChip.GeneChip;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import org.jetbrains.annotations.NotNull;

public enum ChipType implements StringRepresentable {
    COMBAT("combat"),
    MUTATION("mutation"),
    SURVIVAL("survival"),
    TACTICS("tactics"),
    SPECIAL("special");
    public static final StreamCodec<ByteBuf, ChipType> STREAM_CODEC = ByteBufCodecs.STRING_UTF8.map(ChipType::valueOf, ChipType::name);
    private final String name;

    private final ResourceLocation bigCrystalIcon;
    private final ResourceLocation smallCrystalIcon;

    private final ResourceLocation cardFace;

    ChipType(String name) {
        this.name = name;
        this.bigCrystalIcon = ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "textures/chip/crystal_icons/%s_1.png".formatted(name));
        this.smallCrystalIcon = ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "textures/chip/crystal_icons/%s_2.png".formatted(name));
        this.cardFace = ResourceLocation.fromNamespaceAndPath(GeneChip.MODID, "textures/chip/card_faces/%s.png".formatted(name));
    }

    @Override
    public @NotNull String getSerializedName() {
        return name;
    }

    public ResourceLocation getSmallCrystalIcon() {
        return smallCrystalIcon;
    }

    public ResourceLocation getBigCrystalIcon() {
        return bigCrystalIcon;
    }

    public ResourceLocation getCardFace() {
        return cardFace;
    }
}
