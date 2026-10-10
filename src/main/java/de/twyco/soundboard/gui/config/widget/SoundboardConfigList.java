package de.twyco.soundboard.gui.config.widget;

import de.twyco.soundboard.enums.GlobalKeyCombos;
import de.twyco.soundboard.gui.component.SoundboardUi;
import de.twyco.soundboard.gui.config.ConfigDraft;
import de.twyco.soundboard.gui.config.SoundboardConfigScreen;
import de.twyco.soundboard.gui.config.SoundboardConfigScreen.FavoriteFilter;
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
import java.util.function.IntConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Checkbox;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

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

        addConfigRow(new SectionHeaderRow(
                font,
                Component.translatable("gui.soundboard.config.sections.general"),
                draft.isGeneralCategoryExpanded(),
                () -> {
                    draft.setGeneralCategoryExpanded(!draft.isGeneralCategoryExpanded());
                    screen.requestGeneralListRefresh();
                }
        ), DEFAULT_ROW_HEIGHT);
        if (draft.isGeneralCategoryExpanded()) {
            addKeyComboRow(draft, GlobalKeyCombos.SOUND_STOP_ALL, contentWidth);

            addConfigRow(
                    new AmplifierRow(
                            font,
                            Component.translatable("gui.soundboard.config.state.global.sound_amplifier"),
                            Component.translatable("gui.soundboard.config.state.global.sound_amplifier.description"),
                            draft.getSoundAmplifier(),
                            draft::setSoundAmplifier
                    ),
                    contentWidth < 388 ? 56 : DEFAULT_ROW_HEIGHT
            );

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
            addConfigRow(
                    new ActionRow(screen),
                    contentWidth < 280 ? 56 : DEFAULT_ROW_HEIGHT
            );
        }

        addConfigRow(new SectionHeaderRow(
                font,
                Component.translatable("gui.soundboard.config.sections.sound_wheel"),
                draft.isSoundWheelCategoryExpanded(),
                () -> {
                    draft.setSoundWheelCategoryExpanded(!draft.isSoundWheelCategoryExpanded());
                    screen.requestGeneralListRefresh();
                }
        ), DEFAULT_ROW_HEIGHT);
        if (draft.isSoundWheelCategoryExpanded()) {
            addKeyComboRow(draft, GlobalKeyCombos.SOUND_WHEEL, contentWidth);

            CheckboxRow toggleSoundWheel = new CheckboxRow(
                    font,
                    Component.translatable("gui.soundboard.config.state.global.toggle_sound_wheel"),
                    Component.translatable("gui.soundboard.config.state.global.toggle_sound_wheel.description"),
                    draft.isToggleSoundWheel(),
                    value -> {
                        draft.setToggleSoundWheel(value);
                        screen.requestGeneralListRefresh();
                    },
                    contentWidth
            );
            addConfigRow(toggleSoundWheel, toggleSoundWheel.getPreferredHeight());

            if (draft.isToggleSoundWheel()) {
                CheckboxRow closeSoundWheelOnPlay = new CheckboxRow(
                        font,
                        Component.translatable("gui.soundboard.config.state.global.close_sound_wheel_on_play"),
                        Component.translatable("gui.soundboard.config.state.global.close_sound_wheel_on_play.description"),
                        draft.isCloseSoundWheelOnPlay(),
                        draft::setCloseSoundWheelOnPlay,
                        contentWidth
                );
                addConfigRow(closeSoundWheelOnPlay, closeSoundWheelOnPlay.getPreferredHeight());
            }
        }
    }

    private void addKeyComboRow(ConfigDraft draft, GlobalKeyCombos keybind, int contentWidth) {
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

    public void populateSounds(
            ConfigDraft draft,
            SoundboardConfigData config,
            Collection<Sound> sounds,
            String searchQuery,
            SoundSort sort,
            FavoriteFilter favoriteFilter,
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
                .filter(data -> matchesFavoriteFilter(data, favoriteFilter))
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

        int layoutWidth = Math.max(1, contentWidth - 8);
        int rowHeight = layoutWidth >= 560
                ? DEFAULT_ROW_HEIGHT
                : layoutWidth >= 320 ? 56 : layoutWidth >= 140 ? 80 : 104;
        for (SoundRowData data : sortedSounds) {
            addConfigRow(
                    new SoundRow(font, data.sound(), data.draft(), screen),
                    rowHeight
            );
        }
    }

    private static boolean matchesFavoriteFilter(SoundRowData data, FavoriteFilter filter) {
        return switch (filter) {
            case ALL -> true;
            case FAVORITES -> data.draft().isFavorite();
            case NOT_FAVORITES -> !data.draft().isFavorite();
        };
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
    protected void renderListBackground(GuiGraphics graphics) {
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
        public final void renderContent(
                GuiGraphics graphics,
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
            renderRowContent(graphics, mouseX, mouseY, hovered, delta);
        }

        protected boolean shouldDrawRowBackground() {
            return true;
        }

        protected abstract void renderRowContent(
                GuiGraphics graphics,
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

        protected void renderWidgets(GuiGraphics graphics, int mouseX, int mouseY, float delta) {
            for (AbstractWidget widget : widgets) {
                widget.render(graphics, mouseX, mouseY, delta);
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

    private static final class SectionHeaderRow extends ConfigRow {

        private static final int BUTTON_SIZE = 20;
        private static final int LINE_GAP = 6;

        private final Font font;
        private final Component title;
        private final Runnable onToggle;
        private final Button toggleButton;

        private SectionHeaderRow(Font font, Component title, boolean expanded, Runnable onToggle) {
            this.font = font;
            this.title = title;
            this.onToggle = onToggle;
            toggleButton = Button.builder(
                            Component.literal(expanded ? "-" : "+"),
                            button -> onToggle.run()
                    )
                    .build();
            widgets.add(toggleButton);
        }

        @Override
        protected boolean shouldDrawRowBackground() {
            return false;
        }

        @Override
        public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
            if (super.mouseClicked(event, doubleClick)) {
                return true;
            }
            if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT
                    || event.x() < getPaddedX() + BUTTON_SIZE + LINE_GAP
                    || event.x() >= getPaddedX() + getPaddedWidth()
                    || event.y() < getY()
                    || event.y() >= getY() + getHeight()) {
                return false;
            }
            onToggle.run();
            return true;
        }

        @Override
        protected void renderRowContent(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            int x = getPaddedX();
            int y = getPaddedYMiddle();
            int width = getPaddedWidth();
            int centerX = x + width / 2;
            String visibleTitle = SoundboardUi.fitText(
                    font,
                    title.getString(),
                    Math.max(1, width - BUTTON_SIZE * 2 - LINE_GAP * 4)
            );
            int halfTitleWidth = font.width(visibleTitle) / 2;

            setBounds(
                    toggleButton,
                    x,
                    getPaddedYMiddle() - BUTTON_SIZE / 2,
                    BUTTON_SIZE,
                    BUTTON_SIZE
            );
            drawLine(graphics, x + BUTTON_SIZE + LINE_GAP, centerX - halfTitleWidth - LINE_GAP, y);
            drawLine(graphics, centerX + halfTitleWidth + LINE_GAP, x + width, y);
            graphics.drawCenteredString(
                    font,
                    visibleTitle,
                    centerX,
                    y - font.lineHeight / 2,
                    SoundboardUi.TEXT_PRIMARY
            );
            renderWidgets(graphics, mouseX, mouseY, delta);
        }

        private static void drawLine(GuiGraphics graphics, int startX, int endX, int y) {
            if (endX > startX) {
                graphics.fill(startX, y, endX, y + 1, SoundboardUi.BORDER_MID);
            }
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
        protected void renderRowContent(
                GuiGraphics graphics,
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
                graphics.drawString(
                        font,
                        label,
                        x,
                        getPaddedYMiddle() - font.lineHeight / 2,
                        SoundboardUi.TEXT_PRIMARY
                );
            } else {
                graphics.drawString(font, label, x, y, SoundboardUi.TEXT_PRIMARY);
                setBounds(button, x, y + 18, width, 20);
            }
            renderWidgets(graphics, mouseX, mouseY, delta);
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
                    .onValueChange((checkbox, value) -> onChange.accept(value));
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
        protected void renderRowContent(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            checkbox.setX(getPaddedX());
            checkbox.setY(getPaddedYMiddle() - checkbox.getHeight() / 2);
            renderWidgets(graphics, mouseX, mouseY, delta);
        }
    }

    private static final class AmplifierRow extends ConfigRow {

        private final Font font;
        private final Component label;
        private final AmplifierSlider slider;

        private AmplifierRow(
                Font font,
                Component label,
                Component tooltip,
                int amplifier,
                IntConsumer onChange
        ) {
            this.font = font;
            this.label = label;
            slider = new AmplifierSlider(amplifier, onChange);
            slider.setTooltip(Tooltip.create(tooltip));
            widgets.add(slider);
        }

        @Override
        protected void renderRowContent(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            int x = getPaddedX();
            int y = getPaddedY();
            int width = getPaddedWidth();

            if (width >= 380) {
                int sliderWidth = Math.min(180, Math.max(110, width / 3));
                setBounds(slider, x + width - sliderWidth, y, sliderWidth, 20);
                graphics.drawString(
                        font,
                        SoundboardUi.fitText(font, label.getString(), width - sliderWidth - 6),
                        x,
                        getPaddedYMiddle() - font.lineHeight / 2,
                        SoundboardUi.TEXT_PRIMARY
                );
            } else {
                graphics.drawString(font, label, x, y, SoundboardUi.TEXT_PRIMARY);
                setBounds(slider, x, y + 18, width, 20);
            }
            renderWidgets(graphics, mouseX, mouseY, delta);
        }
    }

    private static final class ActionRow extends ConfigRow {

        private final Button openFolder;
        private final Button reload;

        private ActionRow(SoundboardConfigScreen screen) {
            openFolder = Button.builder(
                            Component.translatable("gui.soundboard.config.action.open_sounds_folder"),
                            button -> screen.openSoundsFolder()
                    )
                    .build();
            reload = Button.builder(
                            Component.translatable("gui.soundboard.config.action.reload"),
                            button -> screen.reloadSoundFiles()
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
        protected void renderRowContent(
                GuiGraphics graphics,
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
            renderWidgets(graphics, mouseX, mouseY, delta);
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
        protected void renderRowContent(
                GuiGraphics graphics,
                int mouseX,
                int mouseY,
                boolean hovered,
                float delta
        ) {
            graphics.drawCenteredString(
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
        private final FavoriteButton favoriteButton;
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
            favoriteButton = new FavoriteButton(
                    font,
                    draft.isFavorite(),
                    value -> {
                        draft.setFavorite(value);
                        screen.requestFavoriteFilterRefresh();
                    }
            );
            loopCheckbox = Checkbox.builder(
                            Component.translatable("gui.soundboard.config.sound.loop.short"),
                            font
                    )
                    .selected(draft.isLoop())
                    .onValueChange((checkbox, value) -> {
                        draft.setLoop(value);
                        screen.requestLoopFilterRefresh();
                    })
                    .build();
            amplifierSlider = new AmplifierSlider(draft.getAmplifier(), draft::setAmplifier);
            amplifierSlider.setTooltip(Tooltip.create(
                    Component.translatable("gui.soundboard.config.sound.amplifier")
            ));

            widgets.add(keyComboButton);
            widgets.add(favoriteButton);
            widgets.add(loopCheckbox);
            widgets.add(amplifierSlider);
        }

        @Override
        protected void renderRowContent(
                GuiGraphics graphics,
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
            renderWidgets(graphics, mouseX, mouseY, delta);
        }

        private void extractWide(GuiGraphics graphics, int x, int y, int width) {
            int sliderWidth = 100;
            int sliderX = x + width - sliderWidth;
            int checkboxWidth = loopCheckbox.getWidth();
            int checkboxX = sliderX - GAP - checkboxWidth;
            int comboWidth = 140;
            int comboX = checkboxX - GAP - comboWidth;
            int nameX = x + FavoriteButton.SIZE + GAP;
            int textWidth = Math.max(0, comboX - GAP - nameX);

            setBounds(favoriteButton, x, y + 2, FavoriteButton.SIZE, FavoriteButton.SIZE);
            graphics.drawString(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), textWidth),
                    nameX,
                    getPaddedYMiddle() - font.lineHeight / 2,
                    SoundboardUi.TEXT_PRIMARY
            );
            setBounds(keyComboButton, comboX, y, comboWidth, 20);
            loopCheckbox.setX(checkboxX);
            loopCheckbox.setY(y + 1);
            setBounds(amplifierSlider, sliderX, y, sliderWidth, 20);
        }

        private void extractCompact(GuiGraphics graphics, int x, int y, int width) {
            int nameX = x + FavoriteButton.SIZE + GAP;
            setBounds(favoriteButton, x, y, FavoriteButton.SIZE, FavoriteButton.SIZE);
            graphics.drawString(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), width - FavoriteButton.SIZE - GAP),
                    nameX,
                    y + (FavoriteButton.SIZE - font.lineHeight) / 2,
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

        private void extractNarrow(GuiGraphics graphics, int x, int y, int width) {
            int nameX = x + FavoriteButton.SIZE + GAP;
            setBounds(favoriteButton, x, y, FavoriteButton.SIZE, FavoriteButton.SIZE);
            graphics.drawString(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), width - FavoriteButton.SIZE - GAP),
                    nameX,
                    y + (FavoriteButton.SIZE - font.lineHeight) / 2,
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

        private void extractUltraNarrow(GuiGraphics graphics, int x, int y, int width) {
            int nameX = x + FavoriteButton.SIZE + GAP;
            setBounds(favoriteButton, x, y, FavoriteButton.SIZE, FavoriteButton.SIZE);
            graphics.drawString(
                    font,
                    SoundboardUi.fitText(font, sound.getName(), width - FavoriteButton.SIZE - GAP),
                    nameX,
                    y + (FavoriteButton.SIZE - font.lineHeight) / 2,
                    SoundboardUi.TEXT_PRIMARY
            );
            setBounds(keyComboButton, x, y + 18, width, 20);
            loopCheckbox.setX(x);
            loopCheckbox.setY(y + 44);
            setBounds(amplifierSlider, x, y + 68, width, 20);
        }
    }
}
