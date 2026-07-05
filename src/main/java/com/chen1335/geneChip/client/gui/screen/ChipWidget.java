package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.chip.SlotInfos;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChipWidget extends AbstractWidget {
    private final ChipInstance<?> chipInstance;
    private final ChipConfigScreen parent;

    public float xScale = 1;
    public float yScale = 1;

    public int index = 0;

    public boolean unlocked = false;
    public boolean renderHoverDesc = true;
    public ChipWidget(ChipInstance<?> chipInstance, ChipConfigScreen parent) {
        super(0, 0, 0, 0, Component.empty());
        this.chipInstance = chipInstance;
        this.parent = parent;
        Vec2 windowScale = GuiUtil.getWindowScale();
        xScale = windowScale.x * 4*1.25F;
        yScale = windowScale.y * 4*1.25F;
        this.width = (int) (48 * xScale);
        this.height = (int) (78 * yScale);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft instance = Minecraft.getInstance();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        RenderSystem.enableDepthTest();
        if (isFocused()) {
            pose.translate(0, 0, 60);
        }

        boolean gray = !unlocked;
        if (gray) {
            RenderSystem.setShaderColor(0.35F, 0.35F, 0.35F, 1.0F);
        }
        try {
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getCardFace(), guiGraphics, getX(), getY(), 48 * xScale, 78 * yScale, 8, 1, 24, 39, 40, 40, 0);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getBigCrystalIcon(), guiGraphics, getX() - 2 * xScale, getY() - 2 * yScale, 12 * xScale, 12 * yScale, 1, 1, 6, 6, 8, 8, 0);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getSmallCrystalIcon(), guiGraphics, getX() + 20 * xScale, getY() + 50 * yScale, 8 * xScale, 8 * yScale, 2, 2, 4, 4, 8, 8, 0);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getTexture(), guiGraphics, getX() + 8 * xScale, getY() + 16 * yScale, 32 * xScale, 32 * yScale, 3);
        } finally {
            if (gray) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        int nameColor = gray ? 0x9A9A9A : 0xFFFFFF;
        int descColor = gray ? 0x808080 : 0xFFFFFF;

        pose.pushPose();
        Component displayName = chipInstance.getChip().getDisplayName();
        int stringWidth = instance.font.width(displayName);
        pose.translate(getX() + 32 * xScale - (float) stringWidth * xScale / 2, getY() + 2 * yScale + 2, 0);
        pose.scale(xScale * 0.8F, yScale * 0.8F, 1);
        guiGraphics.drawString(instance.font, displayName, 0, 0, nameColor);
        pose.popPose();

        Component desc = chipInstance.getChip().detailDesc(chipInstance.getLvl());
        List<FormattedCharSequence> split = instance.font.split(desc, 100);

        for (int i = 0; i < split.size(); i++) {
            FormattedCharSequence formattedCharSequence = split.get(i);
            pose.pushPose();
            pose.translate(getX() + (4.5) * xScale, getY() + (67 + 5.5 * i) * yScale - split.size() * 2.5 * yScale, 0);
            pose.scale(xScale / 2.5F, yScale / 2.5F, 1);
            guiGraphics.drawString(instance.font, formattedCharSequence, 0, 0, descColor);
            pose.popPose();
        }

        if (renderHoverDesc) {
            if (isHovered && !parent.isDragging()) {
                if (gray) {
                    List<Component> lines = new ArrayList<>();
                    lines.add(chipInstance.getChip().getDisplayName().copy().withStyle(ChatFormatting.GRAY));
                    lines.add(chipInstance.getChip().getDesc());
                    lines.add(Component.translatable("gene_chip.chip.locked").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
                    guiGraphics.renderComponentTooltip(instance.font, lines, mouseX, mouseY);
                } else {
                    guiGraphics.renderTooltip(instance.font, chipInstance.getChip().getDesc(), mouseX, mouseY);
                }
            }
        }
        pose.popPose();
    }

    @Override
    public @Nullable Tooltip getTooltip() {
        return Tooltip.create(chipInstance.getChip().getDesc());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {

    }

    @Override
    public boolean isHovered() {
        return super.isHovered();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!unlocked) {
            return false;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        setX((int) ((int) mouseX - 24 * xScale));
        setY((int) ((int) mouseY - 41 * yScale));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        setFocused(false);
        if (parent.hoveredSlot != -1) {
            List<EquippedChipWidget> equippedChipWidgets = parent.equippedChipWidgets;
            for (EquippedChipWidget equippedChipWidget : equippedChipWidgets) {
                Optional<ChipInstance<?>> instance = equippedChipWidget.getChipSlot().instance();
                if (instance.isPresent() && instance.get().getChip() == this.chipInstance.getChip()) {
                    return;
                }
            }

            parent.setSlotChip(chipInstance, parent.hoveredSlot);
        }
    }
}
