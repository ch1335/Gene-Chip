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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
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

    private static final int FILTER_BAR_HEIGHT = 25;
    private static final int CHIP_GRID_Y = 22 + FILTER_BAR_HEIGHT;

    public ChipConfigScreen() {
        super(Component.empty());
    }

    public final Map<ChipType, Map<Chip, ChipInstance<?>>> availableChips = new HashMap<>();

    public NonNullList<ChipSlot> slots = NonNullList.create();

    public ChipFilter chipFilter = new ChipFilter();

    public List<ChipWidget> chipWidgets = new ArrayList<>();

    public List<EquippedChipWidget> equippedChipWidgets = new ArrayList<>();

    public int hoveredSlot = -1;

    private EditBox searchBox;
    private DropdownButton<Boolean> unlockedDropdown;
    private DropdownButton<ChipType> typeDropdown;

    @Override
    protected void init() {
        super.init();
        Vec2 windowScale = GuiUtil.getWindowScale();

        xScale = windowScale.x * 2;
        yScale = windowScale.y * 2;

        chipWidgets.clear();
        equippedChipWidgets.clear();

        setupFilterBar();
        rebuildChipWidgets();

        getSlots().values().forEach(chipSlot -> {
            EquippedChipWidget equippedChipWidget = new EquippedChipWidget(chipSlot, chipSlot.index(), this);
            addWidget(equippedChipWidget);
            equippedChipWidgets.add(equippedChipWidget);
        });
        availableChips.clear();
        availableChips.putAll(GeneChipClient.getPlayerChipData().getChipInfos().getChips());

    }

    private void setupFilterBar() {
        int filterY = (int) (2 * yScale * 2);
        int searchX = (int) (170 * xScale * 2);
        int searchW = (int) (80 * xScale*2);
        int searchH = (int) (16 * yScale*2);

        searchBox = new EditBox(Minecraft.getInstance().font,
                searchX, filterY, searchW, searchH,
                Component.translatable("gene_chip.filter.search_hint"));
        searchBox.setHint(Component.translatable("gene_chip.filter.search_hint"));
        searchBox.setValue(chipFilter.getSearchText());
        searchBox.setResponder(text -> {
            chipFilter.setSearchText(text);
            rebuildChipWidgets();
        });
        addRenderableWidget(searchBox);

        int unlockedX = searchX + searchW + 4;
        unlockedDropdown = new DropdownButton<>(
                unlockedX, filterY, (int) (60 * xScale*2), (int) (16 * yScale*2),
                List.of(Boolean.FALSE, Boolean.TRUE),
                chipFilter.isShowAll(),
                on -> on ? Component.translatable("gene_chip.filter.all")
                        : Component.translatable("gene_chip.filter.unlocked"),
                value -> {
                    chipFilter.setShowAll(value);
                    rebuildChipWidgets();
                });
        addWidget(unlockedDropdown);

        int typeX = unlockedX + (int) (60 * xScale*2) + 4;
        List<ChipType> typeOptions = new ArrayList<>();
        typeOptions.add(null);
        typeOptions.addAll(Arrays.asList(ChipType.values()));
        typeDropdown = new DropdownButton<>(
                typeX, filterY, (int) (60 * xScale*2), (int) (16 * yScale*2),
                typeOptions,
                chipFilter.getTypeFilter(),
                type -> type == null
                        ? Component.translatable("gene_chip.filter.type.all")
                        : Component.translatable("gene_chip.chip_type." + type.getSerializedName()),
                value -> {
                    chipFilter.setTypeFilter(value);
                    rebuildChipWidgets();
                });
        addWidget(typeDropdown);
    }

    public void rebuildChipWidgets() {
        for (ChipWidget chipWidget : chipWidgets) {
            removeWidget(chipWidget);
        }
        chipWidgets.clear();

        EnumMap<ChipType, List<Chip>> typeListEnumMap = new EnumMap<>(ChipType.class);
        for (Map.Entry<ResourceKey<Chip>, Chip> entry : RegisterTypes.CHIP.entrySet()) {
            typeListEnumMap.computeIfAbsent(entry.getValue().getType(), k -> new ArrayList<>()).add(entry.getValue());
        }

        List<Chip> availableChips1 = chipFilter.getAvailableChips(GeneChipClient.getClientPlayer(), typeListEnumMap);

        int chipIndex = 0;
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
                chipWidget.setY((int) ((int) ((CHIP_GRID_Y + ((int) (i / 4)) * 85) * yScale * s) + scrollY));
            }
            chipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
            i++;
        }

        for (EquippedChipWidget equippedChipWidget : equippedChipWidgets) {
            equippedChipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        if (unlockedDropdown != null) {
            unlockedDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (typeDropdown != null) {
            typeDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.enableBlend();

        float s = 2;
        GuiUtil.drawColorWithSize(guiGraphics, (int) (10 * xScale * s), (int) (20 * yScale * s), (int) (150 * xScale * s), (int) (240 * yScale * s), FastColor.ARGB32.color(150, 0, 0, 0), 1);

        GuiUtil.drawColorWithSize(guiGraphics,
                (int) (170 * xScale * s), (int) (0 * yScale * s),
                (int) (310 * xScale * s), (int) (FILTER_BAR_HEIGHT * yScale * s),
                FastColor.ARGB32.color(150, 0, 0, 0), 1);

        hoveredSlot = -1;
        for (int i = 0; i < getSlots().size(); i++) {
            renderSlotBox(guiGraphics, mouseX, mouseY, i, partialTick);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        boolean collapsed = false;
        if (unlockedDropdown != null && unlockedDropdown.isExpanded() && !unlockedDropdown.isMouseOver(mouseX, mouseY)) {
            unlockedDropdown.setExpanded(false);
            collapsed = true;
        }
        if (typeDropdown != null && typeDropdown.isExpanded() && !typeDropdown.isMouseOver(mouseX, mouseY)) {
            typeDropdown.setExpanded(false);
            collapsed = true;
        }
        if (collapsed) {
            return true;
        }
        if (unlockedDropdown != null && unlockedDropdown.isExpanded()) {
            if (unlockedDropdown.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        if (typeDropdown != null && typeDropdown.isExpanded()) {
            if (typeDropdown.mouseClicked(mouseX, mouseY, button)) {
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private double scrollY = 0;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (unlockedDropdown != null && unlockedDropdown.isExpanded()) return false;
        if (typeDropdown != null && typeDropdown.isExpanded()) return false;
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
