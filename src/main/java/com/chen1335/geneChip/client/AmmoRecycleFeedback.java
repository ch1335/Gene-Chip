package com.chen1335.geneChip.client;

import com.atsuishio.superbwarfare.config.client.DisplayConfig;
import com.atsuishio.superbwarfare.data.gun.GunData;
import com.atsuishio.superbwarfare.item.gun.GunItem;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * 「维持生计」回收子弹时的 HUD 反馈：在 Superb Warfare 的弹药数字上方生成一个向上飘动、逐渐淡出的 +N。
 * <p>
 * 锚点跟随 SW 的武器 HUD：{@code AmmoBarOverlay} 用 1.5 倍缩放把弹药数字画在原版坐标
 * {@code (hudX - 64, useBackpackAmmo ? hudY - 38 : hudY - 43)} 处（{@code hudX/hudY} 由
 * {@code DisplayConfig.WEAPON_HUD_X/Y_OFFSET} 相对屏幕右下角偏移），本类就贴在它正上方。
 * 没拿枪 / 关掉弹药 HUD / 观察者模式时退回旧的固定位置，保证特效始终可见。
 */
public final class AmmoRecycleFeedback {
    /** 单条特效存活时长（毫秒）。 */
    private static final long LIFETIME_MS = 900L;
    /** 存活期内总上浮高度（GUI 像素）。 */
    private static final float RISE = 12.0F;
    /** 文字颜色（不含 alpha 位），与 SW 弹药数字（{@code AmmoBarOverlay} 里硬编码的 0xFFFFFF）保持一致。 */
    private static final int COLOR = 0xFFFFFF;
    /** 兜底位置：距屏幕右下角的距离。 */
    private static final int FALLBACK_OFFSET_X = 60;
    private static final int FALLBACK_OFFSET_Y = 72;

    private record Entry(int value, long spawnMs, float sideJitter) {
    }

    /** 锚点：弹药数字的水平中心与顶边。 */
    private record Anchor(float centerX, float topY) {
    }

    private static final List<Entry> ENTRIES = new ArrayList<>();

    private AmmoRecycleFeedback() {
    }

    /**
     * 由 {@link com.chen1335.geneChip.network.CardFeedbackPacket} 的客户端 handler 调用。
     * 同一 tick 内多次触发会各占一条，用轻微随机横向偏移错开，避免完全重叠。
     */
    public static void push(int value) {
        if (value <= 0) return;
        float jitter = (float) ((Math.random() - 0.5D) * 10.0D);
        ENTRIES.add(new Entry(value, Util.getMillis(), jitter));
    }

    public static void render(GuiGraphics graphics, Minecraft minecraft, int screenWidth, int screenHeight) {
        if (ENTRIES.isEmpty()) return;
        LocalPlayer player = minecraft.player;
        if (player == null) {
            ENTRIES.clear();
            return;
        }

        Anchor anchor = resolveAnchor(minecraft, player, screenWidth, screenHeight);
        Font font = minecraft.font;
        long now = Util.getMillis();

        Iterator<Entry> it = ENTRIES.iterator();
        while (it.hasNext()) {
            Entry entry = it.next();
            long age = now - entry.spawnMs();
            if (age >= LIFETIME_MS) {
                it.remove();
                continue;
            }
            float t = (float) age / LIFETIME_MS;
            // 先快后慢地上浮；透明度线性衰减。
            // alpha 下限取 8 而不是 0：Font#adjustColor 会把 alpha<=3 的颜色强制改成不透明，取 0 会在最后一帧突然变成实心。
            float rise = RISE * (1.0F - (1.0F - t) * (1.0F - t));
            int alpha = Mth.clamp((int) (255.0F * (1.0F - t)), 8, 255);
            int color = alpha << 24 | COLOR;

            String text = Component.translatable("gene_chip.feedback.ammo_recycled", entry.value()).getString();
            graphics.drawCenteredString(font, text,
                    (int) (anchor.centerX() + entry.sideJitter()),
                    (int) (anchor.topY() - rise),
                    color);
        }
    }

    private static Anchor resolveAnchor(Minecraft minecraft, LocalPlayer player, int screenWidth, int screenHeight) {
        float fallbackX = screenWidth - FALLBACK_OFFSET_X;
        float fallbackY = screenHeight - FALLBACK_OFFSET_Y;

        ItemStack stack = player.getMainHandItem();
        if (!(stack.getItem() instanceof GunItem) || player.isSpectator()) {
            return new Anchor(fallbackX, fallbackY);
        }
        // SW 的客户端配置在极早期可能还没装配，取不到就退回兜底位置。
        if (DisplayConfig.AMMO_HUD == null || DisplayConfig.WEAPON_HUD_X_OFFSET == null || DisplayConfig.WEAPON_HUD_Y_OFFSET == null
                || !DisplayConfig.AMMO_HUD.get()) {
            return new Anchor(fallbackX, fallbackY);
        }

        int hudX = screenWidth + DisplayConfig.WEAPON_HUD_X_OFFSET.get();
        int hudY = screenHeight + DisplayConfig.WEAPON_HUD_Y_OFFSET.get();
        GunData data = GunData.from(stack);

        // 与 AmmoBarOverlay 的绘制保持一致：pose 缩放 1.5，
        // 左边缘 1.5 * (hudX/1.5 - 42.666668) = hudX - 64，顶边 1.5 * ((hudY + 5 - 48)/1.5) = hudY - 43。
        float ammoLeft = hudX - 64.0F;
        float ammoTop = data.useBackpackAmmo() ? hudY - 38.0F : hudY - 43.0F;
        float ammoWidth = minecraft.font.width(currentAmmoText(data, player)) * 1.5F;
        return new Anchor(ammoLeft + ammoWidth / 2.0F, ammoTop - 11.0F);
    }

    /** 复刻 {@code AmmoBarOverlay#getGunAmmoString}，仅用于把 +N 对准那个数字的水平中心。 */
    private static String currentAmmoText(GunData data, LocalPlayer player) {
        if (data.meleeOnly() || data.useBackpackAmmo() && data.hasInfiniteBackupAmmo(player)) {
            return "\u221e";
        }
        if (data.useBackpackAmmo()) {
            return Integer.toString(data.countBackupAmmo(player) - data.virtualAmmo.get());
        }
        return Integer.toString(data.ammo.get());
    }
}
