package de.twyco.soundboard.gui.config.widget;

import de.twyco.soundboard.enums.GlobalKeyCombos;
import de.twyco.soundboard.gui.component.SoundboardUi;
import de.twyco.soundboard.gui.config.ConfigDraft;
import de.twyco.soundboard.gui.config.SoundboardConfigScreen;
import de.twyco.soundboard.gui.config.SoundboardConfigScreen.KeybindFilter;
import de.twyco.soundboard.gui.config.SoundboardConfigScreen.LoopFilter;
import de.twyco.soundboard.gui.config.SoundboardConfigScreen.SoundSort;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.Sound;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public final class SoundboardConfigList extends ContainerObjectSelectionList<SoundboardConfigList.ConfigRow> {

    private static final int DEFAULT_ROW_HEIGHT = 32;
    private static final int MAX_ROW_WIDTH = 680;

    private final SoundboardConfigScreen screen;
    private final Font font;

    public SoundboardConfigList(
            SoundboardConfigScreen screen,
            Minecraft minecraft,
            int width,
            int height,
            int y
    ) {
        super(minecraft, width, height, y, DEFAULT_ROW_HEIGHT);
        this.screen = screen;
        this.font = minecraft.font;
    }

    public void populateGeneral(ConfigDraft draft) {
        clearEntries();
        int contentWidth = getRowWidth() - 8;

        for (GlobalKeyCombos keybind : GlobalKeyCombos.values()) {
            addConfigRow(
                    new KeyComboRow(
                            font,
                            Component.translatable(keybind.getTranslationKey()),
                            draft.getGlobalKeyCombo(keybind.getId()),
                            draft::setGlobalKeyCombo,
                            screen
                    ),
                    contentWidth < 380 ? 56 : DEFAULT_ROW_HEIGHT
            );
        }
        CheckboxRow playWhileMuted = new CheckboxRow(
                font,
                Component.translatable("gui.soundboard.config.state.global.play_while_muted"),
                Component.translatable("gui.soundboard.config.state.global.play_while_muted.description"),
                draft.isPlayWhileMuted(),
                draft::setPlayWhileMuted,
                contentWidth
        );
        addConfigRow(playWhileMuted, playWhileMuted.getPreferredHeight());

        CheckboxRow showPlayingSoundsHud = new CheckboxRow(
                font,
                Component.translatable("gui.soundboard.config.state.global.show_sounds_in_hud"),
                null,
                draft.isShowPlayingSoundsHud(),
                draft::setShowPlayingSoundsHud,
                contentWidth
        );
        addConfigRow(showPlayingSoundsHud, showPlayingSoundsHud.getPreferredHeight());

        CheckboxRow toggleSoundWheel = new CheckboxRow(
                font,
                Component.translatable("gui.soundboard.config.state.global.toggle_sound_wheel"),
                Component.translatable("gui.soundboard.config.state.global.toggle_sound_wheel.description"),
                draft.isToggleSoundWheel(),
                draft::setToggleSoundWheel,
                contentWidth
        );
        addConfigRow(toggleSoundWheel, toggleSoundWheel.getPreferredHeight());

        CheckboxRow closeSoundWheelOnPlay = new CheckboxRow(
                font,
                Component.translatable("gui.soundboard.config.state.global.close_sound_wheel_on_play"),
                Component.translatable("gui.soundboard.config.state.global.close_sound_wheel_on_play.description"),
                draft.isCloseSoundWheelOnPlay(),
                draft::setCloseSoundWheelOnPlay,
                contentWidth
        );
        addConfigRow(closeSoundWheelOnPlay, closeSoundWheelOnPlay.getPreferredHeight());
        addConfigRow(
                new ActionRow(screen),
                contentWidth < 280 ? 56 : DEFAULT_ROW_HEIGHT
        );
    }

    public void populateSounds(
            ConfigDraft draft,
            SoundboardConfigData config,
            Collection<Sound> sounds,
            String searchQuery,
            SoundSort sort,
            KeybindFilter keybindFilter,
            LoopFilter loopFilter
    ) {
        clearEntries();
        int contentWidth = getRowWidth() - 8;
        String normalizedSearch = searchQuery.trim().toLowerCase(Locale.ROOT);
        Comparator<SoundRowData> comparator = Comparator.comparing(
                data -> data.sound().getName(),
                String.CASE_INSENSITIVE_ORDER
        );
        if (sort == SoundSort.NAME_DESCENDING) {
            comparator = comparator.reversed();
        }

        List<SoundRowData> sortedSounds = sounds.stream()
                .map(sound -> new SoundRowData(sound, draft.getSound(sound, config)))
                .filter(data -> normalizedSearch.isEmpty()
                        || data.sound().getName().toLowerCase(Locale.ROOT).contains(normalizedSearch))
                .filter(data -> matchesKeybindFilter(data, keybindFilter))
                .filter(data -> matchesLoopFilter(data, loopFilter))
                .sorted(comparator)
                .toList();

        if (sortedSounds.isEmpty()) {
            addConfigRow(
                    new TextRow(
                            font,
                            Component.translatable(
                                    sounds.isEmpty()
                                            ? "gui.soundboard.config.sounds.empty"
                                            : "gui.soundboard.config.filters.empty"
                            )
                    ),
                    40
            );
            return;
        }

        int rowHeight = contentWidth >= 560
                ? DEFAULT_ROW_HEIGHT
                : contentWidth >= 320 ? 56 : contentWidth >= 140 ? 80 : 104;
        for (SoundRowData data : sortedSounds) {
            addConfigRow(
                    new SoundRow(font, data.sound(), data.draft(), screen),
                    rowHeight
            );
        }
    }

    private static boolean matchesKeybindFilter(SoundRowData data, KeybindFilter filter) {
        boolean bound = !data.draft().getKeyCombo(data.sound().getId()).isEmpty();
        return switch (filter) {
            case ALL -> true;
            case BOUND -> bound;
            case UNBOUND -> !bound;
        };
    }

    private static boolean matchesLoopFilter(SoundRowData data, LoopFilter filter) {
        return switch (filter) {
            case ALL -> true;
            case ENABLED -> data.draft().isLoop();
            case DISABLED -> !data.draft().isLoop();
        };
    }

    @Override
    public int getRowWidth() {
        return Math.max(1, Math.min(getWidth() - 20, MAX_ROW_WIDTH));
    }

    @Override
    protected void extractListBackground(GuiGraphicsExtractor graphics) {
        graphics.fill(
                getX(),
                getY(),
                getX() + getWidth(),
                getY() + getHeight(),
                SoundboardUi.SURFACE_DARK
        );
    }

    private void addConfigRow(ConfigRow row, int height) {
        addEntry(row, height);
    }

    private record SoundRowData(Sound sound, ConfigDraft.SoundDraft draft) {
    }

    public abstract static class ConfigRow extends ContainerObjectSelectionList.Entry<ConfigRow> {

        private static final int HORIZONTAL_PADDING = 8;
        protected static final int VERTICAL_PADDING = 6;

        protected final List<AbstractWidget> widgets = new ArrayList<>();

        @Override
        public final void extractContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            if (shouldDrawRowBackground()) {
                SoundboardUi.drawListRow(
                        graphics,
                        getX() + 2,
                        getY() + 2,
                        Math.max(1, getWidth() - 4),
                        Math.max(1, getHeight() - 4),
                        hovered
                );
            }
            extractRowContent(graphics, mouseX, mouseY, hovered, delta);
        }

        protected boolean shouldDrawRowBackground() {
            return true;
        }

        protected abstract void extractRowContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        );

        @Override
        public List<? extends GuiEventListener> children() {
            return widgets;
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            return widgets;
        }

        protected void extractWidgets(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
            for (AbstractWidget widget : widgets) {
                widget.extractRenderState(graphics, mouseX, mouseY, delta);
            }
        }

        protected static void setBounds(AbstractWidget widget, int x, int y, int width, int height) {
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(Math.max(1, width));
            widget.setHeight(Math.max(1, height));
        }

        protected int getPaddedX() {
            return getX() + HORIZONTAL_PADDING;
        }

        protected int getPaddedY() {
            return getY() + VERTICAL_PADDING;
        }

        protected int getPaddedWidth() {
            return Math.max(1, getWidth() - HORIZONTAL_PADDING * 2);
        }

        protected int getPaddedHeight() {
            return Math.max(1, getHeight() - VERTICAL_PADDING * 2);
        }

        protected int getPaddedXMiddle() {
            return getPaddedX() + getPaddedWidth() / 2;
        }

        protected int getPaddedYMiddle() {
            return getPaddedY() + getPaddedHeight() / 2;
        }

    }

    private static final class KeyComboRow extends ConfigRow {

        private final Font font;
        private final Component label;
        private final KeyComboButton button;

        private KeyComboRow(
                Font font,
                Component label,
                KeyCombo combo,
                Consumer<KeyCombo> onChange,
                SoundboardConfigScreen screen
        ) {
            this.font = font;
            this.label = label;
            this.button = new KeyComboButton(screen, combo, onChange, label);
            widgets.add(button);
        }

        @Override
        protected void extractRowContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            int x = getPaddedX();
            int y = getPaddedY();
            int width = getPaddedWidth();

            if (width >= 380) {
                int buttonWidth = Math.min(180, Math.max(110, width / 3));
                setBounds(button, x + width - buttonWidth, y, buttonWidth, 20);
                graphics.text(
                        font,
                        label,
                        x,
                        getPaddedYMiddle() - font.lineHeight / 2,
                        SoundboardUi.TEXT_PRIMARY
                );
            } else {
                graphics.text(font, label, x, y, SoundboardUi.TEXT_PRIMARY);
                setBounds(button, x, y + 18, width, 20);
            }
            extractWidgets(graphics, mouseX, mouseY, delta);
        }
    }

    private static final class CheckboxRow extends ConfigRow {

        private final Checkbox checkbox;

        private CheckboxRow(
                Font font,
                Component label,
                Component tooltip,
                boolean selected,
                Consumer<Boolean> onChange,
                int maxWidth
        ) {
            Checkbox.Builder builder = Checkbox.builder(label, font)
                    .selected(selected)
                    .maxWidth(Math.max(1, maxWidth))
                    .onValueChange((_, value) -> onChange.accept(value));
            if (tooltip != null) {
                builder.tooltip(Tooltip.create(tooltip));
            }
            this.checkbox = builder.build();
            widgets.add(checkbox);
        }

        private int getPreferredHeight() {
            return Math.max(DEFAULT_ROW_HEIGHT, checkbox.getHeight() + VERTICAL_PADDING * 2);
        }

        @Override
        protected void extractRowContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            checkbox.setX(getPaddedX());
            checkbox.setY(getPaddedYMiddle() - checkbox.getHeight() / 2);
            extractWidgets(graphics, mouseX, mouseY, delta);
        }
    }

    private static final class ActionRow extends ConfigRow {

        private final Button openFolder;
        private final Button reload;

        private ActionRow(SoundboardConfigScreen screen) {
            openFolder = Button.builder(
                            Component.translatable("gui.soundboard.config.action.open_sounds_folder"),
                            _ -> screen.openSoundsFolder()
                    )
                    .build();
            reload = Button.builder(
                            Component.translatable("gui.soundboard.config.action.reload"),
                            _ -> screen.reloadSoundFiles()
                    )
                    .build();
            widgets.add(openFolder);
            widgets.add(reload);
        }

        @Override
        protected boolean shouldDrawRowBackground() {
            return false;
        }

        @Override
        protected void extractRowContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            int x = getPaddedX();
            int y = getPaddedY();
            int width = getPaddedWidth();
            if (width >= 280) {
                int buttonWidth = (width - 4) / 2;
                setBounds(openFolder, x, y, buttonWidth, 20);
                setBounds(reload, x + buttonWidth + 4, y, buttonWidth, 20);
            } else {
                setBounds(openFolder, x, y, width, 20);
                setBounds(reload, x, y + 24, width, 20);
            }
            extractWidgets(graphics, mouseX, mouseY, delta);
        }
    }

    private static final class TextRow extends ConfigRow {

        private final Font font;
        private final Component text;

        private TextRow(Font font, Component text) {
            this.font = font;
            this.text = text;
        }

        @Override
        protected void extractRowContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            graphics.centeredText(
                    font,
                    SoundboardUi.fitText(font, text.getString(), getPaddedWidth()),
                    getPaddedXMiddle(),
                    getPaddedYMiddle() - font.lineHeight / 2,
                    SoundboardUi.TEXT_SECONDARY
            );
        }
    }

    private static final class SoundRow extends ConfigRow {

        private static final int GAP = 6;

        private final Font font;
        private final Sound sound;
        private final KeyComboButton keyComboButton;
        private final Checkbox loopCheckbox;
        private final AmplifierSlider amplifierSlider;

        private SoundRow(
                Font font,
                Sound sound,
                ConfigDraft.SoundDraft draft,
                SoundboardConfigScreen screen
        ) {
            this.font = font;
            this.sound = sound;

            KeyCombo combo = draft.getKeyCombo(sound.getId());
            keyComboButton = new KeyComboButton(
                    screen,
                    combo,
                    newCombo -> {
                        draft.setKeyCombo(newCombo);
                        screen.requestKeybindFilterRefresh();
                    },
                    Component.translatable(
                            "gui.soundboard.config.keybind.sound.tooltip",
                            sound.getName()
                    )
            );
            loopCheckbox = Checkbox.builder(
                            Component.translatable("gui.soundboard.config.sound.loop.short"),
                            font
                    )
                    .selected(draft.isLoop())
                    .onValueChange((_, value) -> {
                        draft.setLoop(value);
                        screen.requestLoopFilterRefresh();
                    })
                    .build();
            amplifierSlider = new AmplifierSlider(draft.getAmplifier(), draft::setAmplifier);
            amplifierSlider.setTooltip(Tooltip.create(
                    Component.translatable("gui.soundboard.config.sound.amplifier")
            ));

            widgets.add(keyComboButton);
            widgets.add(loopCheckbox);
            widgets.add(amplifierSlider);
        }

        @Override
        protected void extractRowContent(
                GuiGraphicsExtractor graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            int x = getPaddedX();
            int y = getPaddedY();
            int width = getPaddedWidth();

            if (width >= 560) {
                extractWide(graphics, x, y, width);
            } else if (width >= 320) {
                extractCompact(graphics, x, y, width);
            } else if (width >= 140) {
                extractNarrow(graphics, x, y, width);
            } else {
                extractUltraNarrow(graphics, x, y, width);
            }
            extractWidgets(graphics, mouseX, mouseY, delta);
        }

        private void extractWide(GuiGraphicsExtractor graphics, int x, int y, int width) {
            int sliderWidth = 110;
            int sliderX = x + width - sliderWidth;
            int checkboxWidth = loopCheckbox.getWidth();
            int checkboxX = sliderX - GAP - checkboxWidth;
            int comboWidth = 150;
            int comboX = checkboxX - GAP - comboWidth;
            int textWidth = Math.max(0, comboX - GAP - x);

            graphics.text(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), textWidth),
                    x,
                    getPaddedYMiddle() - font.lineHeight / 2,
                    SoundboardUi.TEXT_PRIMARY
            );
            setBounds(keyComboButton, comboX, y, comboWidth, 20);
            loopCheckbox.setX(checkboxX);
            loopCheckbox.setY(y + 1);
            setBounds(amplifierSlider, sliderX, y, sliderWidth, 20);
        }

        private void extractCompact(GuiGraphicsExtractor graphics, int x, int y, int width) {
            graphics.text(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), width),
                    x,
                    y,
                    SoundboardUi.TEXT_PRIMARY
            );

            int controlsY = y + 20;
            int sliderWidth = Math.max(82, width / 4);
            int sliderX = x + width - sliderWidth;
            int checkboxWidth = loopCheckbox.getWidth();
            int checkboxX = sliderX - GAP - checkboxWidth;
            int comboWidth = Math.max(80, checkboxX - GAP - x);

            setBounds(keyComboButton, x, controlsY, comboWidth, 20);
            loopCheckbox.setX(checkboxX);
            loopCheckbox.setY(controlsY + 1);
            setBounds(amplifierSlider, sliderX, controlsY, sliderWidth, 20);
        }

        private void extractNarrow(GuiGraphicsExtractor graphics, int x, int y, int width) {
            graphics.text(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), width),
                    x,
                    y,
                    SoundboardUi.TEXT_PRIMARY
            );
            setBounds(keyComboButton, x, y + 18, width, 20);

            int controlsY = y + 44;
            int sliderWidth = Math.max(70, width - loopCheckbox.getWidth() - GAP);
            loopCheckbox.setX(x);
            loopCheckbox.setY(controlsY + 1);
            setBounds(
                    amplifierSlider,
                    x + width - sliderWidth,
                    controlsY,
                    sliderWidth,
                    20
            );
        }

        private void extractUltraNarrow(GuiGraphicsExtractor graphics, int x, int y, int width) {
            graphics.text(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), width),
                    x,
                    y,
                    SoundboardUi.TEXT_PRIMARY
            );
            setBounds(keyComboButton, x, y + 18, width, 20);
            loopCheckbox.setX(x);
            loopCheckbox.setY(y + 44);
            setBounds(amplifierSlider, x, y + 68, width, 20);
        }
    }
}
