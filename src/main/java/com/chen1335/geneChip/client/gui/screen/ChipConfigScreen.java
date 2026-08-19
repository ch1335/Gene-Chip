package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.chen1335.geneChip.network.SetSlotChipPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.FastColor;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class ChipConfigScreen extends Screen {
    private static final int DESIGN_WIDTH = 480;
    private static final int DESIGN_HEIGHT = 270;
    private static final float MIN_SCALE = 0.625F;
    private static final float CARD_SCALE = 1.25F;

    private static final int FILTER_Y = 4;
    private static final int FILTER_HEIGHT = 16;
    private static final int CHIP_GRID_X = 170;
    private static final int CHIP_GRID_Y = 47;
    private static final int CHIP_COLUMNS = 4;
    private static final int CHIP_COLUMN_PITCH = 70;
    private static final int CHIP_ROW_PITCH = 105;
    private static final int CARD_WIDTH = 48;
    private static final int CARD_HEIGHT = 78;
    private static final double SCROLL_STEP = 50.0D;

    private LayoutMetrics layout;
    private double scrollDesignY;

    public final Map<ChipType, Map<Chip, ChipInstance<?>>> availableChips = new HashMap<>();
    public final ChipFilter chipFilter = new ChipFilter();
    public final List<ChipWidget> chipWidgets = new ArrayList<>();
    public final List<EquippedChipWidget> equippedChipWidgets = new ArrayList<>();

    public int hoveredSlot = -1;

    private EditBox searchBox;
    private DropdownButton<Boolean> unlockedDropdown;
    private DropdownButton<ChipType> typeDropdown;

    public ChipConfigScreen() {
        super(Component.empty());
    }

    @Override
    protected void init() {
        super.init();
        layout = new LayoutMetrics(this.width, this.height);

        chipWidgets.clear();
        equippedChipWidgets.clear();
        hoveredSlot = -1;

        setupFilterBar();
        rebuildChipWidgets();

        getSlots().values().forEach(chipSlot -> {
            EquippedChipWidget equippedChipWidget = new EquippedChipWidget(chipSlot, chipSlot.index(), this, layout);
            addWidget(equippedChipWidget);
            equippedChipWidgets.add(equippedChipWidget);
        });
        availableChips.clear();
        availableChips.putAll(GeneChipClient.getPlayerChipData().getChipInfos().getChips());
        clampScrollY();
    }

    private void setupFilterBar() {
        LayoutRect searchRect = layout.rect(170, FILTER_Y, 80, FILTER_HEIGHT);
        searchBox = new EditBox(Minecraft.getInstance().font,
                searchRect.left(), searchRect.top(), searchRect.width(), searchRect.height(),
                Component.translatable("gene_chip.filter.search_hint"));
        searchBox.setHint(Component.translatable("gene_chip.filter.search_hint"));
        searchBox.setValue(chipFilter.getSearchText());
        searchBox.setResponder(text -> {
            chipFilter.setSearchText(text);
            rebuildChipWidgets();
        });
        addRenderableWidget(searchBox);

        LayoutRect unlockedRect = layout.rect(254, FILTER_Y, 60, FILTER_HEIGHT);
        unlockedDropdown = new DropdownButton<>(
                unlockedRect.left(), unlockedRect.top(), unlockedRect.width(), unlockedRect.height(), layout.scale(),
                List.of(Boolean.FALSE, Boolean.TRUE),
                chipFilter.isShowAll(),
                on -> on ? Component.translatable("gene_chip.filter.all")
                        : Component.translatable("gene_chip.filter.unlocked"),
                value -> {
                    chipFilter.setShowAll(value);
                    rebuildChipWidgets();
                });
        addWidget(unlockedDropdown);

        List<ChipType> typeOptions = new ArrayList<>();
        typeOptions.add(null);
        typeOptions.addAll(Arrays.asList(ChipType.values()));
        LayoutRect typeRect = layout.rect(318, FILTER_Y, 60, FILTER_HEIGHT);
        typeDropdown = new DropdownButton<>(
                typeRect.left(), typeRect.top(), typeRect.width(), typeRect.height(), layout.scale(),
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
            typeListEnumMap.computeIfAbsent(entry.getValue().getType(), key -> new ArrayList<>()).add(entry.getValue());
        }

        List<Chip> available = chipFilter.getAvailableChips(GeneChipClient.getClientPlayer(), typeListEnumMap);
        for (int index = 0; index < available.size(); index++) {
            Chip chip = available.get(index);
            ChipInstance<Chip> playerChip = GeneChipClient.getPlayerChip(chip);
            ChipWidget chipWidget = playerChip != null
                    ? new ChipWidget(playerChip, this, layout)
                    : new ChipWidget(new ChipInstance<>(chip, 0, 1), this, layout);
            chipWidget.unlocked = playerChip != null;
            chipWidget.index = index;
            addWidget(chipWidget);
            chipWidgets.add(chipWidget);
        }
        clampScrollY();
    }

    public IntObjectMap<ChipSlot> getSlots() {
        return GeneChipClient.getPlayerChipData().getSlotInfos().getSlots();
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

        for (EquippedChipWidget equippedChipWidget : equippedChipWidgets) {
            equippedChipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        renderEquippedTypeSummary(guiGraphics);

        if (unlockedDropdown != null) {
            unlockedDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (typeDropdown != null) {
            typeDropdown.render(guiGraphics, mouseX, mouseY, partialTick);
        }

        LayoutRect viewport = layout.cardPanelRect();
        guiGraphics.enableScissor(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
        try {
            for (int index = 0; index < chipWidgets.size(); index++) {
                ChipWidget chipWidget = chipWidgets.get(index);
                if (chipWidget.isFocused()) continue;

                LayoutRect cardRect = layout.cardRect(index, scrollDesignY);
                chipWidget.setPosition(cardRect.left(), cardRect.top());
                renderChipWidget(guiGraphics, chipWidget, mouseX, mouseY, partialTick);
            }
        } finally {
            guiGraphics.disableScissor();
        }
        for (ChipWidget chipWidget : chipWidgets) {
            if (chipWidget.isFocused()) {
                renderChipWidget(guiGraphics, chipWidget, mouseX, mouseY, partialTick);
            }
        }
        if (viewport.contains(mouseX, mouseY)) {
            for (ChipWidget chipWidget : chipWidgets) {
                chipWidget.renderHoverTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    private static void renderChipWidget(GuiGraphics guiGraphics, ChipWidget chipWidget,
                                         int mouseX, int mouseY, float partialTick) {
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(0, 0, -30);
        chipWidget.render(guiGraphics, mouseX, mouseY, partialTick);
        pose.popPose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();

        drawRect(guiGraphics, layout.rect(10, 20, 150, 242),
                FastColor.ARGB32.color(100, 160, 160, 160), 1);
        drawRect(guiGraphics, layout.rect(10, 0, 470, 20),
                FastColor.ARGB32.color(100, 160, 160, 160), 0);
        renderCardPanel(guiGraphics);

        hoveredSlot = slotAt(mouseX, mouseY);
        for (int index = 0; index < getSlots().size(); index++) {
            drawRect(guiGraphics, layout.slotRect(index), FastColor.ARGB32.color(150, 0, 0, 0), 1);
        }
    }

    private void renderCardPanel(GuiGraphics guiGraphics) {
        LayoutRect borderRect = layout.rect(160, 20, 319, 242);
        LayoutRect fillRect = layout.cardPanelRect();
        drawRect(guiGraphics, borderRect, FastColor.ARGB32.color(180, 200, 200, 200), -42);
        drawRect(guiGraphics, fillRect, FastColor.ARGB32.color(110, 30, 30, 30), -41);
    }

    private static void drawRect(GuiGraphics guiGraphics, LayoutRect rect, int color, int z) {
        GuiUtil.drawColorWithSize(guiGraphics, rect.left(), rect.top(), rect.width(), rect.height(), color, z);
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
        if (unlockedDropdown != null && unlockedDropdown.isExpanded()
                && unlockedDropdown.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        if (typeDropdown != null && typeDropdown.isExpanded()
                && typeDropdown.mouseClicked(mouseX, mouseY, button)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        if (this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            this.onClose();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (unlockedDropdown != null && unlockedDropdown.isExpanded()) return false;
        if (typeDropdown != null && typeDropdown.isExpanded()) return false;
        if (!layout.cardPanelRect().contains(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        double oldScroll = scrollDesignY;
        scrollDesignY = Math.max(computeMinScrollY(), Math.min(scrollDesignY + scrollY * SCROLL_STEP, 0));
        return oldScroll != scrollDesignY || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private double computeMinScrollY() {
        int rows = (chipWidgets.size() + CHIP_COLUMNS - 1) / CHIP_COLUMNS;
        if (rows == 0) return 0;
        double contentBottom = CHIP_GRID_Y + (rows - 1) * CHIP_ROW_PITCH + CARD_HEIGHT * CARD_SCALE;
        double viewportBottom = 260;
        return Math.min(0, viewportBottom - 10 - contentBottom);
    }

    private void clampScrollY() {
        scrollDesignY = Math.max(computeMinScrollY(), Math.min(scrollDesignY, 0));
    }

    public int slotAt(double mouseX, double mouseY) {
        for (int index = 0; index < getSlots().size(); index++) {
            if (layout.slotRect(index).contains(mouseX, mouseY)) {
                return index;
            }
        }
        return -1;
    }

    private void renderEquippedTypeSummary(GuiGraphics guiGraphics) {
        EnumMap<ChipType, Integer> counts = new EnumMap<>(ChipType.class);
        for (ChipType type : ChipType.values()) {
            counts.put(type, 0);
        }
        IntObjectMap<ChipSlot> chipSlots = getSlots();
        for (ChipSlot slot : chipSlots.values()) {
            slot.instance().ifPresent(instance -> counts.merge(instance.getChip().getType(), 1, Integer::sum));
        }

        int summaryY = 60 + Math.max(0, chipSlots.size() - 1) * 21 + 18 + 8;
        Minecraft minecraft = Minecraft.getInstance();
        int visibleIndex = 0;
        for (ChipType type : ChipType.values()) {
            int count = counts.getOrDefault(type, 0);
            if (count <= 0) continue;

            float iconX = layout.designX(10 + visibleIndex * 30);
            float iconY = layout.designY(summaryY);
            float scale = layout.scale();
            GuiUtil.drawTextureWithSize(type.getSmallCrystalIcon(), guiGraphics,
                    iconX, iconY, 8 * scale, 8 * scale,
                    2, 2, 4, 4, 8, 8, 5);

            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(layout.designX(19 + visibleIndex * 30), layout.designY(summaryY + 1), 1);
            pose.scale(scale, scale, 1);
            guiGraphics.drawString(minecraft.font, Component.literal(Integer.toString(count)), 0, 0, 0xFFFFFFFF, false);
            pose.popPose();
            visibleIndex++;
        }
    }

    public void setSlotChip(@Nullable ChipInstance<?> chipInstance, int slot) {
        EquippedChipWidget equippedChipWidget = this.equippedChipWidgets.get(slot);
        IntObjectMap<ChipSlot> chipSlots = this.getSlots();

        ChipSlot chipSlot = new ChipSlot(Optional.ofNullable(chipInstance), slot);
        equippedChipWidget.setChipSlot(chipSlot);
        if (slot < chipSlots.size()) {
            chipSlots.put(slot, chipSlot);
            PacketDistributor.sendToServer(new SetSlotChipPacket(
                    Optional.ofNullable(chipInstance).map(ChipInstance::getChip), slot));
        }
        GeneChipClient.getPlayerChipData().getSlotInfos().bakeCurrent();
    }

    static final class LayoutMetrics {
        private final float scale;
        private final int originX;
        private final int originY;

        LayoutMetrics(int screenWidth, int screenHeight) {
            float fitScale = Math.min(screenWidth / (float) DESIGN_WIDTH, screenHeight / (float) DESIGN_HEIGHT);
            this.scale = Math.max(MIN_SCALE, fitScale);
            int scaledWidth = Math.round(DESIGN_WIDTH * scale);
            int scaledHeight = Math.round(DESIGN_HEIGHT * scale);
            this.originX = scaledWidth <= screenWidth ? (screenWidth - scaledWidth) / 2 : 0;
            this.originY = scaledHeight <= screenHeight ? (screenHeight - scaledHeight) / 2 : 0;
        }

        float scale() {
            return scale;
        }

        float cardScale() {
            return scale * CARD_SCALE;
        }

        float designX(double designX) {
            return originX + (float) designX * scale;
        }

        float designY(double designY) {
            return originY + (float) designY * scale;
        }

        int length(double designLength) {
            return Math.max(1, Math.round((float) designLength * scale));
        }

        LayoutRect rect(double x, double y, double width, double height) {
            int left = originX + Math.round((float) x * scale);
            int top = originY + Math.round((float) y * scale);
            int right = originX + Math.round((float) (x + width) * scale);
            int bottom = originY + Math.round((float) (y + height) * scale);
            return new LayoutRect(left, top, right, bottom);
        }

        LayoutRect slotRect(int index) {
            return rect(10, 60 + index * 21, 150, 18);
        }

        LayoutRect cardRect(int index, double scrollY) {
            return rect(CHIP_GRID_X + (index % CHIP_COLUMNS) * CHIP_COLUMN_PITCH,
                    CHIP_GRID_Y + (index / CHIP_COLUMNS) * CHIP_ROW_PITCH + scrollY,
                    CARD_WIDTH * CARD_SCALE, CARD_HEIGHT * CARD_SCALE);
        }

        LayoutRect cardPanelRect() {
            return rect(162, 22, 315, 238);
        }
    }

    record LayoutRect(int left, int top, int right, int bottom) {
        int width() {
            return right - left;
        }

        int height() {
            return bottom - top;
        }

        boolean contains(double x, double y) {
            return x >= left && x <= right && y >= top && y <= bottom;
        }
    }
}
