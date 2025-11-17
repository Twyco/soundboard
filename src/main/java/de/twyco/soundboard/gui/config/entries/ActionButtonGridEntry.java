package de.twyco.soundboard.gui.config.entries;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.text.Text;

import java.util.List;
import java.util.Optional;

public class ActionButtonGridEntry extends AbstractConfigListEntry<Void> {

    private final List<ActionButtonEntry> buttonEntries;
    private final int spacing;

    public ActionButtonGridEntry(ActionButtonEntry... buttonEntries) {
        this(4,  buttonEntries);
    }

    public ActionButtonGridEntry(int spacing, ActionButtonEntry... buttonEntries) {
        super(Text.empty(), false);
        this.buttonEntries = List.of(buttonEntries);
        this.spacing = Math.max(0, spacing);
    }

    @Override
    public void render(DrawContext ctx, int index, int y, int x,
                       int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {

        if(buttonEntries.isEmpty()) {
            return;
        }

        int count = buttonEntries.size();
        int totalSpacing = spacing * (count - 1);
        int buttonWidth = (entryWidth - totalSpacing) / count;

        int currentX = x;

        for (ActionButtonEntry button : buttonEntries) {
            button.render(ctx, index, y, currentX, buttonWidth, entryHeight, mouseX, mouseY, hovered, delta);
            currentX += buttonWidth + spacing;
        }
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
        return this.buttonEntries.stream()
                .flatMap(e -> e.narratables().stream())
                .toList();
    }

    @Override
    public List<? extends Element> children() {
        return this.buttonEntries.stream()
                .flatMap(e -> e.children().stream())
                .toList();
    }
}
