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
import net.minecraft.world.phys.Vec2;

import java.util.Optional;

public class EquippedChipWidget extends AbstractWidget {
    private final float xScale;
    private final float yScale;

    private ChipSlot chipSlot;
    public int index = 0;
    private final ChipConfigScreen parent;

    private ChipWidget chipWidget = null;


    public EquippedChipWidget(ChipSlot chipSlot, int index, ChipConfigScreen parent) {
        super(0, 0, 0, 0, Component.empty());
        this.chipSlot = chipSlot;
        this.index = index;
        this.parent = parent;
        Vec2 windowScale = GuiUtil.getWindowScale();
        xScale = windowScale.x * 4;
        yScale = windowScale.y * 4;
        this.width = (int) (150 * xScale);
        this.height = (int) (18 * yScale);

        setX((int) (10 * xScale));

        int ySize = 18;
        int y1 = ySize + 3;
        int y = 40;
        setY((int) ((20 + index * y1 + y) * yScale));

        Optional<ChipInstance<?>> instance = chipSlot.instance();
        instance.ifPresent(chipInstance -> chipWidget = new ChipWidget(chipInstance, parent));
    }

    public ChipSlot getChipSlot() {
        return chipSlot;
    }

    public void setChipSlot(ChipSlot chipSlot) {
        this.chipSlot = chipSlot;
        chipSlot.instance().ifPresentOrElse(chipInstance -> chipWidget = new ChipWidget(chipInstance, parent), () -> {
            chipWidget = null;
        });
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        RenderSystem.enableDepthTest();
        chipSlot.instance().ifPresent(chipInstance -> {
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            if (isFocused()) {
                pose.translate(0, 0, 20);
            }
            guiGraphics.enableScissor(getX(), getY(), getX() + getWidth(), getY() + getHeight());
            RenderSystem.enableBlend();
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), getWidth(), getHeight(), FastColor.ARGB32.color(150, 0, 0, 0), 1);

            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getSmallCrystalIcon(), guiGraphics, getX() + 10 * xScale, getY() + 5 * yScale, 8 * xScale, 8 * yScale, 2, 2, 4, 4, 8, 8, 2);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getCardFace(), guiGraphics, getX() + 102 * xScale, getY() - 27 * yScale, 48 * xScale, 78 * yScale, 8, 1, 24, 39, 40, 40, 2);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getTexture(), guiGraphics, getX() + 110 * xScale, getY() - 9 * yScale, 32 * xScale, 32 * yScale, 2);

            pose.pushPose();
            Component displayName = chipInstance.getChip().getDisplayName();
            Font font = Minecraft.getInstance().font;
            pose.translate(getX() + 32 * xScale, getY() + 5 * yScale, 2);
            guiGraphics.drawString(font, displayName, 0, 0, 0xFFFFFF);
            pose.popPose();

            guiGraphics.disableScissor();
            pose.popPose();
        });
        if (isHovered && !isFocused()) {
            RenderSystem.enableBlend();
            int color = FastColor.ARGB32.color(150, 255, 255, 255);
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), getWidth(), getHeight(), color, 3);
            if (chipWidget != null) {
                chipWidget.unlocked = true;
                chipWidget.renderHoverDesc = false;
                chipWidget.setX(mouseX);
                chipWidget.setY((int) (mouseY - 40 * yScale));
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
        setX((int) ((int) mouseX - 100 * xScale));
        setY((int) ((int) mouseY - 7 * yScale));
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && isHovered) {
            parent.setSlotChip(null, index);
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        setX((int) (10 * xScale));

        int ySize = 18;
        int y1 = ySize + 3;
        int y = 40;
        setY((int) ((20 + index * y1 + y) * yScale));

        if (parent.hoveredSlot != -1) {
            EquippedChipWidget old = parent.equippedChipWidgets.get(parent.hoveredSlot);
            Optional<ChipInstance<?>> oldInstance = old.getChipSlot().instance();
            Optional<ChipInstance<?>> newInstance = this.getChipSlot().instance();
            if (oldInstance.isEmpty()) {
                parent.setSlotChip(null, getChipSlot().index());
            } else {
                parent.setSlotChip(oldInstance.get(), getChipSlot().index());
            }


            newInstance.ifPresent(chipInstance -> {
                parent.setSlotChip(chipInstance, parent.hoveredSlot);
            });
        }

        setFocused(false);
    }
}
