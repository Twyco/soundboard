package de.twyco.soundboard.gui.component;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;

public final class SoundboardUi {

    public static final int TEXT_PRIMARY = 0xFFFFFFFF;
    public static final int TEXT_SECONDARY = 0xFFB8B8B8;
    public static final int SURFACE = 0xEE303030;
    public static final int SURFACE_DARK = 0xEE171717;
    public static final int SURFACE_HOVERED = 0xEE484848;
    public static final int SURFACE_SELECTED = 0xEE5A5A5A;
    public static final int PLAYING = 0xEE397638;
    public static final int PLAYING_HOVERED = 0xEE4E9B49;
    public static final int BORDER_LIGHT = 0xFFAAAAAA;
    public static final int BORDER_MID = 0xFF5A5A5A;
    public static final int BORDER_DARK = 0xFF101010;
    public static final int SHADOW = 0xB0000000;
    public static final int ACCENT = 0xFFFFE36A;

    public static final int PAGE_BUTTON_WIDTH = 23;
    public static final int PAGE_BUTTON_HEIGHT = 13;

    private static final Identifier PAGE_BACKWARD =
            Identifier.withDefaultNamespace("widget/page_backward");
    private static final Identifier PAGE_BACKWARD_HIGHLIGHTED =
            Identifier.withDefaultNamespace("widget/page_backward_highlighted");
    private static final Identifier PAGE_FORWARD =
            Identifier.withDefaultNamespace("widget/page_forward");
    private static final Identifier PAGE_FORWARD_HIGHLIGHTED =
            Identifier.withDefaultNamespace("widget/page_forward_highlighted");

    private SoundboardUi() {
    }

    public static void drawRaisedPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.fill(x, y, x + width, y + height, color);
        graphics.renderOutline(x, y, width, height, BORDER_LIGHT);
    }

    public static void drawInsetPanel(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            int color
    ) {
        if (width <= 0 || height <= 0) {
            return;
        }
        graphics.fill(x, y, x + width, y + height, color);
        graphics.renderOutline(x, y, width, height, BORDER_LIGHT);
    }

    public static void drawListRow(
            GuiGraphics graphics,
            int x,
            int y,
            int width,
            int height,
            boolean hovered
    ) {
        drawInsetPanel(
                graphics,
                x,
                y,
                width,
                height,
                hovered ? SURFACE_HOVERED : SURFACE
        );
    }

    public static void drawCircle(
            GuiGraphics graphics,
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

    public static void drawPageButton(
            GuiGraphics graphics,
            int x,
            int y,
            boolean forward,
            boolean hovered
    ) {
        Identifier sprite;
        if (forward) {
            sprite = hovered ? PAGE_FORWARD_HIGHLIGHTED : PAGE_FORWARD;
        } else {
            sprite = hovered ? PAGE_BACKWARD_HIGHLIGHTED : PAGE_BACKWARD;
        }
        graphics.blitSprite(
                RenderPipelines.GUI_TEXTURED,
                sprite,
                x,
                y,
                PAGE_BUTTON_WIDTH,
                PAGE_BUTTON_HEIGHT
        );
    }

    public static void drawMouseWheelIcon(
            GuiGraphics graphics,
            int centerX,
            int y
    ) {
        int left = centerX - 5;
        int right = centerX + 5;
        graphics.fill(left + 2, y, right - 2, y + 1, TEXT_PRIMARY);
        graphics.fill(left, y + 2, left + 1, y + 12, TEXT_PRIMARY);
        graphics.fill(right - 1, y + 2, right, y + 12, TEXT_PRIMARY);
        graphics.fill(left + 2, y + 13, right - 2, y + 14, TEXT_PRIMARY);
        graphics.fill(left + 1, y + 1, left + 2, y + 2, TEXT_PRIMARY);
        graphics.fill(right - 2, y + 1, right - 1, y + 2, TEXT_PRIMARY);
        graphics.fill(left + 1, y + 12, left + 2, y + 13, TEXT_PRIMARY);
        graphics.fill(right - 2, y + 12, right - 1, y + 13, TEXT_PRIMARY);
        graphics.fill(centerX - 1, y + 2, centerX + 1, y + 6, ACCENT);
    }

    public static boolean contains(
            double mouseX,
            double mouseY,
            int x,
            int y,
            int width,
            int height
    ) {
        return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
    }

    public static String fitText(Font font, String text, int maxWidth) {
        if (maxWidth <= 0) {
            return "";
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String suffix = "...";
        int availableWidth = Math.max(0, maxWidth - font.width(suffix));
        return font.plainSubstrByWidth(text, availableWidth) + suffix;
    }
}
