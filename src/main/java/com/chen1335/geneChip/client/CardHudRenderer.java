package com.chen1335.geneChip.client;

import com.chen1335.geneChip.API.object.ChipTypes;
import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.chip.Chip;
import it.hurts.shatterbyte.clavis.common.client.screen.LockpickingScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public final class CardHudRenderer {
    private static final int ICON_SIZE = 18;

    private CardHudRenderer() {
    }

    @SubscribeEvent
    public static void render(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui) return;
        GuiGraphics graphics = event.getGuiGraphics();
        int screenWidth = graphics.guiWidth();
        int screenHeight = graphics.guiHeight();
        int x = screenWidth - 24;
        int y = screenHeight - 45;

        List<ClientCardHudState.CooldownEntry> cooldowns = ClientCardHudState.cooldowns().stream()
                .sorted(Comparator.comparing(entry -> entry.chip().toString()))
                .limit(4)
                .toList();
        for (ClientCardHudState.CooldownEntry entry : cooldowns) {
            Chip chip = RegisterTypes.CHIP.get(entry.chip());
            if (chip == null) continue;
            drawIcon(graphics, chip.getTexture(), x, y);
            float ratio = Mth.clamp(entry.remainingTicks() / (float) Math.max(1, entry.totalTicks()), 0, 1);
            int cover = Mth.ceil(ICON_SIZE * ratio);
            graphics.fill(x, y + ICON_SIZE - cover, x + ICON_SIZE, y + ICON_SIZE, 0x99000000);
            String seconds = String.format("%.1f", entry.remainingTicks() / 20.0F);
            graphics.drawString(minecraft.font, seconds, x - minecraft.font.width(seconds) - 2, y + 5, 0xFFFFFFFF, true);
            y -= 22;
        }

        List<StatusEntry> statuses = collectStatuses();
        for (int i = 0; i < Math.min(2, statuses.size()); i++) {
            StatusEntry status = statuses.get(i);
            Chip chip = RegisterTypes.CHIP.get(status.chip);
            if (chip == null) continue;
            drawIcon(graphics, chip.getTexture(), x, y);
            if (status.pulse) {
                int alpha = 55 + (int) (35 * Math.sin(ClientCardHudState.statusPulseTicks * 0.7));
                graphics.renderOutline(x - 2, y - 2, ICON_SIZE + 4, ICON_SIZE + 4,
                        (Mth.clamp(alpha, 20, 90) << 24) | 0x00FFFFFF);
            }
            graphics.drawString(minecraft.font, status.text, x - minecraft.font.width(status.text) - 2, y + 5, status.color, true);
            y -= 22;
        }

        renderFeedback(graphics, minecraft, screenWidth, screenHeight);
        renderFeverOverlay(graphics, screenWidth, screenHeight);
    }

    private static List<StatusEntry> collectStatuses() {
        List<StatusEntry> result = new ArrayList<>();
        if (ClientCardHudState.adrenalEquipped && ClientCardHudState.adrenalReady) {
            result.add(new StatusEntry(0, GeneChip.id("adrenal_gland_burst"), "READY", 0xFFFFE37A, true));
        }
        if (ClientCardHudState.growingFervorEquipped) {
            String stage = switch (ClientCardHudState.growingFervorStage) {
                case 1 -> "I";
                case 2 -> "II";
                case 3 -> "III";
                default -> "0";
            };
            result.add(new StatusEntry(1, GeneChip.id("growing_fervor"), stage, 0xFFFF6B8A,
                    ClientCardHudState.statusPulseTicks > 0));
        }
        if (ClientCardHudState.thickSkinnedActive) {
            result.add(new StatusEntry(1, GeneChip.id("thick_skinned"), "+4", 0xFFD9D1B8, false));
        }
        if (ClientCardHudState.infectedInZone) {
            result.add(new StatusEntry(1, GeneChip.id("infected"), "ZONE", 0xFFC879FF, false));
        }
        if (ClientCardHudState.counterStormTicks > 0) {
            result.add(new StatusEntry(0, GeneChip.id("counter_storm"), String.format("%.1f +%.1f", ClientCardHudState.counterStormTicks / 20F, ClientCardHudState.counterStormDamage), 0xFFFFB347, false));
        }
        if (ClientCardHudState.comboFeverTicks > 0) {
            result.add(new StatusEntry(0, GeneChip.id("combo_fever"), String.format("FEVER %.1f", ClientCardHudState.comboFeverTicks / 20F), 0xFFFF6534, false));
        } else if (ClientCardHudState.comboWindowTicks > 0) {
            result.add(new StatusEntry(1, GeneChip.id("combo_fever"), ClientCardHudState.comboCount + "/3", 0xFFFF8060, false));
        }
        if (ClientCardHudState.photosynthesisStacks > 0 || ClientCardHudState.photosynthesisCharging) {
            int progress = ClientCardHudState.photosynthesisTimer * 100 / Math.max(1, ClientCardHudState.photosynthesisInterval);
            result.add(new StatusEntry(2, GeneChip.id("photosynthesis"), ClientCardHudState.photosynthesisStacks + "/" + ClientCardHudState.photosynthesisMaxStacks + " " + progress + "%", 0xFF8CEB72, false));
        }
        return result.stream().sorted(Comparator.comparingInt(StatusEntry::priority)).toList();
    }

    private static void drawIcon(GuiGraphics graphics, ResourceLocation texture, int x, int y) {
        graphics.blit(texture, x, y, 0, 0, ICON_SIZE, ICON_SIZE, ICON_SIZE, ICON_SIZE);
        graphics.renderOutline(x - 1, y - 1, ICON_SIZE + 2, ICON_SIZE + 2, 0xAAFFFFFF);
    }

    private static void renderFeedback(GuiGraphics graphics, Minecraft minecraft, int width, int height) {
        if (ClientCardHudState.feedbackTicks <= 0 || ClientCardHudState.feedbackType == null) return;
        Chip feedbackChip = switch (ClientCardHudState.feedbackType) {
            case ADRENAL_TRIGGERED -> ChipTypes.ADRENAL_GLAND_BURST.get();
            case GROWING_FERVOR_STAGE -> ChipTypes.GROWING_FERVOR.get();
            case LOCKSMITH_BONUS_LOOT -> ChipTypes.LOCKSMITH_INTUITION.get();
            default -> null;
        };
        if (feedbackChip != null) {
            drawIcon(graphics, feedbackChip.getTexture(), width - 69, height - 85);
        }
        String text = switch (ClientCardHudState.feedbackType) {
            case AMMO_RECYCLED -> "+" + ClientCardHudState.feedbackValue;
            case COUNTER_RELEASED -> "COUNTER +" + ClientCardHudState.feedbackValue;
            case COMBO_PROGRESS -> ClientCardHudState.feedbackValue + "/3";
            case COMBO_TRIGGERED -> "COMBO FEVER";
            case HEADSHOT -> "HEADSHOT";
            case ACTION_REJECTED -> "ACTION BLOCKED";
            case INFECTED_ITEM_BLOCKED ->
                    net.minecraft.network.chat.Component.translatable("gene_chip.feedback.infected_item_blocked").getString();
            default -> "";
        };
        if (!text.isEmpty()) {
            graphics.drawCenteredString(minecraft.font, text, width - 60, height - 72, 0xFFFFB347);
        }
    }

    @SubscribeEvent
    public static void renderLocksmithScreenIcon(ScreenEvent.Render.Post event) {
        Screen screen = event.getScreen();
        if (!(screen instanceof LockpickingScreen<?>)) {
            ClientCardHudState.clearLocksmithScreen();
            return;
        }
        if (!ClientCardHudState.shouldRenderLocksmithScreenIcon()) return;
        GuiGraphics graphics = event.getGuiGraphics();
        Chip chip = ChipTypes.LOCKSMITH_INTUITION.get();
        int x = graphics.guiWidth() - ICON_SIZE - 10;
        int y = 10;
        drawIcon(graphics, chip.getTexture(), x, y);
    }

    private static void renderFeverOverlay(GuiGraphics graphics, int width, int height) {
        if (ClientCardHudState.comboFeverTicks <= 0) return;
        int alpha = 35 + (int) (18 * Math.sin(ClientCardHudState.comboFeverTicks * 0.35));
        int red = (Mth.clamp(alpha, 20, 70) << 24) | 0x00FF3B16;
        int orange = (Mth.clamp(alpha / 2, 10, 35) << 24) | 0x00FF8A20;
        graphics.fill(0, 0, width, 5, red);
        graphics.fill(0, height - 5, width, height, red);
        graphics.fill(0, 5, 5, height - 5, orange);
        graphics.fill(width - 5, 5, width, height - 5, orange);
    }

    private record StatusEntry(int priority, ResourceLocation chip, String text, int color, boolean pulse) {
    }
}
