package com.chen1335.geneChip.compat.hunger;

import com.wu_meng.hungerreworkedreforged.common.attachment.PlayerStomach;
import com.wu_meng.hungerreworkedreforged.common.init.HRRAttachmentTypes;
import net.minecraft.world.entity.player.Player;

/**
 * 与 Hunger Reworked Reforged 的胃内容对接。
 * <p>
 * HRR 会把普通食物塞进「胃」（attachments 的 {@link PlayerStomach}），而不是直接加原版饥饿条，
 * 所以「这次吃进去多少点」要看 {@link PlayerStomach#getTotalFood()}。
 * 没装 HRR 时方法内部捕获 {@link Throwable} 并返回 -1。
 */
public final class HrrStomachCompat {
    private HrrStomachCompat() {
    }

    /** 当前胃里存的食物总量；HRR 不可用或读取失败时返回 -1。 */
    public static int totalFood(Player player) {
        try {
            PlayerStomach stomach = player.getData(HRRAttachmentTypes.PLAYER_STOMACH.get());
            return stomach == null ? -1 : stomach.getTotalFood();
        } catch (Throwable throwable) {
            return -1;
        }
    }
}
