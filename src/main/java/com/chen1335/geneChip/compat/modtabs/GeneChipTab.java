package com.chen1335.geneChip.compat.modtabs;

import com.chen1335.geneChip.GeneChip;
import com.chen1335.geneChip.client.gui.screen.ChipConfigScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import vodmordia.modtabs.api.tabs_menu.ScreenRegistry;
import vodmordia.modtabs.api.tabs_menu.SimpleItemTab;
import vodmordia.modtabs.api.tabs_menu.TabConfiguration;
import vodmordia.modtabs.api.tabs_menu.TabRegistry;
import vodmordia.modtabs.api.tabs_menu.TabsMenu;

@EventBusSubscriber(modid = GeneChip.MODID, value = Dist.CLIENT)
public class GeneChipTab extends SimpleItemTab {

    private static final String[] SCREENS = {
            "net.minecraft.client.gui.screens.inventory.InventoryScreen"
    };

    public GeneChipTab() {
        super(new ItemStack(Items.STICK));
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
    public void initTabOnScreens() {
        ScreenRegistry.registerStandardScreens(SCREENS);
    }

    @Override
    public boolean isCurrentlyUsed(Screen screen) {
        return screen instanceof ChipConfigScreen;
    }

    @Override
    public Component getTooltip() {
        return Component.translatable("gene_chip.config");
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        TabRegistry.registerTab(GeneChipTab.class, TabConfiguration.withOrder(100));
        event.enqueueWork(() -> TabsMenu.register(new GeneChipTab()));
    }
}
