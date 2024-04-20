package me.rebirthclient.mod.gui.font;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.util.Objects;

public class FontRenderers {
    public static FontAdapter Arial;
    public static @NotNull RendererFontAdapter createDefault(float size, String name) throws IOException, FontFormatException {
        return new RendererFontAdapter(Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(FontRenderers.class.getClassLoader().getResourceAsStream("assets/rebirth/fonts/" + name + ".ttf"))).deriveFont(Font.PLAIN, size / 2f), size / 2f);
    }

    public static RendererFontAdapter createArial(float size) {
        return new RendererFontAdapter(new Font("Arial", Font.PLAIN, (int) (size / 2f)), size / 2f);
    }
}
