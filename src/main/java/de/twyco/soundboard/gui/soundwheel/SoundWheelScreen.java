package de.twyco.soundboard.gui.soundwheel;

import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.util.keybinding.KeyCombo;
import de.twyco.soundboard.util.sound.Sound;
import de.twyco.soundboard.util.sound.SoundManager;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
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
    private static final int PAGE_BUTTON_SIZE = 22;
    private static final int PAGE_BUTTON_GAP = 8;
    private static final double FULL_CIRCLE = Math.PI * 2.0D;
    private static final double SECTOR_ANGLE = FULL_CIRCLE / SOUNDS_PER_PAGE;

    private static final int WHEEL_COLOR = 0xDD20242A;
    private static final int SELECTED_COLOR = 0xDD278C82;
    private static final int PLAYING_COLOR = 0xDD287A46;
    private static final int PLAYING_SELECTED_COLOR = 0xDD36A564;
    private static final int CENTER_COLOR = 0xEE111418;
    private static final int DIVIDER_COLOR = 0xAAE6E6E6;
    private static final int TEXT_COLOR = 0xFFE8E8E8;
    private static final int SELECTED_TEXT_COLOR = 0xFFFFFFFF;
    private static final int BUTTON_COLOR = 0xCC20242A;
    private static final int BUTTON_HOVER_COLOR = 0xDD278C82;

    private final Screen parent;
    private final KeyCombo activationCombo;
    private final List<Sound> sounds;
    private final List<List<Span>> sectorSpans = new ArrayList<>(SOUNDS_PER_PAGE);

    private int centerX;
    private int centerY;
    private int outerRadius;
    private int innerRadius;
    private int page;
    private int selectedSector = -1;
    private int age;
    private boolean closed;

    private SoundWheelScreen(Screen parent, KeyCombo activationCombo, List<Sound> sounds) {
        super(Component.translatable("gui.soundboard.wheel.title"));
        this.parent = parent;
        this.activationCombo = activationCombo;
        this.sounds = sounds;
    }

    public static void open(KeyCombo activationCombo) {
        Minecraft client = Minecraft.getInstance();
        if (client.gui.screen() != null) {
            return;
        }

        List<Sound> sortedSounds = SoundManager.getAllSounds().stream()
                .sorted(Comparator.comparing(Sound::getName, String.CASE_INSENSITIVE_ORDER))
                .toList();
        client.gui.setScreen(new SoundWheelScreen(client.gui.screen(), activationCombo, sortedSounds));
    }

    @Override
    protected void init() {
        centerX = width / 2;
        centerY = height / 2;
        int availableRadius = Math.min(width, height) / 2 - 34;
        outerRadius = Math.max(MIN_OUTER_RADIUS, Math.min(MAX_OUTER_RADIUS, availableRadius));
        innerRadius = Math.max(22, (int) Math.round(outerRadius * 0.38D));
        buildSectorSpans();
        page = Math.min(page, getPageCount() - 1);
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

        drawCircle(graphics, centerX, centerY, outerRadius, WHEEL_COLOR);
        drawPlayingSectors(graphics, playingSoundIds);
        if (selectedSector >= 0) {
            Sound selectedSound = getSelectedSound();
            boolean selectedSoundPlaying = selectedSound != null
                    && playingSoundIds.contains(selectedSound.getId());
            drawSector(
                    graphics,
                    selectedSector,
                    selectedSoundPlaying ? PLAYING_SELECTED_COLOR : SELECTED_COLOR
            );
        }
        drawCircle(graphics, centerX, centerY, innerRadius, CENTER_COLOR);
        drawDividers(graphics);
        drawLabels(graphics, playingSoundIds);

        graphics.centeredText(
                font,
                title,
                centerX,
                centerY - outerRadius - 22,
                TEXT_COLOR
        );
        graphics.centeredText(
                font,
                Component.translatable("gui.soundboard.wheel.page", page + 1, getPageCount()),
                centerX,
                centerY - font.lineHeight / 2,
                TEXT_COLOR
        );
        drawPageControls(graphics, mouseX, mouseY);
    }

    @Override
    public void tick() {
        super.tick();
        age++;
        if (age > 1 && !activationCombo.allKeysPressed()) {
            onClose();
        }
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (activationCombo.getKeyCodes().contains(event.key())) {
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
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        if (activationCombo.getKeyCodes().contains(event.key())) {
            onClose();
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
        minecraft.gui.setScreen(parent);
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
        return centerX - outerRadius - PAGE_BUTTON_GAP - PAGE_BUTTON_SIZE;
    }

    private int getNextPageButtonX() {
        return centerX + outerRadius + PAGE_BUTTON_GAP;
    }

    private int getPageButtonY() {
        return centerY - outerRadius + PAGE_BUTTON_GAP;
    }

    private boolean isInsidePreviousPageButton(double mouseX, double mouseY) {
        return isInsideButton(mouseX, mouseY, getPreviousPageButtonX(), getPageButtonY());
    }

    private boolean isInsideNextPageButton(double mouseX, double mouseY) {
        return isInsideButton(mouseX, mouseY, getNextPageButtonX(), getPageButtonY());
    }

    private boolean isOverVisiblePageButton(double mouseX, double mouseY) {
        return hasPreviousPage() && isInsidePreviousPageButton(mouseX, mouseY)
                || hasNextPage() && isInsideNextPageButton(mouseX, mouseY);
    }

    private static boolean isInsideButton(double mouseX, double mouseY, int x, int y) {
        return mouseX >= x && mouseX < x + PAGE_BUTTON_SIZE
                && mouseY >= y && mouseY < y + PAGE_BUTTON_SIZE;
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
                drawSector(graphics, i, PLAYING_COLOR);
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
            graphics.fill(innerRadius, -1, outerRadius + 1, 1, DIVIDER_COLOR);
            graphics.pose().popMatrix();
        }
    }

    private void drawPageControls(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        int buttonY = getPageButtonY();
        if (hasPreviousPage()) {
            drawPageButton(
                    graphics,
                    getPreviousPageButtonX(),
                    buttonY,
                    "\u25c0",
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
            drawPageButton(
                    graphics,
                    getNextPageButtonX(),
                    buttonY,
                    "\u25b6",
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
            drawMouseWheelIcon(graphics, centerX, centerY + 8);
            if (mouseX >= centerX - 6 && mouseX < centerX + 6
                    && mouseY >= centerY + 6 && mouseY < centerY + 24) {
                graphics.setTooltipForNextFrame(
                        Component.translatable("gui.soundboard.wheel.mouse_wheel"),
                        mouseX,
                        mouseY
                );
            }
        }
    }

    private void drawPageButton(
            GuiGraphicsExtractor graphics,
            int x,
            int y,
            String icon,
            boolean hovered
    ) {
        graphics.fill(
                x,
                y,
                x + PAGE_BUTTON_SIZE,
                y + PAGE_BUTTON_SIZE,
                hovered ? BUTTON_HOVER_COLOR : BUTTON_COLOR
        );
        graphics.outline(x, y, PAGE_BUTTON_SIZE, PAGE_BUTTON_SIZE, DIVIDER_COLOR);
        graphics.centeredText(
                font,
                icon,
                x + PAGE_BUTTON_SIZE / 2,
                y + (PAGE_BUTTON_SIZE - font.lineHeight) / 2,
                SELECTED_TEXT_COLOR
        );
    }

    private static void drawMouseWheelIcon(GuiGraphicsExtractor graphics, int centerX, int y) {
        int left = centerX - 5;
        int right = centerX + 5;
        graphics.fill(left + 2, y, right - 2, y + 1, TEXT_COLOR);
        graphics.fill(left, y + 2, left + 1, y + 12, TEXT_COLOR);
        graphics.fill(right - 1, y + 2, right, y + 12, TEXT_COLOR);
        graphics.fill(left + 2, y + 13, right - 2, y + 14, TEXT_COLOR);
        graphics.fill(left + 1, y + 1, left + 2, y + 2, TEXT_COLOR);
        graphics.fill(right - 2, y + 1, right - 1, y + 2, TEXT_COLOR);
        graphics.fill(left + 1, y + 12, left + 2, y + 13, TEXT_COLOR);
        graphics.fill(right - 2, y + 12, right - 1, y + 13, TEXT_COLOR);
        graphics.fill(centerX - 1, y + 2, centerX + 1, y + 6, SELECTED_COLOR);
    }

    private void drawLabels(GuiGraphicsExtractor graphics, Set<String> playingSoundIds) {
        List<Sound> pageSounds = getSoundsOnCurrentPage();
        int labelRadius = (innerRadius + outerRadius) / 2;
        int maxTextWidth = Math.max(36, (int) (outerRadius * 0.72D));

        for (int i = 0; i < pageSounds.size(); i++) {
            Sound sound = pageSounds.get(i);
            double angle = -Math.PI / 2.0D + i * SECTOR_ANGLE;
            int x = centerX + (int) Math.round(Math.cos(angle) * labelRadius);
            int y = centerY + (int) Math.round(Math.sin(angle) * labelRadius);
            boolean playing = playingSoundIds.contains(sound.getId());
            String prefix = (playing ? "\u25b6 " : "") + (sound.isLoop() ? "\u27f3 " : "");
            int nameWidth = Math.max(0, maxTextWidth - font.width(prefix));
            String name = prefix + fitText(font, sound.getName(), nameWidth);
            graphics.centeredText(
                    font,
                    name,
                    x,
                    y - font.lineHeight / 2,
                    i == selectedSector ? SELECTED_TEXT_COLOR : TEXT_COLOR
            );
        }
    }

    private static void drawCircle(
            GuiGraphicsExtractor graphics,
            int centerX,
            int centerY,
            int radius,
            int color
    ) {
        int radiusSquared = radius * radius;
        for (int y = -radius; y <= radius; y++) {
            int halfWidth = (int) Math.floor(Math.sqrt(radiusSquared - y * y));
            graphics.fill(
                    centerX - halfWidth,
                    centerY + y,
                    centerX + halfWidth + 1,
                    centerY + y + 1,
                    color
            );
        }
    }

    private static String fitText(Font font, String text, int maxWidth) {
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int availableWidth = Math.max(0, maxWidth - font.width(suffix));
        return font.plainSubstrByWidth(text, availableWidth) + suffix;
    }

    private record Span(int y, int startX, int endX) {
    }
}
