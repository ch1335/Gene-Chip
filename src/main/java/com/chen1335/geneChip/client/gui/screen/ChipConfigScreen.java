package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.chen1335.geneChip.network.SetSlotChipPacket;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec2;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class ChipConfigScreen extends Screen {
    private float xScale;
    private float yScale;

    public ChipConfigScreen() {
        super(Component.empty());
    }

    public final Map<ChipType, Map<Chip, ChipInstance<?>>> availableChips = new HashMap<>();

    public NonNullList<ChipSlot> slots = NonNullList.create();

    public ChipFilter chipFilter = new ChipFilter();

    public List<ChipWidget> chipWidgets = new ArrayList<>();

    public List<EquippedChipWidget> equippedChipWidgets = new ArrayList<>();

    public int hoveredSlot = -1;

    @Override
    protected void init() {
        super.init();
        chipWidgets.clear();
        equippedChipWidgets.clear();
        Vec2 windowScale = GuiUtil.getWindowScale();

        xScale = windowScale.x * 2;
        yScale = windowScale.y * 2;
        int chipIndex = 0;

        EnumMap<ChipType, List<Chip>> typeListEnumMap = new EnumMap<>(ChipType.class);


        for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
            typeListEnumMap.computeIfAbsent(entry.getValue().getType(), k -> new ArrayList<>()).add(entry.getValue());
        }

        List<Chip> availableChips1 = chipFilter.getAvailableChips(GeneChipClient.getClientPlayer(), typeListEnumMap);

        for (Chip chip : availableChips1) {
            ChipInstance<Chip> playerChip = GeneChipClient.getPlayerChip(chip);
            ChipWidget chipWidget;
            if (playerChip != null) {
                chipWidget = new ChipWidget(playerChip, this);
                chipWidget.unlocked = true;
            } else {
                chipWidget = new ChipWidget(new ChipInstance<>(chip, 0, 0), this);
                chipWidget.unlocked = false;
            }
            chipWidget.index = chipIndex;
            addWidget(chipWidget);
            chipWidgets.add(chipWidget);
            chipIndex++;
        }


        getSlots().values().forEach(chipSlot -> {
            EquippedChipWidget equippedChipWidget = new EquippedChipWidget(chipSlot, chipSlot.index(), this);
            addWidget(equippedChipWidget);
            equippedChipWidgets.add(equippedChipWidget);
        });
        availableChips.clear();
        availableChips.putAll(GeneChipClient.getPlayerChipData().getChipInfos().getChips());

    }

    public IntObjectMap<ChipSlot> getSlots() {
        return GeneChipClient.getPlayerChipData().getSlotInfos().getSlots();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        float s = 2;
        int i = 0;
        for (ChipWidget chipWidget : chipWidgets) {
            if (!chipWidget.isFocused()) {
                chipWidget.setX((int) ((170 + (i % 4) * 60) * xScale * s));
                chipWidget.setY((int) ((int) ((22 + ((int) (i / 4)) * 85) * yScale * s) + scrollY));
            }
            chipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            i++;
        }

        for (EquippedChipWidget equippedChipWidget : equippedChipWidgets) {
            equippedChipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();

        float s = 2;
        GuiUtil.drawColorWithSize(guiGraphics, (int) (10 * xScale * s), (int) (20 * yScale* s), (int) (150 * xScale* s), (int) (240 * yScale* s), FastColor.ARGB32.color(150, 0, 0, 0), 1);

        hoveredSlot = -1;
        for (int i = 0; i < getSlots().size(); i++) {
            renderSlotBox(guiGraphics, mouseX, mouseY, i, partialTick);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);

    }

    private double scrollY = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        this.scrollY = Math.min(this.scrollY + scrollY * 50, 0);
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    public void renderSlotBox(GuiGraphics guiGraphics, int mouseX, int mouseY, int index, float partialTick) {
        int ySize = 18;
        int y1 = ySize + 3;
        int y = 40;
        float s = 2;
        int color = FastColor.ARGB32.color(150, 0, 0, 0);
        if (mouseX >= 10 * xScale * s && mouseX <= 160 * xScale * s && mouseY >= (20 + index * y1 + y) * yScale * s && mouseY <= (20 + ySize + index * y1 + y) * yScale * s) {
            hoveredSlot = index;
        }

        GuiUtil.drawColorWithSize(guiGraphics, (int) (10 * xScale * s), (int) (((20 + index * y1 + y) * yScale) * s), (int) (150 * xScale) * s, (int) (ySize * yScale) * s, color, 1);

    }

    public void setSlotChip(@Nullable ChipInstance<?> chipInstance, int slot) {
        EquippedChipWidget equippedChipWidget = this.equippedChipWidgets.get(slot);
        IntObjectMap<ChipSlot> chipSlots = this.getSlots();

        if (chipInstance == null) {
            ChipSlot chipSlot = new ChipSlot(Optional.empty(), slot);
            equippedChipWidget.setChipSlot(chipSlot);
            if (slot < chipSlots.size()) {
                chipSlots.put(slot, chipSlot);
                PacketDistributor.sendToServer(new SetSlotChipPacket(Optional.empty(), slot));
            }
        } else {
            ChipSlot chipSlot = new ChipSlot(Optional.of(chipInstance), slot);
            equippedChipWidget.setChipSlot(chipSlot);
            if (slot < chipSlots.size()) {
                chipSlots.put(slot, chipSlot);
                PacketDistributor.sendToServer(new SetSlotChipPacket(Optional.of(chipInstance.getChip()), slot));
            }
        }

        GeneChipClient.getPlayerChipData().getSlotInfos().bakeCurrent();
    }
}
