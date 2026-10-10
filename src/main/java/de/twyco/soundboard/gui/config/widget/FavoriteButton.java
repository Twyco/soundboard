package de.twyco.soundboard.gui.config.widget;

import de.twyco.soundboard.gui.component.SoundboardUi;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

final class FavoriteButton extends Button {

    static final int SIZE = 16;

    private static final String EMPTY_STAR = "\u2606";
    private static final String FAVORITE_STAR = "\u2605";

    private final Font font;
    private final Consumer<Boolean> onChange;
    private boolean favorite;

    FavoriteButton(Font font, boolean favorite, Consumer<Boolean> onChange) {
        super(0, 0, SIZE, SIZE, Component.empty(), _ -> {
        }, DEFAULT_NARRATION);
        this.font = font;
        this.onChange = onChange;
        setFavorite(favorite);
    }

    @Override
    public void onPress(InputWithModifiers input) {
        setFavorite(!favorite);
        onChange.accept(favorite);
    }

    @Override
    protected void extractContents(
            GuiGraphicsExtractor graphics,
            int mouseX,
            int mouseY,
            float delta
    ) {
        int color = favorite ? SoundboardUi.ACCENT : SoundboardUi.TEXT_SECONDARY;
        graphics.centeredText(
                font,
                favorite ? FAVORITE_STAR : EMPTY_STAR,
                getX() + getWidth() / 2,
                getY() + (getHeight() - font.lineHeight) / 2,
                color
        );
    }

    private void setFavorite(boolean favorite) {
        this.favorite = favorite;
        String actionKey = favorite
                ? "gui.soundboard.config.sound.favorite.remove"
                : "gui.soundboard.config.sound.favorite.add";
        Component action = Component.translatable(actionKey);
        setMessage(action);
        setTooltip(Tooltip.create(action));
    }
}
