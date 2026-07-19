package de.twyco.soundboard.gui.config.widget;

import de.twyco.soundboard.gui.config.SoundboardConfigScreen;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

final class KeyComboButton extends Button {

    private final SoundboardConfigScreen screen;
    private final Consumer<KeyCombo> onChange;
    private KeyCombo combo;

    KeyComboButton(
            SoundboardConfigScreen screen,
            KeyCombo combo,
            Consumer<KeyCombo> onChange,
            Component tooltip
    ) {
        super(
                0,
                0,
                150,
                DEFAULT_HEIGHT,
                Component.literal(combo.toString()),
                button -> {
                },
                DEFAULT_NARRATION
        );
        this.screen = screen;
        this.combo = combo;
        this.onChange = onChange;
        setTooltip(Tooltip.create(tooltip));
    }

    @Override
    public void onPress(InputWithModifiers input) {
        screen.beginKeyComboCapture(this, combo, this::setCombo);
    }

    @Override
    protected void renderContents(
            GuiGraphics graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        renderDefaultSprite(graphics);
        renderDefaultLabel(graphics.textRendererForWidget(
                this,
                GuiGraphics.HoveredTextEffects.NONE
        ));
    }

    private void setCombo(KeyCombo combo) {
        this.combo = combo;
        setMessage(Component.literal(combo.toString()));
        onChange.accept(combo);
    }
}
