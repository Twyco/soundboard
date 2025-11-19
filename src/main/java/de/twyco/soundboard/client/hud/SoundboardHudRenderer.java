package de.twyco.soundboard.client.hud;

import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.util.PlayingSound;
import de.twyco.soundboard.util.client.SoundboardRuntimeState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.TextWidget;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

public class SoundboardHudRenderer {


    public static void render(DrawContext drawContext, RenderTickCounter renderTickCounter) {
        if (!SoundboardRuntimeState.isShowPlayingSoundsHud()) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if(client == null || client.options.hudHidden) {
            return;
        }

        if(!SimpleVoicechatService.isAvailable()) {
            return;
        }

        List<PlayingSound> playing = SimpleVoicechatService.getCurrentlyPlayingSounds();
        if(playing.isEmpty()) {
            return;
        }

        TextRenderer tr = client.textRenderer;

        List<Text> lines = new ArrayList<>();
        lines.add(Text.literal("Currently Playing").formatted(Formatting.RED, Formatting.UNDERLINE));
        for (PlayingSound sound : playing) {
            MutableText displayName = Text.literal(sound.displayName).formatted(Formatting.WHITE);
            if(sound.loop){
                displayName.append(Text.literal(" Looping").formatted(Formatting.GRAY));
            }
            lines.add(displayName);
        }

        int screenWidth = client.getWindow().getScaledWidth();
        int screenHeight = client.getWindow().getScaledHeight();

        int padding = 5;
        int lineSpacing = 2;

        int maxWidth = 0;
        for (Text line : lines) {
            int w = tr.getWidth(line);
            if (w > maxWidth) {
                maxWidth = w;
            }
        }

        int lineHeight = tr.fontHeight + lineSpacing;
        int totalHeight = lines.size() * lineHeight;

        int x = screenWidth - maxWidth - padding;
        int startY = screenHeight - totalHeight - padding;

        float delta = renderTickCounter.getDynamicDeltaTicks();

        int bgPadding = 3;

        int bgLeft   = x - bgPadding;
        int bgTop    = startY - bgPadding;
        int bgRight  = x + maxWidth + bgPadding;
        int bgBottom = startY + totalHeight + bgPadding;

        drawContext.fill(bgLeft, bgTop, bgRight, bgBottom, 0x80000000);

        int y = startY;
        boolean title = true;
        for (Text line : lines) {
            TextWidget widget = new TextWidget(line, tr);
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(maxWidth);
            widget.setHeight(tr.fontHeight);
            widget.render(drawContext, 0, 0, delta);

            y += lineHeight;
            if(title){
                y += tr.fontHeight / 2;
                title = false;
            }
        }
    }

}
