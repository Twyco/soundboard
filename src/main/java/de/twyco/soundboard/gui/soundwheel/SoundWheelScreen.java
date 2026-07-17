package de.twyco.soundboard.gui.soundwheel;

import com.mojang.blaze3d.platform.InputConstants;
import de.twyco.soundboard.gui.component.SoundboardUi;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.Sound;
import de.twyco.soundboard.util.sound.SoundManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public final class SoundWheelScreen extends Screen {

    private static final int SOUNDS_PER_PAGE = 6;
    private static final int MAX_OUTER_RADIUS = 120;
    private static final int MIN_OUTER_RADIUS = 54;
    private static final int PAGE_BUTTON_GAP = 8;
    private static final double FULL_CIRCLE = Math.PI * 2.0D;
    private static final double SECTOR_ANGLE = FULL_CIRCLE / SOUNDS_PER_PAGE;

    private final Screen parent;
    private final KeyCombo activationCombo;
    private final List<Sound> sounds;
    private final boolean toggleMode;
    private final boolean closeOnPlay;
    private final List<List<Span>> sectorSpans = new ArrayList<>(SOUNDS_PER_PAGE);
    private final Set<KeyMapping> forwardedMovementKeys = new HashSet<>();

    private int centerX;
    private int centerY;
    private int outerRadius;
    private int innerRadius;
    private int page;
    private int selectedSector = -1;
    private int age;
    private boolean closed;
    private boolean toggleComboReleased;

    private SoundWheelScreen(
            Screen parent,
            KeyCombo activationCombo,
            List<Sound> sounds,
            boolean toggleMode,
            boolean closeOnPlay
    ) {
        super(Component.translatable("gui.soundboard.wheel.title"));
        this.parent = parent;
        this.activationCombo = activationCombo;
        this.sounds = sounds;
        this.toggleMode = toggleMode;
        this.closeOnPlay = closeOnPlay;
    }

    public static void open(KeyCombo activationCombo) {
        Minecraft client = Minecraft.getInstance();
        if (client.screen != null) {
            return;
        }

        List<Sound> sortedSounds = SoundManager.getAllSounds().stream()
                .sorted(Comparator.comparing(Sound::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        client.setScreen(new SoundWheelScreen(
                client.screen,
                activationCombo,
                sortedSounds,
                SoundboardConfig.get().globalState.toggleSoundWheel,
                SoundboardConfig.get().globalState.closeSoundWheelOnPlay
        ));
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
        int availableRadius = Math.min(width, height) / 2 - 34;
        outerRadius = Math.max(MIN_OUTER_RADIUS, Math.min(MAX_OUTER_RADIUS, availableRadius));
        innerRadius = Math.max(28, (int) Math.round(outerRadius * 0.4D));
        buildSectorSpans();
        page = Math.min(page, getPageCount() - 1);
        updateMovementKeyStates();
    }

    @Override
    public void extractRenderState(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        if (isOverVisiblePageButton(mouseX, mouseY)) {
            selectedSector = -1;
        } else {
            updateSelection(mouseX, mouseY);
        }
        Set<String> playingSoundIds = SimpleVoicechatService.getCurrentlyPlayingSoundIds();

        SoundboardUi.drawCircle(
                graphics,
                centerX + 2,
                centerY + 2,
                outerRadius + 4,
                SoundboardUi.SHADOW
        );
        SoundboardUi.drawCircle(
                graphics,
                centerX,
                centerY,
                outerRadius + 3,
                SoundboardUi.BORDER_DARK
        );
        SoundboardUi.drawCircle(
                graphics,
                centerX,
                centerY,
                outerRadius + 2,
                SoundboardUi.BORDER_LIGHT
        );
        SoundboardUi.drawCircle(
                graphics,
                centerX,
                centerY,
                outerRadius,
                SoundboardUi.SURFACE
        );
        drawPlayingSectors(graphics, playingSoundIds);
        if (selectedSector >= 0) {
            Sound selectedSound = getSelectedSound();
            boolean selectedSoundPlaying = selectedSound != null
                    && playingSoundIds.contains(selectedSound.getId());
            drawSector(
                    graphics,
                    selectedSector,
                    selectedSoundPlaying
                            ? SoundboardUi.PLAYING_HOVERED
                            : SoundboardUi.SURFACE_SELECTED
            );
        }
        drawDividers(graphics);
        SoundboardUi.drawCircle(
                graphics,
                centerX,
                centerY,
                innerRadius + 3,
                SoundboardUi.BORDER_DARK
        );
        SoundboardUi.drawCircle(
                graphics,
                centerX,
                centerY,
                innerRadius + 1,
                SoundboardUi.BORDER_LIGHT
        );
        SoundboardUi.drawCircle(
                graphics,
                centerX,
                centerY,
                innerRadius,
                SoundboardUi.SURFACE_DARK
        );
        drawLabels(graphics, playingSoundIds);

        int titleWidth = font.width(title) + 12;
        int titleY = centerY - outerRadius - 28;
        SoundboardUi.drawRaisedPanel(
                graphics,
                centerX - titleWidth / 2,
                titleY,
                titleWidth,
                18,
                SoundboardUi.SURFACE
        );
        graphics.centeredText(
                font,
                title,
                centerX,
                titleY + 5,
                SoundboardUi.TEXT_PRIMARY
        );
        graphics.centeredText(
                font,
                Component.translatable("gui.soundboard.wheel.page", page + 1, getPageCount()),
                centerX,
                centerY - font.lineHeight / 2,
                SoundboardUi.TEXT_PRIMARY
        );
        drawPageControls(graphics, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        updateMovementKeyStates();
        if (!toggleMode && age > 1 && !activationCombo.allKeysPressed()) {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (activationCombo.getKeyCodes().contains(event.key())) {
            if (toggleMode && toggleComboReleased && activationCombo.allKeysPressed()) {
                onClose();
            }
            return true;
        }
        if (event.isEscape()) {
            onClose();
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_LEFT) {
            changePage(-1);
            return true;
        }
        if (event.key() == GLFW.GLFW_KEY_RIGHT) {
            changePage(1);
            return true;
        }
        if (forwardMovementKeyEvent(event, true)) {
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (activationCombo.getKeyCodes().contains(event.key())) {
            if (toggleMode) {
                toggleComboReleased = true;
            } else {
                onClose();
            }
            return true;
        }
        if (forwardMovementKeyEvent(event, false)) {
            return true;
        }
        return super.keyReleased(event);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() != GLFW.GLFW_MOUSE_BUTTON_LEFT) {
            return super.mouseClicked(event, doubleClick);
        }

        if (hasPreviousPage() && isInsidePreviousPageButton(event.x(), event.y())) {
            changePage(-1);
            return true;
        }
        if (hasNextPage() && isInsideNextPageButton(event.x(), event.y())) {
            changePage(1);
            return true;
        }

        updateSelection((int) event.x(), (int) event.y());
        Sound selectedSound = getSelectedSound();
        if (selectedSound == null) {
            return false;
        }

        selectedSound.play();
        if (closeOnPlay) {
            onClose();
        }
        return true;
    }

    @Override
    public boolean mouseScrolled(
            double mouseX,
            double mouseY,
            double horizontalAmount,
            double verticalAmount
    ) {
        if (verticalAmount == 0.0D) {
            return false;
        }
        changePage(verticalAmount > 0.0D ? -1 : 1);
        return true;
    }

    @Override
    public void onClose() {
        if (closed) {
            return;
        }
        closed = true;
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean isInGameUi() {
        return true;
    }

    private void changePage(int direction) {
        int pageCount = getPageCount();
        page = Math.floorMod(page + direction, pageCount);
        selectedSector = -1;
    }

    private void updateMovementKeyStates() {
        updateMovementKeyState(minecraft.options.keyUp);
        updateMovementKeyState(minecraft.options.keyDown);
        updateMovementKeyState(minecraft.options.keyLeft);
        updateMovementKeyState(minecraft.options.keyRight);
        updateMovementKeyState(minecraft.options.keyJump);
        updateMovementKeyState(minecraft.options.keyShift);
        updateMovementKeyState(minecraft.options.keySprint);
    }

    private void updateMovementKeyState(KeyMapping keyMapping) {
        if (keyMapping.isUnbound()) {
            return;
        }
        InputConstants.Key key = InputConstants.getKey(keyMapping.saveString());
        if (key.getType() != InputConstants.Type.KEYSYM) {
            return;
        }

        boolean pressed = InputConstants.isKeyDown(minecraft.getWindow(), key.getValue());
        setForwardedMovementKey(keyMapping, pressed);
    }

    private boolean forwardMovementKeyEvent(KeyEvent event, boolean pressed) {
        boolean handled = false;
        handled |= forwardMovementKeyEvent(minecraft.options.keyUp, event, pressed);
        handled |= forwardMovementKeyEvent(minecraft.options.keyDown, event, pressed);
        handled |= forwardMovementKeyEvent(minecraft.options.keyLeft, event, pressed);
        handled |= forwardMovementKeyEvent(minecraft.options.keyRight, event, pressed);
        handled |= forwardMovementKeyEvent(minecraft.options.keyJump, event, pressed);
        handled |= forwardMovementKeyEvent(minecraft.options.keyShift, event, pressed);
        handled |= forwardMovementKeyEvent(minecraft.options.keySprint, event, pressed);
        return handled;
    }

    private boolean forwardMovementKeyEvent(KeyMapping keyMapping, KeyEvent event, boolean pressed) {
        if (!keyMapping.matches(event)) {
            return false;
        }
        setForwardedMovementKey(keyMapping, pressed);
        return true;
    }

    private void setForwardedMovementKey(KeyMapping keyMapping, boolean pressed) {
        boolean stateChanged = pressed
                ? forwardedMovementKeys.add(keyMapping)
                : forwardedMovementKeys.remove(keyMapping);
        if (stateChanged) {
            keyMapping.setDown(pressed);
        }
    }

    private void updateSelection(int mouseX, int mouseY) {
        int dx = mouseX - centerX;
        int dy = mouseY - centerY;
        int deadZone = Math.max(18, innerRadius * 2 / 3);
        if (dx * dx + dy * dy < deadZone * deadZone) {
            selectedSector = -1;
            return;
        }

        int sector = getSector(dx, dy);
        selectedSector = sector < getSoundsOnCurrentPage().size() ? sector : -1;
    }

    private Sound getSelectedSound() {
        List<Sound> pageSounds = getSoundsOnCurrentPage();
        if (selectedSector < 0 || selectedSector >= pageSounds.size()) {
            return null;
        }
        return pageSounds.get(selectedSector);
    }

    private List<Sound> getSoundsOnCurrentPage() {
        int start = page * SOUNDS_PER_PAGE;
        int end = Math.min(start + SOUNDS_PER_PAGE, sounds.size());
        return sounds.subList(start, end);
    }

    private int getPageCount() {
        return Math.max(1, (sounds.size() + SOUNDS_PER_PAGE - 1) / SOUNDS_PER_PAGE);
    }

    private boolean hasPreviousPage() {
        return page > 0;
    }

    private boolean hasNextPage() {
        return page + 1 < getPageCount();
    }

    private int getPreviousPageButtonX() {
        return centerX - outerRadius - PAGE_BUTTON_GAP - SoundboardUi.PAGE_BUTTON_WIDTH;
    }

    private int getNextPageButtonX() {
        return centerX + outerRadius + PAGE_BUTTON_GAP;
    }

    private int getPageButtonY() {
        return centerY - outerRadius + PAGE_BUTTON_GAP;
    }

    private boolean isInsidePreviousPageButton(double mouseX, double mouseY) {
        return SoundboardUi.contains(
                mouseX,
                mouseY,
                getPreviousPageButtonX(),
                getPageButtonY(),
                SoundboardUi.PAGE_BUTTON_WIDTH,
                SoundboardUi.PAGE_BUTTON_HEIGHT
        );
    }

    private boolean isInsideNextPageButton(double mouseX, double mouseY) {
        return SoundboardUi.contains(
                mouseX,
                mouseY,
                getNextPageButtonX(),
                getPageButtonY(),
                SoundboardUi.PAGE_BUTTON_WIDTH,
                SoundboardUi.PAGE_BUTTON_HEIGHT
        );
    }

    private boolean isOverVisiblePageButton(double mouseX, double mouseY) {
        return hasPreviousPage() && isInsidePreviousPageButton(mouseX, mouseY)
                || hasNextPage() && isInsideNextPageButton(mouseX, mouseY);
    }

    private void buildSectorSpans() {
        sectorSpans.clear();
        for (int i = 0; i < SOUNDS_PER_PAGE; i++) {
            sectorSpans.add(new ArrayList<>());
        }

        int outerSquared = outerRadius * outerRadius;
        int innerSquared = innerRadius * innerRadius;
        for (int y = -outerRadius; y <= outerRadius; y++) {
            int activeSector = -1;
            int runStart = 0;
            for (int x = -outerRadius; x <= outerRadius; x++) {
                int distanceSquared = x * x + y * y;
                int sector = distanceSquared <= outerSquared && distanceSquared >= innerSquared
                        ? getSector(x, y)
                        : -1;

                if (sector == activeSector) {
                    continue;
                }
                if (activeSector >= 0) {
                    sectorSpans.get(activeSector).add(new Span(y, runStart, x));
                }
                activeSector = sector;
                runStart = x;
            }
            if (activeSector >= 0) {
                sectorSpans.get(activeSector).add(new Span(y, runStart, outerRadius + 1));
            }
        }
    }

    private static int getSector(int dx, int dy) {
        double angle = Math.atan2(dy, dx) + Math.PI / 2.0D + SECTOR_ANGLE / 2.0D;
        angle = (angle % FULL_CIRCLE + FULL_CIRCLE) % FULL_CIRCLE;
        return (int) (angle / SECTOR_ANGLE) % SOUNDS_PER_PAGE;
    }

    private void drawPlayingSectors(GuiGraphicsExtractor graphics, Set<String> playingSoundIds) {
        List<Sound> pageSounds = getSoundsOnCurrentPage();
        for (int i = 0; i < pageSounds.size(); i++) {
            if (playingSoundIds.contains(pageSounds.get(i).getId())) {
                drawSector(graphics, i, SoundboardUi.PLAYING);
            }
        }
    }

    private void drawSector(GuiGraphicsExtractor graphics, int sector, int color) {
        for (Span span : sectorSpans.get(sector)) {
            graphics.fill(
                    centerX + span.startX(),
                    centerY + span.y(),
                    centerX + span.endX(),
                    centerY + span.y() + 1,
                    color
            );
        }
    }

    private void drawDividers(GuiGraphicsExtractor graphics) {
        for (int i = 0; i < SOUNDS_PER_PAGE; i++) {
            float angle = (float) (-Math.PI * 2.0D / 3.0D + i * SECTOR_ANGLE);
            graphics.pose().pushMatrix();
            graphics.pose().translate(centerX, centerY);
            graphics.pose().rotate(angle);
            graphics.fill(
                    innerRadius,
                    -1,
                    outerRadius + 1,
                    2,
                    SoundboardUi.BORDER_DARK
            );
            graphics.fill(
                    innerRadius + 2,
                    -1,
                    outerRadius,
                    0,
                    SoundboardUi.BORDER_MID
            );
            graphics.pose().popMatrix();
        }
    }

    private void drawPageControls(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int buttonY = getPageButtonY();
        if (hasPreviousPage()) {
            SoundboardUi.drawPageButton(
                    graphics,
                    getPreviousPageButtonX(),
                    buttonY,
                    false,
                    isInsidePreviousPageButton(mouseX, mouseY)
            );
            if (isInsidePreviousPageButton(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(
                        Component.translatable("gui.soundboard.wheel.previous_page"),
                        mouseX,
                        mouseY
                );
            }
        }
        if (hasNextPage()) {
            SoundboardUi.drawPageButton(
                    graphics,
                    getNextPageButtonX(),
                    buttonY,
                    true,
                    isInsideNextPageButton(mouseX, mouseY)
            );
            if (isInsideNextPageButton(mouseX, mouseY)) {
                graphics.setTooltipForNextFrame(
                        Component.translatable("gui.soundboard.wheel.next_page"),
                        mouseX,
                        mouseY
                );
            }
        }
        if (getPageCount() > 1) {
            SoundboardUi.drawMouseWheelIcon(graphics, centerX, centerY + 9);
            if (mouseX >= centerX - 6 && mouseX < centerX + 6
                    && mouseY >= centerY + 7 && mouseY < centerY + 25) {
                graphics.setTooltipForNextFrame(
                        Component.translatable("gui.soundboard.wheel.mouse_wheel"),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    private void drawLabels(GuiGraphicsExtractor graphics, Set<String> playingSoundIds) {
        List<Sound> pageSounds = getSoundsOnCurrentPage();
        int labelRadius = (innerRadius + outerRadius) / 2 - 2;
        int maxTextWidth = Math.max(30, (int) (outerRadius * 0.5D));

        for (int i = 0; i < pageSounds.size(); i++) {
            Sound sound = pageSounds.get(i);
            double angle = -Math.PI / 2.0D + i * SECTOR_ANGLE;
            int x = centerX + (int) Math.round(Math.cos(angle) * labelRadius);
            int y = centerY + (int) Math.round(Math.sin(angle) * labelRadius);
            boolean playing = playingSoundIds.contains(sound.getId());
            String prefix = (playing ? "\u25b6 " : "") + (sound.isLoop() ? "\u27f3 " : "");
            int nameWidth = Math.max(0, maxTextWidth - font.width(prefix));
            String name = prefix + SoundboardUi.fitText(font, sound.getName(), nameWidth);
            graphics.centeredText(
                    font,
                    name,
                    x,
                    y - font.lineHeight / 2,
                    i == selectedSector
                            ? SoundboardUi.TEXT_PRIMARY
                            : SoundboardUi.TEXT_SECONDARY
            );
        }
    }

    private record Span(int y, int startX, int endX) {
    }
}
