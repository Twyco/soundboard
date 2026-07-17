package de.twyco.soundboard.gui.config.widget;

import java.util.function.IntConsumer;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.network.chat.Component;

final class AmplifierSlider extends AbstractSliderButton {

    private static final int MAX_AMPLIFIER = 300;

    private final IntConsumer onChange;

    AmplifierSlider(int amplifier, IntConsumer onChange) {
        super(
                0,
                0,
                100,
                20,
                Component.empty(),
                Math.max(0, Math.min(amplifier, MAX_AMPLIFIER)) / (double) MAX_AMPLIFIER
        );
        this.onChange = onChange;
        updateMessage();
    }

    @Override
    protected void updateMessage() {
        setMessage(Component.literal(getAmplifier() + "%"));
    }

    @Override
    protected void applyValue() {
        onChange.accept(getAmplifier());
    }

    private int getAmplifier() {
        return (int) Math.round(value * MAX_AMPLIFIER);
    }
}
