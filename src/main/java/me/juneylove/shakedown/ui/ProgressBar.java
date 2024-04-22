package me.juneylove.shakedown.ui;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;

public class ProgressBar {

    private static final String BAR_LEFT  = "\uE042" + TextFormat.NEGATIVE_ONE_PX;
    private static final String BAR_RIGHT = "\uE042";
    private static final String BAR_EMPTY = "\uE043" + TextFormat.NEGATIVE_ONE_PX;
    private static final String BAR_FULL  = "\uE044" + TextFormat.NEGATIVE_ONE_PX;

    public static TextComponent progressBar(double fraction, int width, TextColor color) {

        if (fraction > 1.0) fraction = 1.0;
        if (fraction < 0.0) fraction = 0.0;

        int emptyPixels = (int) ((1-fraction) * (width-2));
        int fullPixels = (width-2) - emptyPixels;

        TextComponent full  = Component.text(BAR_FULL.repeat(fullPixels)).color(color);
        TextComponent empty = Component.text(BAR_EMPTY.repeat(emptyPixels));
        TextComponent left  = Component.text(BAR_LEFT);
        TextComponent right = Component.text(BAR_RIGHT);

        return left.append(full).append(empty).append(right);

    }

}
