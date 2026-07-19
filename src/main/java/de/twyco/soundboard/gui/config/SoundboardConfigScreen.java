package de.twyco.soundboard.gui.config;

import de.twyco.soundboard.client.GlobalKeybinds;
import de.twyco.soundboard.gui.component.SoundboardUi;
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
import net.minecraft.client.gui.components.CycleButton;
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
    private SoundSort soundSort = SoundSort.NAME_ASCENDING;
    private KeybindFilter keybindFilter = KeybindFilter.ALL;
    private LoopFilter loopFilter = LoopFilter.ALL;
    private int pageX;
    private int pageWidth;
    private int listY;
    private boolean clearFocusNextTick;
    private boolean refreshSoundListNextTick;
    private boolean refreshGeneralListNextTick;

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

        pageWidth = Math.min(PAGE_MAX_WIDTH, Math.max(1, width - PAGE_MARGIN * 2));
        pageX = (width - pageWidth) / 2;
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

        listY = HEADER_HEIGHT;
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
            listY += addSoundListControls(pageX, listY, pageWidth);
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
        SoundboardUi.drawRaisedPanel(
                graphics,
                pageX - 6,
                4,
                pageWidth + 12,
                height - 8,
                SoundboardUi.SURFACE
        );
        super.extractRenderState(graphics, mouseX, mouseY, delta);
        graphics.centeredText(font, title, width / 2, 10, SoundboardUi.TEXT_PRIMARY);
    }

    @Override
    public void tick() {
        super.tick();
        if (clearFocusNextTick) {
            setFocused(null);
            clearFocusNextTick = false;
        }
        if (refreshSoundListNextTick) {
            populateSoundList();
            refreshSoundListNextTick = false;
        }
        if (refreshGeneralListNextTick) {
            configList.populateGeneral(draft);
            refreshGeneralListNextTick = false;
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

    public void requestKeybindFilterRefresh() {
        refreshSoundListNextTick |= keybindFilter != KeybindFilter.ALL;
    }

    public void requestLoopFilterRefresh() {
        refreshSoundListNextTick |= loopFilter != LoopFilter.ALL;
    }

    public void requestGeneralListRefresh() {
        refreshGeneralListNextTick = true;
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
                soundSearch,
                soundSort,
                keybindFilter,
                loopFilter
        );
    }

    private int addSoundListControls(int x, int y, int width) {
        if (width >= 300) {
            int controlWidth = Math.max(1, (width - GAP * 2) / 3);
            addSortButton(x, y, controlWidth);
            addKeybindFilterButton(x + controlWidth + GAP, y, controlWidth);
            addLoopFilterButton(x + (controlWidth + GAP) * 2, y, controlWidth);
            return BUTTON_HEIGHT + GAP;
        }

        addSortButton(x, y, width);
        int secondRowY = y + BUTTON_HEIGHT + GAP;
        int controlWidth = Math.max(1, (width - GAP) / 2);
        addKeybindFilterButton(x, secondRowY, controlWidth);
        addLoopFilterButton(x + controlWidth + GAP, secondRowY, controlWidth);
        return (BUTTON_HEIGHT + GAP) * 2;
    }

    private void addSortButton(int x, int y, int width) {
        addRenderableWidget(
                CycleButton.builder(SoundSort::getLabel, soundSort)
                        .withValues(SoundSort.values())
                        .create(
                                x,
                                y,
                                width,
                                BUTTON_HEIGHT,
                                Component.translatable("gui.soundboard.config.sort"),
                                (_, value) -> {
                                    soundSort = value;
                                    populateSoundList();
                                    clearFocusNextTick = true;
                                }
                        )
        );
    }

    private void addKeybindFilterButton(int x, int y, int width) {
        addRenderableWidget(
                CycleButton.builder(KeybindFilter::getLabel, keybindFilter)
                        .withValues(KeybindFilter.values())
                        .create(
                                x,
                                y,
                                width,
                                BUTTON_HEIGHT,
                                Component.translatable("gui.soundboard.config.filter.keybind"),
                                (_, value) -> {
                                    keybindFilter = value;
                                    populateSoundList();
                                    clearFocusNextTick = true;
                                }
                        )
        );
    }

    private void addLoopFilterButton(int x, int y, int width) {
        addRenderableWidget(
                CycleButton.builder(LoopFilter::getLabel, loopFilter)
                        .withValues(LoopFilter.values())
                        .create(
                                x,
                                y,
                                width,
                                BUTTON_HEIGHT,
                                Component.translatable("gui.soundboard.config.filter.loop"),
                                (_, value) -> {
                                    loopFilter = value;
                                    populateSoundList();
                                    clearFocusNextTick = true;
                                }
                        )
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
        minecraft.setScreen(parent);
    }

    private enum Tab {
        GENERAL,
        SOUNDS
    }

    public enum SoundSort {
        NAME_ASCENDING("gui.soundboard.config.sort.name_ascending"),
        NAME_DESCENDING("gui.soundboard.config.sort.name_descending");

        private final String translationKey;

        SoundSort(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getLabel() {
            return Component.translatable(translationKey);
        }
    }

    public enum KeybindFilter {
        ALL("gui.soundboard.config.filter.all"),
        BOUND("gui.soundboard.config.filter.keybind.bound"),
        UNBOUND("gui.soundboard.config.filter.keybind.unbound");

        private final String translationKey;

        KeybindFilter(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getLabel() {
            return Component.translatable(translationKey);
        }
    }

    public enum LoopFilter {
        ALL("gui.soundboard.config.filter.all"),
        ENABLED("gui.soundboard.config.filter.loop.enabled"),
        DISABLED("gui.soundboard.config.filter.loop.disabled");

        private final String translationKey;

        LoopFilter(String translationKey) {
            this.translationKey = translationKey;
        }

        public Component getLabel() {
            return Component.translatable(translationKey);
        }
    }
}
