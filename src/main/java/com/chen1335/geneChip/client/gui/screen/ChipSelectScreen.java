package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.chen1335.geneChip.network.ChipSelectedPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ChipSelectScreen extends Screen {
    private static final int ENTER_DURATION = 20;
    private static final int EXPAND_DURATION = 15;
    private static final int EXPAND_PHASE_END = ENTER_DURATION + EXPAND_DURATION;

    private static final float CARD_SCALE = 1.6F;
    private static final int CARD_W = (int) (48 * CARD_SCALE);
    private static final int CARD_H = (int) (78 * CARD_SCALE);
    private static final int SPREAD = CARD_W + 20;

    private final List<ChipInstance<?>> candidates;

    private int animationTick = 0;
    private boolean selected = false;
    private int hoveredIndex = -1;

    public ChipSelectScreen(List<ChipInstance<?>> candidates) {
        super(Component.translatable("gene_chip.chip_select.title"));
        this.candidates = candidates;
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    public void tick() {
        if (animationTick < EXPAND_PHASE_END) {
            animationTick++;
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    private float easeOutCubic(float t) {
        float x = Math.max(0.0F, Math.min(1.0F, t));
        return 1.0F - (1.0F - x) * (1.0F - x) * (1.0F - x);
    }

    private float enterProgress(float partialTick) {
        if (animationTick >= ENTER_DURATION) return 1.0F;
        return easeOutCubic(((float) animationTick + partialTick) / ENTER_DURATION);
    }

    private float expandProgress(float partialTick) {
        if (animationTick < ENTER_DURATION) return 0.0F;
        if (animationTick >= EXPAND_PHASE_END) return 1.0F;
        return easeOutCubic(((float)animationTick + partialTick - ENTER_DURATION) / EXPAND_DURATION);
    }

    private float[] cardCenter(int i, float partialTick) {
        int n = candidates.size();
        int mid = n / 2;
        int targetOffsetX = (i - mid) * SPREAD;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float enter = enterProgress(partialTick);
        float expand = expandProgress(partialTick);

        float endX = centerX + targetOffsetX;
        float curX = (float) centerX + (endX - (float) centerX) * expand;

        float startY = this.height + CARD_H;
        float curY = startY + ((float) centerY - startY) * enter;

        return new float[]{ curX,  curY};
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();
        GuiUtil.drawColorWithSize(guiGraphics, 0, 0, this.width, this.height, 0xC0202020, 0);

        hoveredIndex = -1;
        if (animationTick >= EXPAND_PHASE_END && !selected) {
            for (int i = 0; i < candidates.size(); i++) {
                float[] center = cardCenter(i, partialTick);
                float x = center[0] - (float) CARD_W / 2;
                float y = center[1] - (float) CARD_H / 2;
                if (mouseX >= x && mouseX <= x + CARD_W && mouseY >= y && mouseY <= y + CARD_H) {
                    hoveredIndex = i;
                    break;
                }
            }
        }
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        Minecraft mc = Minecraft.getInstance();
        for (int i = 0; i < candidates.size(); i++) {
            float[] center = cardCenter(i, partialTick);
            float x = center[0] - (float) CARD_W / 2;
            float y = center[1] - (float) CARD_H / 2;
            if (hoveredIndex == i) {
                y -= 10;
            }
            pose.translate(0,0,30);
            renderCard(guiGraphics, candidates.get(i), x, y, CARD_SCALE, hoveredIndex == i);

        }
        pose.popPose();

        if (animationTick >= EXPAND_PHASE_END && !selected) {
            Component hint = Component.translatable("gene_chip.chip_select.hint");
            guiGraphics.drawCenteredString(mc.font, hint, this.width / 2, this.height - 20, 0xFFFFFFFF);
        }
    }

    private void renderCard(GuiGraphics guiGraphics, ChipInstance<?> instance, float x, float y, float scale, boolean hovered) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, hovered ? 50 : 10);
        RenderSystem.enableDepthTest();

        GuiUtil.drawTextureWithSize(instance.getChip().getType().getCardFace(), guiGraphics,
                x, y, 48 * scale, 78 * scale, 8, 1, 24, 39, 40, 40, 0);
        GuiUtil.drawTextureWithSize(instance.getChip().getType().getBigCrystalIcon(), guiGraphics,
                x - 2 * scale, y - 2 * scale, 12 * scale, 12 * scale, 1, 1, 6, 6, 8, 8, 0);
        GuiUtil.drawTextureWithSize(instance.getChip().getType().getSmallCrystalIcon(), guiGraphics,
                x + 20 * scale, y + 50 * scale, 8 * scale, 8 * scale, 2, 2, 4, 4, 8, 8, 0);
        GuiUtil.drawTextureWithSize(instance.getChip().getTexture(), guiGraphics,
                x + 8 * scale, y + 16 * scale, 32 * scale, 32 * scale, 3);

        pose.pushPose();
        Component displayName = instance.getChip().getDisplayName();
        int stringWidth = mc.font.width(displayName);
        pose.translate(x + 32 * scale - stringWidth * scale * 0.8F / 2, y + 2 * scale + 2, 0);
        pose.scale(scale * 0.8F, scale * 0.8F, 1);
        guiGraphics.drawString(mc.font, displayName, 0, 0, 0xFFFFFFFF, false);
        pose.popPose();

        Component desc = instance.getChip().detailDesc(instance.getLvl());
        List<FormattedCharSequence> split = mc.font.split(desc, (int) (60 * scale));
        for (int i = 0; i < split.size(); i++) {
            FormattedCharSequence line = split.get(i);
            pose.pushPose();
            pose.translate(x + 4.5 * scale, y + (67 + 5.5 * i) * scale - split.size() * 2.5 * scale, 0);
            pose.scale(scale / 2.5F, scale / 2.5F, 1);
            guiGraphics.drawString(mc.font, line, 0, 0, 0xFFFFFFFF, false);
            pose.popPose();
        }

        pose.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (selected) return false;
        if (animationTick < EXPAND_PHASE_END) return false;

        for (int i = 0; i < candidates.size(); i++) {
            float[] center = cardCenter(i, 0);
            float x = center[0] - (float) CARD_W / 2;
            float y = center[1] - (float) CARD_H / 2;
            if (mouseX >= x && mouseX <= x + CARD_W && mouseY >= y && mouseY <= y + CARD_H) {
                selected = true;
                PacketDistributor.sendToServer(new ChipSelectedPacket(candidates.get(i)));
                Minecraft.getInstance().setScreen(null);
                return true;
            }
        }
        return false;
    }
}
