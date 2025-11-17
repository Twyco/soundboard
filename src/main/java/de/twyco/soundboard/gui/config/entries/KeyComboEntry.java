package de.twyco.soundboard.gui.config.entries;

import de.twyco.soundboard.util.keybinding.KeyCombo;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeyComboEntry extends AbstractConfigListEntry<Void> {

    private static KeyComboEntry currentlyListening = null;

    private final ButtonWidget button;
    private final TextWidget textWidget;
    private final java.util.function.Consumer<KeyCombo> onChange;
    private final int fieldLabelColor;

    private KeyCombo combo;
    private boolean listening = false;
    private final Set<Integer> pressedKeys = new LinkedHashSet<>();

    public KeyComboEntry(@NotNull Text fieldLabel,
                         @NotNull KeyCombo initialCombo,
                         @NotNull java.util.function.Consumer<KeyCombo> onChange
    ) {
        this(fieldLabel, initialCombo, onChange, 0xFFFFFF);
    }

    public KeyComboEntry(@NotNull Text fieldLabel,
                         @NotNull KeyCombo initialCombo,
                         @NotNull java.util.function.Consumer<KeyCombo> onChange,
                         int fieldLabelColor
    ) {
        super(fieldLabel, false);
        this.combo = initialCombo;
        this.onChange = onChange;

        this.button = ButtonWidget.builder(
                        Text.literal(initialCombo.toString()),
                        b -> onButtonClick()
                )
                .build();
        this.textWidget = new TextWidget(fieldLabel, MinecraftClient.getInstance().textRenderer);
        this.fieldLabelColor = fieldLabelColor;
    }

    private void onButtonClick() {
        if(!listening) {
            if (currentlyListening != null && currentlyListening != this) {
                currentlyListening.stopListening();
            }
            startListening();
            currentlyListening = this;
        }else {
            stopListening();
            if (currentlyListening == this) {
                currentlyListening = null;
            }
        }
    }

    private void startListening() {
        listening = true;
        pressedKeys.clear();
        updateButtonMessage();
    }

    private void stopListening() {
        listening = false;
        pressedKeys.clear();
        updateButtonMessage();
    }


    public void updateButtonMessage() {
        if (listening) {
            Text listeningLabel = Text.empty()
                    .append(Text.literal("> ").formatted(Formatting.YELLOW))
                    .append(Text.literal(combo.toString())
                            .formatted(Formatting.WHITE, Formatting.UNDERLINE))
                    .append(Text.literal(" <").formatted(Formatting.YELLOW));
            button.setMessage(listeningLabel);

            return;
        }

        button.setMessage(Text.literal(combo.toString()));
    }

    @Override
    public void render(DrawContext ctx, int index, int y, int x,
                       int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;


        int buttonWidth = 150;
        int buttonHeight = 20;
        int bx = x + entryWidth - buttonWidth;
        int by = y + (entryHeight - buttonHeight) / 2;

        button.setX(bx);
        button.setY(by);
        button.setWidth(buttonWidth);
        button.setHeight(buttonHeight);
        button.setFocused(listening);

        button.render(ctx, mouseX, mouseY, delta);


        int labelWidth = Math.min(textRenderer.getWidth(textWidget.getMessage()), entryWidth - buttonWidth);
        int labelHeight = 20;
        int textY = y + (entryHeight - labelHeight) / 2;

        textWidget.setX(x);
        textWidget.setY(textY);
        textWidget.setWidth(labelWidth);
        textWidget.setHeight(labelHeight);
        textWidget.setTextColor(fieldLabelColor);

        textWidget.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(KeyInput event) {
        if (!listening) {
            return button.keyPressed(event) || super.keyPressed(event);
        }

        if (event.key() == GLFW.GLFW_KEY_BACKSPACE
                || event.key() == GLFW.GLFW_KEY_DELETE
                || event.key() == GLFW.GLFW_KEY_ESCAPE
        ) {
            KeyCombo combo = KeyCombo.empty(this.combo.getId());
            this.combo = combo;

            onChange.accept(combo);

            stopListening();
            return true;
        }

        pressedKeys.add(event.key());
        return true;
    }

    @Override
    public boolean keyReleased(KeyInput event) {
        if (!listening) {
            return button.keyReleased(event) || super.keyReleased(event);
        }

        int[] codes = pressedKeys.stream().mapToInt(Integer::intValue).toArray();
        KeyCombo combo = KeyCombo.of(this.combo.getId(), codes);
        this.combo = combo;

        onChange.accept(combo);

        stopListening();
        return true;
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
