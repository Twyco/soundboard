package de.twyco.soundboard.client.hud;

import de.twyco.soundboard.modImplementations.simpleVoicechatApi.SimpleVoicechatService;
import de.twyco.soundboard.modImplementations.simpleVoicechatApi.util.PlayingSound;
import de.twyco.soundboard.util.config.SoundboardConfig;
import de.twyco.soundboard.util.config.SoundboardConfigData;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

public class SoundboardHudRenderer {


    public static void extractRenderState(GuiGraphicsExtractor drawContext, DeltaTracker renderTickCounter) {
        SoundboardConfigData config = SoundboardConfig.get();
        if (!config.globalState.showPlayingSoundsHud) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        if(client.gui.hud.isHidden()) {
            return;
        }

        if(!SimpleVoicechatService.isAvailable()) {
            return;
        }

        List<PlayingSound> playing = SimpleVoicechatService.getCurrentlyPlayingSounds();
        if(playing.isEmpty()) {
            return;
        }

        Font tr = client.font;

        List<Component> lines = new ArrayList<>();
        lines.add(Component.literal("Currently Playing").withStyle(ChatFormatting.RED, ChatFormatting.UNDERLINE));
        for (PlayingSound sound : playing) {
            MutableComponent displayName = Component.literal(sound.displayName).withStyle(ChatFormatting.WHITE);
            if(sound.loop){
                displayName.append(Component.literal(" Looping").withStyle(ChatFormatting.GRAY));
            }
            lines.add(displayName);
        }

        int screenWidth = client.getWindow().getGuiScaledWidth();
        int screenHeight = client.getWindow().getGuiScaledHeight();

        int padding = 5;
        int lineSpacing = 2;

        int maxWidth = 0;
        for (Component line : lines) {
            int w = tr.width(line);
            if (w > maxWidth) {
                maxWidth = w;
            }
        }

        int lineHeight = tr.lineHeight + lineSpacing;
        int totalHeight = lines.size() * lineHeight;

        int x = screenWidth - maxWidth - padding;
        int startY = screenHeight - totalHeight - padding;

        float delta = renderTickCounter.getGameTimeDeltaTicks();

        int bgPadding = 3;

        int bgLeft   = x - bgPadding;
        int bgTop    = startY - bgPadding;
        int bgRight  = x + maxWidth + bgPadding;
        int bgBottom = startY + totalHeight + bgPadding;

        drawContext.fill(bgLeft, bgTop, bgRight, bgBottom, 0x80000000);

        int y = startY;
        boolean title = true;
        for (Component line : lines) {
            StringWidget widget = new StringWidget(line, tr);
            widget.setX(x);
            widget.setY(y);
            widget.setWidth(maxWidth);
            widget.setHeight(tr.lineHeight);
            widget.extractRenderState(drawContext, 0, 0, delta);

            y += lineHeight;
            if(title){
                y += tr.lineHeight / 2;
                title = false;
            }
        }
    }

}
