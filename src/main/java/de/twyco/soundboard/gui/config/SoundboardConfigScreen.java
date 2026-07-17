package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.client.GlobalKeybinds;
import de.twyco.soundboard.gui.config.widget.SoundboardConfigList;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.SoundManager;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class SoundboardConfigScreen extends Screen {

    private static final int PAGE_MAX_WIDTH = 720;
    private static final int PAGE_MARGIN = 12;
    private static final int HEADER_HEIGHT = 54;
    private static final int FOOTER_HEIGHT = 36;
    private static final int BUTTON_HEIGHT = 20;
    private static final int GAP = 4;

    private final Screen parent;

    private ConfigDraft draft;
    private Tab selectedTab = Tab.GENERAL;
    private Button generalTabButton;
    private Button soundsTabButton;
    private SoundboardConfigList configList;
    private String soundSearch = "";
    private boolean clearFocusNextTick;

    private Button capturingButton;
    private KeyCombo capturingCombo;
    private Consumer<KeyCombo> capturingCallback;
    private final Set<Integer> capturedKeys = new LinkedHashSet<>();

    public SoundboardConfigScreen(Screen parent) {
        super(Component.translatable("gui.soundboard.config.title"));
        this.parent = parent;
        this.draft = ConfigDraft.from(SoundboardConfig.get());
    }

    @Override
    protected void init() {
        stopKeyComboCapture();

        int pageWidth = Math.min(PAGE_MAX_WIDTH, Math.max(1, width - PAGE_MARGIN * 2));
        int pageX = (width - pageWidth) / 2;
        int tabWidth = Math.max(1, (pageWidth - GAP) / 2);

        generalTabButton = addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.soundboard.config.categories.general.title"),
                                _ -> selectTab(Tab.GENERAL)
                        )
                        .bounds(pageX, 28, tabWidth, BUTTON_HEIGHT)
                        .build()
        );
        soundsTabButton = addRenderableWidget(
                Button.builder(
                                Component.translatable("gui.soundboard.config.categories.sounds.title"),
                                _ -> selectTab(Tab.SOUNDS)
                        )
                        .bounds(pageX + tabWidth + GAP, 28, tabWidth, BUTTON_HEIGHT)
                        .build()
        );
        updateTabButtons();

        int listY = HEADER_HEIGHT;
        if (selectedTab == Tab.SOUNDS) {
            EditBox searchBox = new EditBox(
                    font,
                    pageX,
                    HEADER_HEIGHT,
                    pageWidth,
                    BUTTON_HEIGHT,
                    Component.translatable("gui.soundboard.config.search")
            );
            searchBox.setValue(soundSearch);
            searchBox.setHint(Component.translatable("gui.soundboard.config.search.hint"));
            searchBox.setResponder(this::updateSoundSearch);
            addRenderableWidget(searchBox);
            listY += BUTTON_HEIGHT + GAP;
        }

        int listHeight = Math.max(20, height - listY - FOOTER_HEIGHT);
        configList = new SoundboardConfigList(
                this,
                minecraft,
                pageWidth,
                listHeight,
                listY
        );
        configList.setX(pageX);
        if (selectedTab == Tab.GENERAL) {
            configList.populateGeneral(draft);
        } else {
            populateSoundList();
        }
        addRenderableWidget(configList);

        int footerY = height - 28;
        int footerButtonWidth = Math.max(1, (pageWidth - GAP * 2) / 3);
        addRenderableWidget(
                Button.builder(Component.translatable("gui.cancel"), _ -> onClose())
                        .bounds(pageX, footerY, footerButtonWidth, BUTTON_HEIGHT)
                        .build()
        );
        addRenderableWidget(
                Button.builder(Component.translatable("gui.soundboard.config.action.apply"), _ -> applyDraft())
                        .bounds(
                                pageX + footerButtonWidth + GAP,
                                footerY,
                                footerButtonWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
        addRenderableWidget(
                Button.builder(Component.translatable("gui.done"), _ -> {
                            applyDraft();
                            closeToParent();
                        })
                        .bounds(
                                pageX + (footerButtonWidth + GAP) * 2,
                                footerY,
                                footerButtonWidth,
                                BUTTON_HEIGHT
                        )
                        .build()
        );
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 10, 0xFFFFFFFF);
    }

    @Override
    public void tick() {
        super.tick();
        if (clearFocusNextTick) {
            setFocused(null);
            clearFocusNextTick = false;
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (capturingButton != null) {
            if (event.key() == GLFW.GLFW_KEY_BACKSPACE
                    || event.key() == GLFW.GLFW_KEY_DELETE
                    || event.key() == GLFW.GLFW_KEY_ESCAPE) {
                finishKeyComboCapture(KeyCombo.empty(capturingCombo.getId()));
                return true;
            }
            capturedKeys.add(event.key());
            updateCaptureButtonMessage();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (capturingButton != null) {
            if (!capturedKeys.isEmpty()) {
                finishKeyComboCapture(KeyCombo.of(
                        capturingCombo.getId(),
                        capturedKeys.stream().mapToInt(Integer::intValue).toArray()
                ));
            }
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public void onClose() {
        stopKeyComboCapture();
        closeToParent();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    public void beginKeyComboCapture(
            Button button,
            KeyCombo combo,
            Consumer<KeyCombo> onChange
    ) {
        stopKeyComboCapture();
        capturingButton = button;
        capturingCombo = combo;
        capturingCallback = onChange;
        capturedKeys.clear();
        updateCaptureButtonMessage();
        setFocused(button);
    }

    public void openSoundsFolder() {
        applyDraft();
        SoundManager.openSoundsFolder();
    }

    public void reloadSoundFiles() {
        applyDraft();
        SoundboardConfig.load();
        SoundManager.reload();
        reloadFromConfig();
    }

    public void reloadFromConfig() {
        stopKeyComboCapture();
        draft = ConfigDraft.from(SoundboardConfig.get());
        rebuildWidgets();
    }

    private void selectTab(Tab tab) {
        if (selectedTab == tab) {
            return;
        }
        selectedTab = tab;
        rebuildWidgets();
    }

    private void updateTabButtons() {
        generalTabButton.active = selectedTab != Tab.GENERAL;
        soundsTabButton.active = selectedTab != Tab.SOUNDS;
    }

    private void applyDraft() {
        stopKeyComboCapture();
        draft.applyTo(SoundboardConfig.get());
        SoundboardConfig.save();
        SoundManager.applyConfigToSounds(SoundboardConfig.get());
        GlobalKeybinds.reloadAll();
        clearFocusNextTick = true;
    }

    private void updateSoundSearch(String query) {
        soundSearch = query;
        populateSoundList();
    }

    private void populateSoundList() {
        if (configList == null) {
            return;
        }
        configList.populateSounds(
                draft,
                SoundboardConfig.get(),
                SoundManager.getAllSounds(),
                soundSearch
        );
    }

    private void finishKeyComboCapture(KeyCombo combo) {
        Consumer<KeyCombo> callback = capturingCallback;
        Button button = capturingButton;
        stopKeyComboCapture();
        callback.accept(combo);
        button.setMessage(Component.literal(combo.toString()));
    }

    private void stopKeyComboCapture() {
        if (capturingButton != null && capturingCombo != null) {
            capturingButton.setMessage(Component.literal(capturingCombo.toString()));
        }
        capturingButton = null;
        capturingCombo = null;
        capturingCallback = null;
        capturedKeys.clear();
    }

    private void updateCaptureButtonMessage() {
        KeyCombo preview = capturedKeys.isEmpty()
                ? capturingCombo
                : KeyCombo.of(
                        capturingCombo.getId(),
                        capturedKeys.stream().mapToInt(Integer::intValue).toArray()
                );
        capturingButton.setMessage(
                Component.literal("> ")
                        .withStyle(ChatFormatting.YELLOW)
                        .append(Component.literal(preview.toString())
                                .withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                        .append(Component.literal(" <").withStyle(ChatFormatting.YELLOW))
        );
    }

    private void closeToParent() {
        minecraft.gui.setScreen(parent);
    }

    private enum Tab {
        GENERAL,
        SOUNDS
    }
}
