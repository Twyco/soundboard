package de.twyco.soundboard.gui.config.entries;

import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.entries.SoundEntry;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.Sound;
import de.twyco.soundboard.util.sound.SoundManager;
import me.shedaniel.clothconfig2.api.AbstractConfigListEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import org.lwjgl.glfw.GLFW;

import java.util.*;

public class KeyComboEntry extends AbstractConfigListEntry<Void> {

    private static KeyComboEntry currentlyListening = null;

    private final Sound sound;
    private final SoundEntry soundEntry;
    private final ButtonWidget button;

    private boolean listening = false;
    private final Set<Integer> pressedKeys = new LinkedHashSet<>();


    public KeyComboEntry(Sound sound, SoundEntry soundEntry) {
        super(Text.translatable("gui.soundboard.config.keybind"), false);
        this.sound = sound;
        this.soundEntry = soundEntry;

        this.button = ButtonWidget.builder(
                        Text.literal(sound.getKeyCombo().toString()),
                        b -> onButtonClick()
                )
                .build();
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


    private void updateButtonMessage() {
        if (listening) {
            Text listeningLabel = Text.empty()
                    .append(Text.literal("> ").formatted(Formatting.YELLOW))
                    .append(Text.literal(sound.getKeyCombo().toString())
                            .formatted(Formatting.WHITE, Formatting.UNDERLINE))
                    .append(Text.literal(" <").formatted(Formatting.YELLOW));
            button.setMessage(listeningLabel);

            return;
        }

        button.setMessage(Text.literal(sound.getKeyCombo().toString()));
    }

    @Override
    public void render(DrawContext ctx, int index, int y, int x,
                       int entryWidth, int entryHeight,
                       int mouseX, int mouseY, boolean hovered, float delta) {
        TextRenderer tr = MinecraftClient.getInstance().textRenderer;

        Text label = this.getFieldName();
        ctx.drawText(tr, "test", 0, y, 0xFFFFFF, true);

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
    }

    @Override
    public boolean keyPressed(KeyInput event) {
        if (!listening) {
            return button.keyPressed(event) || super.keyPressed(event);
        }

        if (event.key() == GLFW.GLFW_KEY_BACKSPACE || event.key() == GLFW.GLFW_KEY_DELETE || event.key() == GLFW.GLFW_KEY_ESCAPE) {
            KeyCombo combo = KeyCombo.empty(sound.getKeyCombo().getId());
            SoundManager.updateSoundKeyCombo(sound, combo);
            soundEntry.keyCombo = combo.getKeyCodes();
            SoundboardConfig.save();

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

        KeyCombo combo = KeyCombo.of(
                sound.getKeyCombo().getId(),
                pressedKeys.stream().mapToInt(Integer::intValue).toArray()
        );

        SoundManager.updateSoundKeyCombo(sound, combo);
        soundEntry.keyCombo = combo.getKeyCodes();
        SoundboardConfig.save();

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
