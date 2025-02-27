package net.flytre.flytre_lib.api.gui;

import net.flytre.flytre_lib.mixin.gui.EntryListWidgetAccessor;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.widget.ElementListWidget;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.option.SimpleOption;
import net.minecraft.client.util.math.MatrixStack;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

public class ButtonListWidget<K extends ClickableWidget> extends ElementListWidget<ButtonListWidget.ButtonEntry<K>> {


    protected final int buttonWidth;
    protected final int buttonLeft;
    protected final int buttonPadding;

    /**
     * @param percentCenter The center of this widget as a percent of the screen's width
     * @param percentWidth  The percent of the screen's width this widget takes around the center
     * @param percentBuffer The button size buffer to make buttons not touch the edge of the screen / adjacent lists
     * @param buttonPadding The amount of padding between each entry
     */
    public ButtonListWidget(MinecraftClient client, int parentWidth, int parentHeight, int top, int bottom, int buttonHeight, int buttonPadding, float percentCenter, float percentWidth, float percentBuffer) {
        super(client, parentWidth, parentHeight, top, bottom, buttonHeight + buttonPadding);
        this.buttonWidth = (int) ((percentWidth - percentBuffer) * parentWidth);
        this.left = (int) (parentWidth * (percentCenter - percentWidth / 2));
        this.right = left + (int) (percentWidth * parentWidth);
        this.buttonLeft = (int) (parentWidth * percentCenter) - buttonWidth / 2;
        this.centerListVertically = false;
        this.buttonPadding = buttonPadding / 2;
    }

    public void addEntry(SimpleOption<?> option) {
        this.addEntry(ButtonEntry.create(this.client.options, buttonLeft, buttonWidth, option, buttonPadding));
    }

    public void addEntry(ButtonCreator<K> buttonCreator) {
        this.addEntry(ButtonEntry.create(buttonLeft, buttonWidth, buttonCreator, buttonPadding));
    }

    @Override
    public int getRowWidth() {
        return this.right - this.left;
    }

    protected int getScrollbarPositionX() {
        return super.getScrollbarPositionX() + 32;
    }

    @Nullable
    public K getButtonFor(SimpleOption<?> option) {

        for (ButtonEntry<K> element : this.children())
            if (element.option == option && option != null)
                return element.button;

        return null;

    }

    public Optional<K> getHoveredButton(double mouseX, double mouseY) {

        for (ButtonEntry<K> entry : this.children()) {
            if (entry.button.isMouseOver(mouseX, mouseY)) {
                return Optional.of(entry.button);
            }
        }

        return Optional.empty();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        this.updateScrollingState(mouseX, mouseY, button);
        if (!this.isMouseOver(mouseX, mouseY)) {
            return false;
        } else {
            ButtonEntry<K> entry = this.getEntryAtPosition2(mouseX, mouseY);
            if (entry != null) {
                if (entry.mouseClicked(mouseX, mouseY, button)) {
                    this.setFocused(entry);
                    this.setDragging(true);
                    return true;
                }
            } else if (button == 0) {
                this.clickedHeader((int) (mouseX - (double) (this.left + this.width / 2 - this.getRowWidth() / 2)), (int) (mouseY - (double) this.top) + (int) this.getScrollAmount() - 4);
                return true;
            }

            return ((EntryListWidgetAccessor<?>) this).getScrolling();
        }
    }

    protected final ButtonEntry<K> getEntryAtPosition2(double x, double y) {

        for (var entry : children()) {
            if (entry.button.isMouseOver(x, y))
                return entry;
        }
        return null;

    }

    @Override
    public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
        int k = getRowLeft();
        if (this.getScrollAmount() > getMaxScroll()) {
            this.setScrollAmount(this.getMaxScroll());
        }
        int l = this.top + 4 - (int) getScrollAmount();
        renderList(matrices, mouseX, mouseY, delta);
    }


    @FunctionalInterface
    public interface ButtonCreator<K extends ClickableWidget> {
        K create(int x, int y, int buttonWidth);
    }


    protected static class ButtonEntry<K extends ClickableWidget> extends Entry<ButtonEntry<K>> {
        public final K button;
        public final SimpleOption<?> option;
        public final int padding;


        protected ButtonEntry(K button, int padding) {
            this.button = button;
            this.option = null;
            this.padding = padding;
        }

        protected ButtonEntry(K button, SimpleOption<?> option, int padding) {
            this.button = button;
            this.option = option;
            this.padding = padding;
        }

        /**
         * Contract: option.createButton produces an instance of K
         */
        public static <K extends ClickableWidget> ButtonEntry<K> create(GameOptions options, int startX, int buttonWidth, SimpleOption<?> option, int buttonPadding) {
            //noinspection unchecked
            return new ButtonEntry<>((K) option.createButton(options, startX, 0, buttonWidth), option, buttonPadding);
        }

        public static <K extends ClickableWidget> ButtonEntry<K> create(int startX, int buttonWidth, ButtonCreator<K> buttonCreator, int buttonPadding) {
            K button = buttonCreator.create(startX, 0, buttonWidth);
            return new ButtonEntry<>(button, buttonPadding);
        }


        @Override
        public void render(MatrixStack matrices, int index, int y, int x, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float tickDelta) {
            button.y = y + padding;
            button.render(matrices, mouseX, mouseY, tickDelta);
        }

        @Override
        public List<? extends Element> children() {
            return Collections.singletonList(button);
        }

        @Override
        public List<? extends Selectable> selectableChildren() {
            return Collections.singletonList(button);
        }
    }
}
