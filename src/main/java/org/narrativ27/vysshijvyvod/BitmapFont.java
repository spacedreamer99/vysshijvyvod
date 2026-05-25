package org.narrativ27.vysshijvyvod;
import java.nio.file.*;
import java.util.*;

public class BitmapFont
{
    private Texture texture;
    private final Map<Character, Glyph> glyphs = new HashMap<>();

    public BitmapFont(String folderPath) throws Exception
    {
        Path texPath = Paths.get(folderPath, "spritesheet.png");
        texture = new Texture(texPath.toString());

        Path charsetPath = Paths.get(folderPath, "charset.txt");
        List<String> lines = Files.readAllLines(charsetPath);
        for (String line : lines)
        {
            String[] parts = line.trim().split("\\s+");
            if (parts.length < 6) continue;
            String ch = parts[0];
            if (ch.isEmpty() || ch.length() != 1) continue;
            char c = ch.charAt(0);
            int x = Integer.parseInt(parts[1]);
            int y = Integer.parseInt(parts[2]);
            int w = Integer.parseInt(parts[3]);
            int h = Integer.parseInt(parts[4]);
            int advance = Integer.parseInt(parts[5]);
            glyphs.put(c, new Glyph(x, y, w, h, advance));
        }
    }

    public Glyph getGlyph(char c) { return glyphs.get(c); }
    public Texture getTexture() { return texture; }

    public static class Glyph
    {
        public final int x, y, width, height, advance;
        public Glyph(int x, int y, int w, int h, int advance)
        {
            this.x = x; this.y = y; width = w; height = h; this.advance = advance;
        }
    }

    public void dispose() { if (texture != null) texture.dispose(); }
}
