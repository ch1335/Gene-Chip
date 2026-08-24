package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ChipWidget extends AbstractWidget {
    private final ChipInstance<?> chipInstance;
    private final ChipConfigScreen parent;
    private final ChipConfigScreen.LayoutMetrics layout;
    private final float scale;

    public int index;
    public boolean unlocked;
    public boolean renderHoverDesc = true;

    private double pressX;
    private double pressY;
    private boolean draggingCard;

    public ChipWidget(ChipInstance<?> chipInstance, ChipConfigScreen parent, ChipConfigScreen.LayoutMetrics layout) {
        super(0, 0, Math.round(48 * layout.cardScale()), Math.round(78 * layout.cardScale()), Component.empty());
        this.chipInstance = chipInstance;
        this.parent = parent;
        this.layout = layout;
        this.scale = layout.cardScale();
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        RenderSystem.enableDepthTest();
        if (draggingCard) {
            pose.translate(0, 0, 60);
        }

        boolean gray = !unlocked;
        if (gray) {
            RenderSystem.setShaderColor(0.35F, 0.35F, 0.35F, 1.0F);
        }
        try {
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getCardFace(), guiGraphics,
                    getX(), getY(), 48 * scale, 78 * scale,
                    8, 1, 24, 39, 40, 40, 0);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getBigCrystalIcon(), guiGraphics,
                    getX() - 2 * scale, getY() - 2 * scale, 12 * scale, 12 * scale,
                    1, 1, 6, 6, 8, 8, 0);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getType().getSmallCrystalIcon(), guiGraphics,
                    getX() + 20 * scale, getY() + 50 * scale, 8 * scale, 8 * scale,
                    2, 2, 4, 4, 8, 8, 0);
            GuiUtil.drawTextureWithSize(chipInstance.getChip().getTexture(), guiGraphics,
                    getX() + 8 * scale, getY() + 16 * scale, 32 * scale, 32 * scale, 3);
        } finally {
            if (gray) {
                RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            }
        }

        int nameColor = gray ? 0x9A9A9A : 0xFFFFFF;
        int descColor = gray ? 0x808080 : 0xFFFFFF;

        pose.pushPose();
        Component displayName = chipInstance.getChip().getDisplayName();
        int stringWidth = minecraft.font.width(displayName);
        float nameScale = scale * 0.8F;
        pose.translate(getX() + 24 * scale - stringWidth * nameScale / 2, getY() + 2 * scale + 2, 0);
        pose.scale(nameScale, nameScale, 1);
        guiGraphics.drawString(minecraft.font, displayName, 0, 0, nameColor);
        pose.popPose();

        Component desc = chipInstance.getChip().detailDesc(chipInstance.getLvl());
        List<FormattedCharSequence> split = minecraft.font.split(desc, 100);
        float descScale = scale / 2.5F;
//        for (int i = 0; i < split.size(); i++) {
//            pose.pushPose();
//            pose.translate(getX() + 4.5F * scale,
//                    getY() + (67 + 5.5F * i) * scale - split.size() * 2.5F * scale, 0);
//            pose.scale(descScale, descScale, 1);
//            guiGraphics.drawString(minecraft.font, split.get(i), 0, 0, descColor);
//            pose.popPose();
//        }

        pose.popPose();
    }

    public void renderHoverTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!renderHoverDesc || !isHovered || parent.isDragging() || parent.isSelected(chipInstance)) return;

        Minecraft minecraft = Minecraft.getInstance();
        if (!unlocked) {
            List<Component> lines = new ArrayList<>();
            lines.add(chipInstance.getChip().getDisplayName().copy().withStyle(ChatFormatting.GRAY));
            lines.add(chipInstance.getChip().getDesc());
            lines.add(Component.translatable("gene_chip.chip.locked").withStyle(ChatFormatting.RED, ChatFormatting.ITALIC));
            guiGraphics.renderComponentTooltip(minecraft.font, lines, mouseX, mouseY);
        } else {
            guiGraphics.renderTooltip(minecraft.font, chipInstance.getChip().getDesc(), mouseX, mouseY);
        }
    }

    @Override
    public @Nullable Tooltip getTooltip() {
        return Tooltip.create(chipInstance.getChip().getDesc());
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!unlocked || button != 0 || !isMouseOver(mouseX, mouseY)) {
            return false;
        }
        pressX = mouseX;
        pressY = mouseY;
        draggingCard = false;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        super.onDrag(mouseX, mouseY, dragX, dragY);
        double threshold = Math.max(3, Math.round(4 * layout.scale()));
        double deltaX = mouseX - pressX;
        double deltaY = mouseY - pressY;
        if (!draggingCard && deltaX * deltaX + deltaY * deltaY < threshold * threshold) {
            return;
        }
        draggingCard = true;
        setX((int) Math.round(mouseX - 24 * scale));
        setY((int) Math.round(mouseY - 41 * scale));
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (!draggingCard) {
            parent.selectChip(chipInstance);
            setFocused(false);
            return;
        }

        int targetSlot = parent.slotAt(mouseX, mouseY);
        draggingCard = false;
        setFocused(false);
        if (targetSlot == -1) return;

        for (EquippedChipWidget equippedChipWidget : parent.equippedChipWidgets) {
            Optional<ChipInstance<?>> instance = equippedChipWidget.getChipSlot().instance();
            if (instance.isPresent() && instance.get().getChip() == this.chipInstance.getChip()) {
                return;
            }
        }
        parent.setSlotChip(chipInstance, targetSlot);
    }

    public boolean isDraggingCard() {
        return draggingCard;
    }

    public ChipInstance<?> getChipInstance() {
        return chipInstance;
    }
}
