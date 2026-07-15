package com.chen1335.geneChip.compat.modtabs;

import com.chen1335.geneChip.API.object.GCItems;
import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.gui.screen.ChipConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfig;
import vodmordia.modtabs.api.tabs_menu.TabConfiguration;
import vodmordia.modtabs.api.tabs_menu.TabRegistry;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

@TabConfig(configKey = "geneChipTab", defaultOrder = 100)
@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class GeneChipTab extends SimpleItemTab {

    public GeneChipTab() {
        super(() -> new ItemStack(GCItems.GENE_ENHANCER.get()));
    }

    @Override
    public void openTargetScreen(Player player) {
        Minecraft.getInstance().setScreen(new ChipConfigScreen());
    }

    @Override
    public boolean isEnabled(Player player) {
        return true;
    }

    @Override
    public boolean isCurrentlyUsed(Screen screen) {
        return screen instanceof ChipConfigScreen;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("gene_chip.config");
    }

    @Override
    public void initTabOnScreens() {
        ScreenRegistry.registerStandardScreens(InventoryScreen.class);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(GeneChipTab::register);
    }

    private static void register() {
        TabRegistry.registerTab(GeneChipTab.class, TabConfiguration.withOrder(100));
        TabsMenu.register(new GeneChipTab());
        TabsMenu.finalizePendingRegistrations();
    }
}
