package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;

import java.util.Optional;

public class EquippedChipWidget extends AbstractWidget {
    private ChipSlot chipSlot;
    public final int index;
    private final ChipConfigScreen parent;
    private final ChipConfigScreen.LayoutMetrics layout;
    private final float scale;

    private ChipWidget chipWidget;
    private double pressX;
    private double pressY;
    private boolean draggingSlot;

    public EquippedChipWidget(ChipSlot chipSlot, int index, ChipConfigScreen parent,
                              ChipConfigScreen.LayoutMetrics layout) {
        super(0, 0, 0, 0, Component.empty());
        this.chipSlot = chipSlot;
        this.index = index;
        this.parent = parent;
        this.layout = layout;
        this.scale = layout.scale();
        resetPosition();
        chipSlot.instance().ifPresent(chipInstance -> chipWidget = new ChipWidget(chipInstance, parent, layout));
    }

    private void resetPosition() {
        ChipConfigScreen.LayoutRect rect = layout.slotRect(index);
        setX(rect.left());
        setY(rect.top());
        this.width = rect.width();
        this.height = rect.height();
    }

    public ChipSlot getChipSlot() {
        return chipSlot;
    }

    public void setChipSlot(ChipSlot chipSlot) {
        this.chipSlot = chipSlot;
        chipSlot.instance().ifPresentOrElse(
                chipInstance -> chipWidget = new ChipWidget(chipInstance, parent, layout),
                () -> chipWidget = null);
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        chipSlot.instance().ifPresent(chipInstance -> {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            if (draggingSlot) {
                pose.translate(0, 0, 20);
            }
            guiGraphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            try {
                RenderSystem.enableBlend();
                GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                        FastColor.ARGB32.color(150, 0, 0, 0), 1);
                GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getSmallCrystalIcon(), guiGraphics,
                        getX() + 10 * scale, getY() + 5 * scale, 8 * scale, 8 * scale,
                        2, 2, 4, 4, 8, 8, 2);
                GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getCardFace(), guiGraphics,
                        getX() + 37 * scale, getY() - 27 * scale, 48 * scale, 78 * scale,
                        8, 1, 24, 39, 40, 40, 2);
                GuiUtil.drawTextureWithSize(chipInstance.getChip().getTexture(), guiGraphics,
                        getX() + 45 * scale, getY() - 9 * scale, 32 * scale, 32 * scale, 2);

                Component displayName = chipInstance.getChip().getDisplayName();
                Font font = Minecraft.getInstance().font;
                pose.pushPose();
                pose.translate(getX() + 22 * scale, getY() + 5 * scale, 2);
                pose.scale(scale * 0.72F, scale * 0.72F, 1);
                guiGraphics.drawString(font, displayName, 0, 0, 0xFFFFFF);
                pose.popPose();
            } finally {
                guiGraphics.disableScissor();
                pose.popPose();
            }
        });

        if (isHovered && !isFocused()) {
            RenderSystem.enableBlend();
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), getWidth(), getHeight(),
                    FastColor.ARGB32.color(150, 255, 255, 255), 3);
            if (chipWidget != null && !parent.isDragging()) {
                chipWidget.unlocked = true;
                chipWidget.renderHoverDesc = false;
                chipWidget.setX(mouseX);
                chipWidget.setY((int) (mouseY - 40 * scale));
                PoseStack pose = guiGraphics.pose();
                pose.pushPose();
                pose.translate(0, 0, 40);
                chipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
                pose.popPose();
            }
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        double threshold = Math.max(3, Math.round(4 * layout.scale()));
        double deltaX = mouseX - pressX;
        double deltaY = mouseY - pressY;
        if (!draggingSlot && deltaX * deltaX + deltaY * deltaY < threshold * threshold) {
            return;
        }
        draggingSlot = true;
        setX((int) Math.round(mouseX - 42 * scale));
        setY((int) Math.round(mouseY - 7 * scale));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && isMouseOver(mouseX, mouseY)) {
            parent.setSlotChip(null, index);
            return true;
        }
        if (button != 0 || chipSlot.instance().isEmpty() || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        pressX = mouseX;
        pressY = mouseY;
        draggingSlot = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        Optional<ChipInstance<?>> sourceInstance = this.getChipSlot().instance();
        if (!draggingSlot) {
            sourceInstance.ifPresent(parent::selectChip);
            setFocused(false);
            return;
        }

        resetPosition();
        draggingSlot = false;
        int targetSlot = parent.slotAt(mouseX, mouseY);
        if (targetSlot != -1 && targetSlot != index) {
            EquippedChipWidget target = parent.equippedChipWidgets.get(targetSlot);
            Optional<ChipInstance<?>> targetInstance = target.getChipSlot().instance();

            parent.setSlotChip(targetInstance.orElse(null), index);
            sourceInstance.ifPresent(chipInstance -> parent.setSlotChip(chipInstance, targetSlot));
        }
        setFocused(false);
    }
}
