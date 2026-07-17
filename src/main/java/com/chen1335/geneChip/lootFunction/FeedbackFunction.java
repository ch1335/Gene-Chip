package com.chen1335.geneChip.lootFunction;

import com.chen1335.geneChip.network.WorldItemFeedbackPacket;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.SimpleMapCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

/**
 * 野外猎手额外掉落触发时调用：在动物尸体位置向击杀者显示对应资源（肉 / 皮革）的 icon 反馈。
 * 该函数只在 {@link com.chen1335.geneChip.lootInject.DTLootInjector} 注入的额外掉落条目实际掷出时执行，
 * 因此普通掉落不会触发反馈。
 */
public class FeedbackFunction implements LootItemFunction {
    public static FeedbackFunction INSTANCE = new FeedbackFunction();
    public static final MapCodec<FeedbackFunction> CODEC = SimpleMapCodec.unit(INSTANCE);
    public static final LootItemFunctionType<FeedbackFunction> FEEDBACK_FUNCTION_TYPE = new LootItemFunctionType<>(CODEC);

    @Override
    public @NotNull LootItemFunctionType<? extends LootItemFunction> getType() {
        return FEEDBACK_FUNCTION_TYPE;
    }

    @Override
    public ItemStack apply(ItemStack itemStack, LootContext lootContext) {
        if (itemStack.isEmpty()) {
            return itemStack;
        }
        // 击杀者：额外掉落 icon 只发给触发卡牌的玩家
        Entity attacker = lootContext.getParamOrNull(LootContextParams.ATTACKING_ENTITY);
        if (!(attacker instanceof ServerPlayer player)) {
            return itemStack;
        }
        // 反馈绑定到动物尸体位置
        Vec3 origin = lootContext.getParamOrNull(LootContextParams.ORIGIN);
        Entity victim = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        double x;
        double y;
        double z;
        if (origin != null) {
            x = origin.x;
            y = origin.y + 0.6;
            z = origin.z;
        } else if (victim != null) {
            x = victim.getX();
            y = victim.getY() + victim.getBbHeight() * 0.5;
            z = victim.getZ();
        } else {
            return itemStack;
        }
        ItemStack icon = itemStack.copyWithCount(1);
        PacketDistributor.sendToPlayer(player, new WorldItemFeedbackPacket(x, y, z, icon));
        return itemStack;
    }

    public static LootItemFunction.Builder builder() {
        return () -> INSTANCE;
    }
}
