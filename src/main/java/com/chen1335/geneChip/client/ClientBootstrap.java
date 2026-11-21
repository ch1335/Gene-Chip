package com.chen1335.geneChip.client;

import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;

/**
 * 客户端初始化入口。
 * <p>
 * 这个类里全是客户端专用逻辑（Cloth Config 界面、player animation 注册），
 * 只能由公共代码在 {@code dist == CLIENT} 的判断分支里调用——这样专用服务器不会加载它，
 * 也就不会碰到被剔除的客户端类。
 */
public final class ClientBootstrap {
    private ClientBootstrap() {
    }

    public static void init(ModContainer modContainer) {
        GeneChipClient.init();
        if (ModList.get().isLoaded("cloth_config")) {
            com.chen1335.geneChip.client.config.ClothConfig.build(modContainer);
        }
    }
}
