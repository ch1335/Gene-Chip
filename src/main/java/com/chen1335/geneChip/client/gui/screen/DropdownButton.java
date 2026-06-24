package com.chen1335.geneChip.client.gui.screen;

import com.chen1335.geneChip.client.gui.GuiUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class DropdownButton<T> extends AbstractWidget {
    private static final int RENDER_Z = 300;

    private final List<T> options;
    private final Function<T, Component> valueToText;
    private final Consumer<T> onSelect;
    private final int baseHeight;
    private final int optionHeight;

    private T value;
    private boolean expanded;

    public DropdownButton(int x, int y, int width, int baseHeight, List<T> options, T initial,
                          Function<T, Component> valueToText, Consumer<T> onSelect) {
        super(x, y, width, baseHeight, Component.empty());
        this.options = options;
        this.valueToText = valueToText;
        this.onSelect = onSelect;
        this.value = initial;
        this.baseHeight = baseHeight;
        this.optionHeight = baseHeight;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
    }

    public int getExpandedHeight() {
        return baseHeight + options.size() * optionHeight;
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        int h = expanded ? getExpandedHeight() : baseHeight;
        return mouseX >= getX() && mouseX <= getX() + (double) width && mouseY >= getY() && mouseY <= getY() + (double) h;
    }

    public boolean isOverTrigger(double mouseX, double mouseY) {
        return mouseX >= getX() && mouseX <= getX() + (double) width && mouseY >= getY() && mouseY <= getY() + (double) baseHeight;
    }

    private int optionAt(double mouseX, double mouseY) {
        if (!expanded) return -1;
        if (mouseX < getX() || mouseX > getX() + (double) width) return -1;
        if (mouseY < getY() + (double) baseHeight) return -1;
        int idx = (int) ((mouseY - getY() - baseHeight) / optionHeight);
        if (idx < 0 || idx >= options.size()) return -1;
        return idx;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return false;
        if (!isMouseOver(mouseX, mouseY)) return false;
        if (isOverTrigger(mouseX, mouseY)) {
            expanded = !expanded;
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        int idx = optionAt(mouseX, mouseY);
        if (idx >= 0) {
            T newval = options.get(idx);
            value = newval;
            onSelect.accept(newval);
            expanded = false;
            playDownSound(Minecraft.getInstance().getSoundManager());
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;
        int borderColor = 0xFFA0A0A0;
        boolean triggerHover = isOverTrigger(mouseX, mouseY);
        int triggerColor = triggerHover ? 0xFF595959 : 0xFF2E2E2E;

        drawBox(guiGraphics, getX(), getY(), width, baseHeight, triggerColor, borderColor);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, RENDER_Z + 1);
        Component text = valueToText.apply(value);
        guiGraphics.drawString(font, text, getX() + 6, getY() + (baseHeight - font.lineHeight) / 2 + 1, 0xFFFFFFFF, false);
        String arrow = expanded ? "▲" : "▼";
        guiGraphics.drawString(font, arrow, getX() + width - 12, getY() + (baseHeight - font.lineHeight) / 2 + 1, 0xFFCCCCCC, false);
        guiGraphics.pose().popPose();

        if (expanded) {
            for (int i = 0; i < options.size(); i++) {
                int oy = getY() + baseHeight + i * optionHeight;
                boolean hov = mouseX >= getX() && mouseX <= getX() + (double) width && mouseY >= oy && mouseY <= oy + (double) optionHeight;
                int color = hov ? 0xFF595959 : 0xFF3A3A3A;
                drawBox(guiGraphics, getX(), oy, width, optionHeight, color, borderColor);
                Component optText = valueToText.apply(options.get(i));
                boolean isCurrent = options.get(i) == value || (options.get(i) != null && options.get(i).equals(value));
                int textColor = isCurrent ? 0xFFFFFF00 : 0xFFFFFFFF;
                guiGraphics.pose().pushPose();
                guiGraphics.pose().translate(0, 0, RENDER_Z + 1);
                guiGraphics.drawString(font, optText, getX() + 6, oy + (optionHeight - font.lineHeight) / 2 + 1, textColor, false);
                guiGraphics.pose().popPose();
            }
        }
    }

    private static void drawBox(GuiGraphics guiGraphics, int x, int y, int w, int h, int fillColor, int borderColor) {
        GuiUtil.drawColorWithSize(guiGraphics, x, y, w, h, fillColor, RENDER_Z);
        GuiUtil.drawColorWithSize(guiGraphics, x, y, w, 1, borderColor, RENDER_Z + 1);
        GuiUtil.drawColorWithSize(guiGraphics, x, y + h - 1, w, 1, borderColor, RENDER_Z + 1);
        GuiUtil.drawColorWithSize(guiGraphics, x, y, 1, h, borderColor, RENDER_Z + 1);
        GuiUtil.drawColorWithSize(guiGraphics, x + w - 1, y, 1, h, borderColor, RENDER_Z + 1);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
    }
}
