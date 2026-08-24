package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.chen1335.geneChip.network.ChipSelectedPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class ChipSelectScreen extends Screen {
    // 动画各阶段时长（毫秒），墙钟时间驱动，与游戏刻/暂停/帧率解耦，插值天然连续
    private static final long ENTER_DURATION = 1000;
    private static final long EXPAND_DURATION = 750;
    private static final long EXPAND_PHASE_END = ENTER_DURATION + EXPAND_DURATION;
    private static final long FLIP_DURATION = 600;
    private static final long FLIP_PHASE_END = EXPAND_PHASE_END + FLIP_DURATION;

    private static final float CARD_SCALE = 1.6F;
    private static final int CARD_W = (int) (48 * CARD_SCALE);
    private static final int CARD_H = (int) (78 * CARD_SCALE);
    private static final int SPREAD = CARD_W + 20;
    private static final int EFFECT_PANEL_WIDTH = CARD_W + SPREAD * 2;
    private static final int EFFECT_PANEL_HEIGHT = 53;
    private static final int EFFECT_PANEL_GAP = 12;

    private static final ResourceLocation CARD_BACK =
            ResourceLocation.fromNamespaceAndPath("gene_chip", "textures/chip/card_back.png");

    private final List<ChipInstance<?>> candidates;

    private long startTimeMs = -1;
    private boolean selected = false;
    private int hoveredIndex = -1;

    public ChipSelectScreen(List<ChipInstance<?>> candidates) {
        super(Component.translatable("gene_chip.chip_select.title"));
        this.candidates = candidates;
    }

    @Override
    protected void init() {
        super.init();
        if (startTimeMs < 0) {
            startTimeMs = Util.getMillis();
        }
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }

    /** 自界面打开以来经过的毫秒数（墙钟时间，与帧率/暂停无关）。 */
    private long elapsed() {
        if (startTimeMs < 0) return 0;
        return Util.getMillis() - startTimeMs;
    }

    private boolean animationFinished() {
        return elapsed() >= FLIP_PHASE_END;
    }

    private float easeOutCubic(float t) {
        float x = Math.max(0.0F, Math.min(1.0F, t));
        return 1.0F - (1.0F - x) * (1.0F - x) * (1.0F - x);
    }

    private float enterProgress() {
        long e = elapsed();
        if (e >= ENTER_DURATION) return 1.0F;
        return easeOutCubic((float) e / ENTER_DURATION);
    }

    private float expandProgress() {
        long e = elapsed();
        if (e < ENTER_DURATION) return 0.0F;
        if (e >= EXPAND_PHASE_END) return 1.0F;
        return easeOutCubic((float) (e - ENTER_DURATION) / EXPAND_DURATION);
    }

    private float flipProgress() {
        long e = elapsed();
        if (e < EXPAND_PHASE_END) return 0.0F;
        if (e >= FLIP_PHASE_END) return 1.0F;
        return easeOutCubic((float) (e - EXPAND_PHASE_END) / FLIP_DURATION);
    }

    private float[] cardCenter(int i) {
        int n = candidates.size();
        int mid = n / 2;
        int targetOffsetX = (i - mid) * SPREAD;
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        float enter = enterProgress();
        float expand = expandProgress();

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
        if (animationFinished() && !selected) {
            for (int i = 0; i < candidates.size(); i++) {
                float[] center = cardCenter(i);
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
            float[] center = cardCenter(i);
            float x = center[0] - (float) CARD_W / 2;
            float y = center[1] - (float) CARD_H / 2;
            if (hoveredIndex == i) {
                y -= 10;
            }
            pose.translate(0,0,30);
            renderCard(guiGraphics, candidates.get(i), x, y, CARD_SCALE, hoveredIndex == i);

        }
        pose.popPose();

        if (animationFinished() && !selected) {
            renderEffectPanel(guiGraphics);
        }
    }

    private void renderEffectPanel(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        int panelWidth = Math.min(EFFECT_PANEL_WIDTH, this.width - 16);
        int panelX = (this.width - panelWidth) / 2;
        int cardBottom = this.height / 2 + CARD_H / 2;
        int panelY = Math.min(cardBottom + EFFECT_PANEL_GAP, this.height - EFFECT_PANEL_HEIGHT - 8);

        GuiUtil.drawColorWithSize(guiGraphics, panelX, panelY, panelWidth, EFFECT_PANEL_HEIGHT,
                0xFFD0D0D0, 40);
        GuiUtil.drawColorWithSize(guiGraphics, panelX + 1, panelY + 1,
                panelWidth - 2, EFFECT_PANEL_HEIGHT - 2, 0xE0282828, 41);

        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, 50);
        if (hoveredIndex < 0) {
            Component hint = Component.translatable("gene_chip.chip_select.effect_hint");
            guiGraphics.drawCenteredString(mc.font, hint, this.width / 2,
                    panelY + (EFFECT_PANEL_HEIGHT - mc.font.lineHeight) / 2, 0xFFB0B0B0);
            pose.popPose();
            return;
        }

        ChipInstance<?> hovered = candidates.get(hoveredIndex);
        Component name = hovered.getChip().getDisplayName();
        guiGraphics.drawCenteredString(mc.font, name, this.width / 2, panelY + 6, 0xFFFFFFFF);

        Component effect = hovered.getChip().detailDesc(hovered.getLvl());
        List<FormattedCharSequence> lines = mc.font.split(effect, panelWidth - 16);
        int maxLines = Math.min(lines.size(), 3);
        int textY = panelY + 20;
        for (int index = 0; index < maxLines; index++) {
            guiGraphics.drawCenteredString(mc.font, lines.get(index), this.width / 2,
                    textY + index * (mc.font.lineHeight + 1), 0xFFDDDDDD);
        }
        pose.popPose();
    }

    private void renderCard(GuiGraphics guiGraphics, ChipInstance<?> instance, float x, float y, float scale, boolean hovered) {
        Minecraft mc = Minecraft.getInstance();
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, hovered ? 50 : 10);
        RenderSystem.enableDepthTest();

        float flip = flipProgress();
        float scaleX;
        boolean showFront;
        if (flip < 0.5F) {
            scaleX = 1.0F - flip * 2.0F;
            showFront = false;
        } else {
            scaleX = (flip - 0.5F) * 2.0F;
            showFront = true;
        }
        float cardCenterX = x + 24 * scale;
        pose.translate(cardCenterX, 0, 0);
        pose.scale(scaleX, 1, 1);
        pose.translate(-cardCenterX, 0, 0);

        if (showFront) {
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

        } else {
            GuiUtil.drawTextureWithSize(CARD_BACK, guiGraphics,
                    x, y, 48 * scale, 78 * scale, 0);
        }

        pose.popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (selected) return false;
        if (!animationFinished()) return false;

        for (int i = 0; i < candidates.size(); i++) {
            float[] center = cardCenter(i);
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
