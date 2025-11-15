package de.twyco.soundboard.gui.config.entries;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class ActionButtonEntry extends AbstractConfigListEntry<Void> {

    private final ButtonWidget button;

    public ActionButtonEntry(Text name, Text buttonText, Runnable action) {
        super(name, false);
        this.button = ButtonWidget.builder(buttonText, b -> action.run()).build();
    }

    @Override
    public void render(DrawContext ctx, int index, int y, int x,
                       int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {

        button.setX(x);
        button.setY(y);
        button.setWidth(150);
        button.setHeight(20);

        button.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public Optional<Void> getDefaultValue() {
        return Optional.empty();
    }

    @Override
    public Void getValue() {
        return null;
    }

    @Override
    public List<? extends Selectable> narratables() {
        return List.of(button);
    }

    @Override
    public List<? extends Element> children() {
        return List.of(button);
    }
}
