package de.twyco.soundboard.gui.config.entries;

import de.twyco.soundboard.util.keybinding.KeyCombo;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeyComboEntry extends AbstractConfigListEntry<Void> {

    private static KeyComboEntry currentlyListening = null;

    private final Button button;
    private final StringWidget textWidget;
    private final java.util.function.Consumer<KeyCombo> onChange;

    private KeyCombo combo;
    private boolean listening = false;
    private final Set<Integer> pressedKeys = new LinkedHashSet<>();

    public KeyComboEntry(@NotNull Component fieldLabel,
                         @NotNull KeyCombo initialCombo,
                         @NotNull java.util.function.Consumer<KeyCombo> onChange
    ) {
        this(fieldLabel, initialCombo, onChange, 0xFFFFFF);
    }

    public KeyComboEntry(@NotNull Component fieldLabel,
                         @NotNull KeyCombo initialCombo,
                         @NotNull java.util.function.Consumer<KeyCombo> onChange,
                         int fieldLabelColor
    ) {
        super(createLabel(fieldLabel, fieldLabelColor), false);
        this.combo = initialCombo;
        this.onChange = onChange;

        this.button = Button.builder(
                        Component.literal(initialCombo.toString()),
                        b -> onButtonClick()
                )
                .build();
        this.textWidget = new StringWidget(fieldLabel, Minecraft.getInstance().font);
    }

    private static Component createLabel(Component fieldLabel, int fieldLabelColor) {
        ChatFormatting formatting = ChatFormatting.getById(fieldLabelColor);
        MutableComponent text = fieldLabel.copy();
        if (formatting != null) {
            text.withStyle(formatting);
        }
        return text;
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
            Component listeningLabel = Component.empty()
                    .append(Component.literal("> ").withStyle(ChatFormatting.YELLOW))
                    .append(Component.literal(combo.toString())
                            .withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                    .append(Component.literal(" <").withStyle(ChatFormatting.YELLOW));
            button.setMessage(listeningLabel);

            return;
        }

        button.setMessage(Component.literal(combo.toString()));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor ctx, int index, int y, int x,
                       int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {
        Font textRenderer = Minecraft.getInstance().font;


        int buttonWidth = 150;
        int buttonHeight = 20;
        int bx = x + entryWidth - buttonWidth;
        int by = y + (entryHeight - buttonHeight) / 2;

        button.setX(bx);
        button.setY(by);
        button.setWidth(buttonWidth);
        button.setHeight(buttonHeight);
        button.setFocused(listening);

        button.extractRenderState(ctx, mouseX, mouseY, delta);


        int labelWidth = Math.min(textRenderer.width(textWidget.getMessage()), entryWidth - buttonWidth);
        int labelHeight = 20;
        int textY = y + (entryHeight - labelHeight) / 2;

        textWidget.setX(x);
        textWidget.setY(textY);
        textWidget.setWidth(labelWidth);
        textWidget.setHeight(labelHeight);

        textWidget.extractRenderState(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
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
    public boolean keyReleased(KeyEvent event) {
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
    public List<? extends NarratableEntry> narratables() {
        return List.of(button);
    }

    @Override
    public List<? extends GuiEventListener> children() {
        return List.of(button);
    }
}
