package de.twyco.soundboard.gui.config.entries;

import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class ActionButtonEntry extends AbstractConfigListEntry<Void> {

    private final Button button;

    public ActionButtonEntry(Component name, Component buttonText, Runnable action) {
        super(name, false);
        this.button = Button.builder(buttonText, b -> action.run()).build();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int index, int y, int x,
                       int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {

        button.setX(x);
        button.setY(y);
        button.setWidth(entryWidth);
        button.setHeight(20);

        button.extractRenderState(ctx, mouseX, mouseY, delta);
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
    public List<? extends NarratableEntry> narratables() {
        return List.of(button);
    }

    @Override
    public @Nullable List<? extends GuiEventListener> children() {
        return List.of(button);
    }
}
