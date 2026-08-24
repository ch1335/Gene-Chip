package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.API.object.RegisterTypes;
import com.chen1335.geneChip.chip.Chip;
import com.chen1335.geneChip.chip.ChipInstance;
import com.chen1335.geneChip.chip.ChipProgression;
import com.chen1335.geneChip.chip.ChipSlot;
import com.chen1335.geneChip.chip.ChipType;
import com.chen1335.geneChip.client.GeneChipClient;
import com.chen1335.geneChip.client.gui.GuiUtil;
import com.chen1335.geneChip.config.GameplayConfig;
import com.chen1335.geneChip.network.SetSlotChipPacket;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import io.netty.util.collection.IntObjectMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
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
    private static final float CARD_SCALE = 1.15F;

    private static final int FILTER_Y = 4;
    private static final int FILTER_HEIGHT = 16;
    private static final int CHIP_GRID_X = 115;
    private static final int CHIP_GRID_Y = 24;
    private static final int CHIP_COLUMNS = 4;
    private static final int CHIP_COLUMN_PITCH = 58;
    private static final int CHIP_ROW_PITCH = 96;
    private static final int CARD_WIDTH = 48;
    private static final int CARD_HEIGHT = 78;
    private static final double SCROLL_STEP = 50.0D;

    private LayoutMetrics layout;
    private double scrollDesignY;
    private boolean draggingScrollBar;
    private double scrollBarGrabOffset;

    public final Map<ChipType, Map<Chip, ChipInstance<?>>> availableChips = new HashMap<>();
    public final ChipFilter chipFilter = new ChipFilter();
    public final List<ChipWidget> chipWidgets = new ArrayList<>();
    public final List<EquippedChipWidget> equippedChipWidgets = new ArrayList<>();

    public int hoveredSlot = -1;

    private EditBox searchBox;
    private DropdownButton<Boolean> unlockedDropdown;
    private DropdownButton<ChipType> typeDropdown;
    private DetailActionButton equipButton;
    private ChipInstance<?> selectedChip;

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
        setupEquipButton();
        rebuildChipWidgets();

        IntObjectMap<ChipSlot> slots = getSlots();
        for (int index = 0; index < slots.size(); index++) {
            ChipSlot chipSlot = slots.get(index);
            if (chipSlot == null) continue;
            EquippedChipWidget equippedChipWidget = new EquippedChipWidget(chipSlot, index, this, layout);
            addWidget(equippedChipWidget);
            equippedChipWidgets.add(equippedChipWidget);
        }
        availableChips.clear();
        availableChips.putAll(GeneChipClient.getPlayerChipData().getChipInfos().getChips());
        clampScrollY();
    }

    private void setupFilterBar() {
        LayoutRect searchRect = layout.rect(115, FILTER_Y, 80, FILTER_HEIGHT);
        searchBox = new EditBox(Minecraft.getInstance().font,
                searchRect.left(    ), searchRect.top(), searchRect.width(), searchRect.height(),
                Component.translatable("gene_chip.filter.search_hint"));
        searchBox.setHint(Component.translatable("gene_chip.filter.search_hint"));
        searchBox.setValue(chipFilter.getSearchText());
        searchBox.setResponder(text -> {
            chipFilter.setSearchText(text);
            rebuildChipWidgets();
        });
        addRenderableWidget(searchBox);

        LayoutRect unlockedRect = layout.rect(199, FILTER_Y, 54, FILTER_HEIGHT);
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
        LayoutRect typeRect = layout.rect(257, FILTER_Y, 54, FILTER_HEIGHT);
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

    private void setupEquipButton() {
        LayoutRect rect = layout.equipButtonRect();
        equipButton = new DetailActionButton(rect, layout.scale(), this::equipSelectedChip,
                Component.translatable("gene_chip.details.equip"));
        equipButton.visible = selectedChip != null;
        equipButton.active = selectedChip != null;
        addWidget(equipButton);
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

        LayoutRect viewport = layout.cardViewportRect();
        guiGraphics.enableScissor(viewport.left(), viewport.top(), viewport.right(), viewport.bottom());
        try {
            for (int index = 0; index < chipWidgets.size(); index++) {
                ChipWidget chipWidget = chipWidgets.get(index);
                if (chipWidget.isDraggingCard()) continue;

                LayoutRect cardRect = layout.cardRect(index, scrollDesignY);
                chipWidget.setPosition(cardRect.left(), cardRect.top());
                renderChipWidget(guiGraphics, chipWidget, mouseX, mouseY, partialTick);
            }
        } finally {
            guiGraphics.disableScissor();
        }
        for (ChipWidget chipWidget : chipWidgets) {
            if (chipWidget.isDraggingCard()) {
                renderChipWidget(guiGraphics, chipWidget, mouseX, mouseY, partialTick);
            }
        }
        renderScrollBar(guiGraphics, mouseX, mouseY);
        renderSelectedChipDetails(guiGraphics);
        if (equipButton != null) {
            updateEquipButtonState();
            equipButton.render(guiGraphics, mouseX, mouseY, partialTick);
        }
        if (viewport.contains(mouseX, mouseY)) {
            for (ChipWidget chipWidget : chipWidgets) {
                chipWidget.renderHoverTooltip(guiGraphics, mouseX, mouseY);
            }
        }
    }

    private void renderScrollBar(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        LayoutRect track = layout.scrollBarTrackRect();
        drawRect(guiGraphics, track, FastColor.ARGB32.color(180, 25, 25, 25), 5);
        LayoutRect thumb = getScrollBarThumbRect();
        int color = draggingScrollBar || thumb.contains(mouseX, mouseY)
                ? FastColor.ARGB32.color(255, 215, 215, 215)
                : FastColor.ARGB32.color(255, 150, 150, 150);
        drawRect(guiGraphics, thumb, color, 6);
    }

    private LayoutRect getScrollBarThumbRect() {
        LayoutRect track = layout.scrollBarTrackRect();
        double minScroll = computeMinScrollY();
        int minimumThumb = layout.length(18);
        if (minScroll >= 0) {
            return new LayoutRect(track.left(), track.top(), track.right(), track.bottom());
        }

        double contentHeight = getContentHeight();
        double viewportHeight = 248;
        int thumbHeight = Math.max(minimumThumb,
                (int) Math.round(track.height() * Math.min(1.0D, viewportHeight / contentHeight)));
        double progress = Math.max(0.0D, Math.min(1.0D, scrollDesignY / minScroll));
        int thumbTop = track.top() + (int) Math.round((track.height() - thumbHeight) * progress);
        return new LayoutRect(track.left(), thumbTop, track.right(), thumbTop + thumbHeight);
    }

    private void updateScrollFromThumb(double mouseY) {
        LayoutRect track = layout.scrollBarTrackRect();
        LayoutRect thumb = getScrollBarThumbRect();
        int travel = track.height() - thumb.height();
        if (travel <= 0) {
            scrollDesignY = 0;
            return;
        }
        double thumbTop = Math.max(track.top(), Math.min(mouseY - scrollBarGrabOffset, track.bottom() - thumb.height()));
        double progress = (thumbTop - track.top()) / travel;
        scrollDesignY = computeMinScrollY() * progress;
        clampScrollY();
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

        drawRect(guiGraphics, layout.rect(0, 20, 105, 250),
                FastColor.ARGB32.color(100, 160, 160, 160), 1);
        drawRect(guiGraphics, layout.rect(0, 0, 480, 20),
                FastColor.ARGB32.color(100, 160, 160, 160), 0);
        renderCardPanel(guiGraphics);

        renderEquippedCardsTitle(guiGraphics);
        hoveredSlot = slotAt(mouseX, mouseY);
        for (int index = 0; index < getSlots().size(); index++) {
            drawRect(guiGraphics, layout.slotRect(index), FastColor.ARGB32.color(150, 0, 0, 0), 1);
        }
    }

    private void renderEquippedCardsTitle(GuiGraphics guiGraphics) {
        LayoutRect titleRect = layout.equippedCardsTitleRect();
        drawBorderedRect(guiGraphics, titleRect,
                FastColor.ARGB32.color(180, 200, 200, 200),
                FastColor.ARGB32.color(100, 0, 0, 0), 1);

        Font font = Minecraft.getInstance().font;
        float textScale = layout.scale() * 0.65F;
        String title = Component.translatable("gene_chip.details.equipped_cards").getString();
        int textWidth = font.width(title);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(
                titleRect.left() + (titleRect.width() - textWidth * textScale) / 2.0F,
                titleRect.top() + (titleRect.height() - font.lineHeight * textScale) / 2.0F,
                2);
        pose.scale(textScale, textScale, 1);
        guiGraphics.drawString(font, title, 0, 0, 0xFFFFFFFF, false);
        pose.popPose();
    }

    private void renderCardPanel(GuiGraphics guiGraphics) {
        LayoutRect cardPanel = layout.cardPanelRect();
        LayoutRect detailPanel = layout.detailPanelRect();
        int borderColor = FastColor.ARGB32.color(180, 200, 200, 200);
        drawBorderedRect(guiGraphics, cardPanel,
                borderColor,
                FastColor.ARGB32.color(110, 30, 30, 30), -42);
        drawRect(guiGraphics, layout.scrollBarTopBorderRect(), borderColor, -42);
        drawRect(guiGraphics, layout.scrollBarBottomBorderRect(), borderColor, -42);
        drawBorderedRect(guiGraphics, detailPanel,
                FastColor.ARGB32.color(180, 200, 200, 200),
                FastColor.ARGB32.color(150, 18, 18, 18), -40);
    }

    private void drawBorderedRect(GuiGraphics guiGraphics, LayoutRect outer, int borderColor, int fillColor, int z) {
        drawRect(guiGraphics, outer, borderColor, z);
        int border = layout.length(1);
        LayoutRect inner = new LayoutRect(
                outer.left() + border, outer.top() + border,
                outer.right() - border, outer.bottom() - border);
        drawRect(guiGraphics, inner, fillColor, z + 1);
    }

    private static void drawRect(GuiGraphics guiGraphics, LayoutRect rect, int color, int z) {
        GuiUtil.drawColorWithSize(guiGraphics, rect.left(), rect.top(), rect.width(), rect.height(), color, z);
    }

    private void renderSelectedChipDetails(GuiGraphics guiGraphics) {
        LayoutRect borderPanel = layout.detailPanelRect();
        int inset = layout.length(2);
        LayoutRect panel = new LayoutRect(
                borderPanel.left() + inset, borderPanel.top() + inset,
                borderPanel.right() - inset, borderPanel.bottom() - inset);
        float scale = layout.scale();
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack pose = guiGraphics.pose();

        if (selectedChip == null) {
            drawWrappedText(guiGraphics, Component.translatable("gene_chip.details.hint"),
                    panel.left() + layout.length(5), panel.top() + layout.length(8),
                    panel.width() - layout.length(10), scale * 0.8F, 0xFFB0B0B0);
            return;
        }

        Chip chip = selectedChip.getChip();
        float iconSize = 24 * scale;
        GuiUtil.drawTextureWithSize(chip.getTexture(), guiGraphics,
                panel.left() + 5 * scale, panel.top() + 5 * scale, iconSize, iconSize, 8);
        GuiUtil.drawTextureWithSize(chip.getType().getSmallCrystalIcon(), guiGraphics,
                panel.right() - 13 * scale, panel.top() + 7 * scale, 8 * scale, 8 * scale,
                2, 2, 4, 4, 8, 8, 8);

        pose.pushPose();
        pose.translate(panel.left() + 33 * scale, panel.top() + 6 * scale, 8);
        pose.scale(scale * 0.85F, scale * 0.85F, 1);
        guiGraphics.drawString(minecraft.font, chip.getDisplayName(), 0, 0, 0xFFFFFFFF, false);
        pose.popPose();

        Component typeText = Component.translatable("gene_chip.chip_type." + chip.getType().getSerializedName());
        pose.pushPose();
        pose.translate(panel.left() + 33 * scale, panel.top() + 18 * scale, 8);
        pose.scale(scale * 0.7F, scale * 0.7F, 1);
        guiGraphics.drawString(minecraft.font, typeText, 0, 0, 0xFFCCCCCC, false);
        pose.popPose();

        int contentLeft = panel.left() + layout.length(5);
        int contentWidth = panel.width() - layout.length(10);
        float textScale = scale * 0.72F;
        int y = panel.top() + layout.length(36);
        y = drawWrappedText(guiGraphics, chip.getDesc(), contentLeft, y, contentWidth, textScale, 0xFFCCCCCC);
        y += layout.length(5);
        drawWrappedText(guiGraphics, chip.detailDesc(selectedChip.getLvl()),
                contentLeft, y, contentWidth, textScale, 0xFFFFFFFF);

        renderExperienceFooter(guiGraphics, panel, selectedChip);
    }

    private void renderExperienceFooter(GuiGraphics guiGraphics, LayoutRect panel, ChipInstance<?> instance) {
        float scale = layout.scale();
        LayoutRect bar = layout.experienceBarRect();
        int required = instance.getLvl() >= GameplayConfig.getMaxLevel() ? 0 : ChipProgression.requiredExperience(instance.getLvl());
        float progress = required == 0 ? 1.0F : Math.min(1.0F, instance.getExp() / (float) required);

        GuiUtil.drawColorWithSize(guiGraphics, bar.left(), bar.top(), bar.width(), bar.height(), 0xFFA0A0A0, 8);
        GuiUtil.drawColorWithSize(guiGraphics, bar.left() + 1, bar.top() + 1,
                bar.width() - 2, bar.height() - 2, 0xFF242424, 9);
        int fillWidth = Math.round((bar.width() - 2) * progress);
        if (fillWidth > 0) {
            GuiUtil.drawColorWithSize(guiGraphics, bar.left() + 1, bar.top() + 1,
                    fillWidth, bar.height() - 2, 0xFF4D9CFF, 10);
        }

        Component levelText = Component.translatable("gene_chip.details.level", instance.getLvl());
        Component expText = required == 0
                ? Component.translatable("gene_chip.details.max")
                : Component.translatable("gene_chip.details.exp_progress", instance.getExp(), required);
        PoseStack pose = guiGraphics.pose();
        pose.pushPose();
        pose.translate(panel.left() + 5 * scale, bar.top() - 12 * scale, 11);
        pose.scale(scale * 0.72F, scale * 0.72F, 1);
        guiGraphics.drawString(Minecraft.getInstance().font, levelText, 0, 0, 0xFFFFFF55, false);
        pose.popPose();
        pose.pushPose();
        pose.translate(bar.left(), bar.bottom() + 2 * scale, 11);
        pose.scale(scale * 0.65F, scale * 0.65F, 1);
        guiGraphics.drawString(Minecraft.getInstance().font, expText, 0, 0, 0xFFCCCCCC, false);
        pose.popPose();
    }

    private int drawWrappedText(GuiGraphics guiGraphics, Component text, int x, int y,
                                int width, float textScale, int color) {
        Minecraft minecraft = Minecraft.getInstance();
        int wrapWidth = Math.max(1, (int) (width / textScale));
        List<net.minecraft.util.FormattedCharSequence> lines = minecraft.font.split(text, wrapWidth);
        PoseStack pose = guiGraphics.pose();
        int lineHeight = Math.max(1, Math.round(10 * textScale));
        for (int index = 0; index < lines.size(); index++) {
            pose.pushPose();
            pose.translate(x, y + index * lineHeight, 8);
            pose.scale(textScale, textScale, 1);
            guiGraphics.drawString(minecraft.font, lines.get(index), 0, 0, color, false);
            pose.popPose();
        }
        return y + lines.size() * lineHeight;
    }

    public void selectChip(ChipInstance<?> chipInstance) {
        this.selectedChip = chipInstance;
        updateEquipButtonState();
    }

    private void updateEquipButtonState() {
        if (equipButton == null) return;
        equipButton.visible = selectedChip != null;
        if (selectedChip == null) {
            equipButton.active = false;
            return;
        }

        boolean equipped = false;
        boolean hasEmptySlot = false;
        IntObjectMap<ChipSlot> slots = getSlots();
        for (int index = 0; index < slots.size(); index++) {
            ChipSlot slot = slots.get(index);
            if (slot == null) continue;
            Optional<ChipInstance<?>> instance = slot.instance();
            if (instance.isPresent() && instance.get().getChip() == selectedChip.getChip()) {
                equipped = true;
                break;
            }
            hasEmptySlot |= slot.isEmpty();
        }

        if (equipped) {
            equipButton.setMessage(Component.translatable("gene_chip.details.equipped"));
            equipButton.active = false;
        } else if (!hasEmptySlot) {
            equipButton.setMessage(Component.translatable("gene_chip.details.no_slot"));
            equipButton.active = false;
        } else {
            equipButton.setMessage(Component.translatable("gene_chip.details.equip"));
            equipButton.active = true;
        }
    }

    private void equipSelectedChip() {
        if (selectedChip == null) return;
        IntObjectMap<ChipSlot> slots = getSlots();
        for (int index = 0; index < slots.size(); index++) {
            ChipSlot slot = slots.get(index);
            if (slot != null && slot.isEmpty()) {
                setSlotChip(selectedChip, index);
                updateEquipButtonState();
                return;
            }
        }
    }

    public boolean isSelected(ChipInstance<?> chipInstance) {
        return selectedChip == chipInstance;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            LayoutRect track = layout.scrollBarTrackRect();
            if (track.contains(mouseX, mouseY) && computeMinScrollY() < 0) {
                LayoutRect thumb = getScrollBarThumbRect();
                draggingScrollBar = true;
                if (thumb.contains(mouseX, mouseY)) {
                    scrollBarGrabOffset = mouseY - thumb.top();
                } else {
                    scrollBarGrabOffset = thumb.height() / 2.0D;
                    updateScrollFromThumb(mouseY);
                }
                return true;
            }
        }

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
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && draggingScrollBar) {
            updateScrollFromThumb(mouseY);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0 && draggingScrollBar) {
            draggingScrollBar = false;
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
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
        if (!layout.cardViewportRect().contains(mouseX, mouseY)) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
        }

        double oldScroll = scrollDesignY;
        scrollDesignY = Math.max(computeMinScrollY(), Math.min(scrollDesignY + scrollY * SCROLL_STEP, 0));
        return oldScroll != scrollDesignY || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    private double getContentHeight() {
        int rows = (chipWidgets.size() + CHIP_COLUMNS - 1) / CHIP_COLUMNS;
        if (rows == 0) return 0;
        return (rows - 1) * CHIP_ROW_PITCH + CARD_HEIGHT * CARD_SCALE;
    }

    private double computeMinScrollY() {
        double viewportBottom = 269;
        double firstCardTop = CHIP_GRID_Y;
        double contentHeight = getContentHeight();
        if (contentHeight <= 0) return 0;

        double bottomLimitedScroll = viewportBottom - (firstCardTop + contentHeight);
        return Math.min(0, bottomLimitedScroll);
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

            float iconX = layout.designX(2 + visibleIndex * 20);
            float iconY = layout.designY(summaryY);
            float scale = layout.scale();
            GuiUtil.drawTextureWithSize(type.getSmallCrystalIcon(), guiGraphics,
                    iconX, iconY, 8 * scale, 8 * scale,
                    2, 2, 4, 4, 8, 8, 5);

            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(layout.designX(11 + visibleIndex * 20), layout.designY(summaryY + 1), 1);
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

        LayoutRect equippedCardsTitleRect() {
            return rect(0, 39, 105, 18);
        }

        LayoutRect slotRect(int index) {
            return rect(0, 60 + index * 21, 105, 18);
        }

        LayoutRect cardRect(int index, double scrollY) {
            return rect(CHIP_GRID_X + (index % CHIP_COLUMNS) * CHIP_COLUMN_PITCH,
                    CHIP_GRID_Y + (index / CHIP_COLUMNS) * CHIP_ROW_PITCH + scrollY,
                    CARD_WIDTH * CARD_SCALE, CARD_HEIGHT * CARD_SCALE);
        }

        LayoutRect cardPanelRect() {
            return rect(105, 20, 240, 250);
        }

        LayoutRect cardViewportRect() {
            return rect(106, 21, 238, 248);
        }

        LayoutRect scrollBarTrackRect() {
            return rect(345, 21, 6, 248);
        }

        LayoutRect scrollBarTopBorderRect() {
            return rect(345, 20, 6, 1);
        }

        LayoutRect scrollBarBottomBorderRect() {
            return rect(345, 269, 6, 1);
        }

        LayoutRect detailPanelRect() {
            return rect(351, 20, 129, 250);
        }

        LayoutRect experienceBarRect() {
            return rect(358, 226, 115, 8);
        }

        LayoutRect equipButtonRect() {
            return rect(358, 248, 115, 16);
        }
    }

    private static final class DetailActionButton extends AbstractWidget {
        private final Runnable onPress;
        private final int borderWidth;

        DetailActionButton(LayoutRect rect, float uiScale, Runnable onPress, Component message) {
            super(rect.left(), rect.top(), rect.width(), rect.height(), message);
            this.onPress = onPress;
            this.borderWidth = Math.max(1, Math.round(uiScale));
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button != 0 || !this.active || !this.visible || !this.isMouseOver(mouseX, mouseY)) {
                return false;
            }
            playDownSound(Minecraft.getInstance().getSoundManager());
            onPress.run();
            return true;
        }

        @Override
        protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
            int borderColor = active ? 0xFFA0A0A0 : 0xFF707070;
            int fillColor = !active ? 0xFF353535 : isHovered ? 0xFF595959 : 0xFF2E2E2E;
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), width, height, fillColor, 9);
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), width, borderWidth, borderColor, 10);
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY() + height - borderWidth,
                    width, borderWidth, borderColor, 10);
            GuiUtil.drawColorWithSize(guiGraphics, getX(), getY(), borderWidth, height, borderColor, 10);
            GuiUtil.drawColorWithSize(guiGraphics, getX() + width - borderWidth, getY(),
                    borderWidth, height, borderColor, 10);

            Font font = Minecraft.getInstance().font;
            int textX = getX() + (width - font.width(getMessage())) / 2;
            int textY = getY() + (height - font.lineHeight) / 2 + 1;
            PoseStack pose = guiGraphics.pose();
            pose.pushPose();
            pose.translate(0,0,10);
            guiGraphics.drawString(font, getMessage(), textX, textY, active ? 0xFFFFFFFF : 0xFF999999, false);
            pose.popPose();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
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
