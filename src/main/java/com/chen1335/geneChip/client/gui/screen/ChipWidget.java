package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.chip.SlotInfos;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
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

import java.util.List;
import java.util.Optional;

public class ChipWidget extends AbstractWidget {
    private final ChipInstance<?> chipInstance;
    private final ChipConfigScreen parent;

    public float xScale = 1;
    public float yScale = 1;

    public int index = 0;

    public boolean unlocked = false;

    public ChipWidget(ChipInstance<?> chipInstance, ChipConfigScreen parent) {
        super(0, 0, 0, 0, Component.empty());
        this.chipInstance = chipInstance;
        this.parent = parent;
        Vec2 windowScale = GuiUtil.getWindowScale();
        xScale = windowScale.x * 2;
        yScale = windowScale.y * 2;
        this.width = (int) (48 * xScale);
        this.height = (int) (78 * yScale);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft instance = Minecraft.getInstance();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 10);
        RenderSystem.enableDepthTest();
        if (isFocused()) {
            pose.translate(0, 0, 20);
        }
        GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getCardFace(), guiGraphics, getX(), getY(), 48 * xScale, 78 * yScale, 8, 1, 24, 39, 40, 40, 0);
        GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getBigCrystalIcon(), guiGraphics, getX() - 2 * xScale, getY() - 2 * yScale, 12 * xScale, 12 * yScale, 1, 1, 6, 6, 8, 8, 0);
        GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getSmallCrystalIcon(), guiGraphics, getX() + 20 * xScale, getY() + 50 * yScale, 8 * xScale, 8 * yScale, 2, 2, 4, 4, 8, 8, 0);
        GuiUtil.drawTextureWithSize(chipInstance.getChip().getTexture(), guiGraphics, getX() + 8 * xScale, getY() + 16 * yScale, 32 * xScale, 32 * yScale, 3);


        pose.pushPose();
        Component displayName = chipInstance.getChip().getDisplayName();
        int stringWidth = instance.font.width(displayName);
        pose.translate(getX() + 32 * xScale - (float) stringWidth * xScale / 2, getY() + 2 * yScale + 2, 0);
        pose.scale(xScale * 0.8F, yScale * 0.8F, 1);
        guiGraphics.drawString(instance.font, displayName, 0, 0, 0xFFFFFF);
        pose.popPose();

        Component desc = chipInstance.getChip().detailDesc(chipInstance.getLvl());
        int descWidth = instance.font.width(desc);
        List<FormattedCharSequence> split = instance.font.split(desc, 80);

        for (int i = 0; i < split.size(); i++) {
            FormattedCharSequence formattedCharSequence = split.get(i);
            pose.pushPose();
            pose.translate(getX() + 46 * xScale - (float) 40 * xScale, getY() + (67 + 5.5 * i) * yScale - split.size() * 2.5 * yScale, 0);
            pose.scale(xScale / 2, yScale / 2, 1);
            guiGraphics.drawString(instance.font, formattedCharSequence, 0, 0, 0xFFFFFF);
            pose.popPose();
        }

        if (isHovered && !parent.isDragging()) {
            guiGraphics.renderTooltip(instance.font, chipInstance.getChip().getDesc(), mouseX, mouseY);
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
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        setX((int) ((int) mouseX - 24 * xScale));
        setY((int) ((int) mouseY - 41 * yScale));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (parent.hoveredSlot != -1) {
            parent.setSlotChip(chipInstance, parent.hoveredSlot);
        }
        setFocused(false);
    }
}
