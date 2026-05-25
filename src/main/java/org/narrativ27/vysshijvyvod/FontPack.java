package org.narrativ27.vysshijvyvod;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import javax.imageio.ImageIO;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL12.*;
import static org.lwjgl.opengl.GL13.*;

public class FontPack
{
    private Map<Character, Glyph> glyphs = new HashMap<>();
    private Texture texture;

    public FontPack(String folderPath) throws IOException
    {
        File dir = new File(folderPath);
        File[] files = dir.listFiles((d, name) -> name.endsWith(".png"));
        if (files == null) throw new IOException("Папка не найдена: " + folderPath);

        Map<Character, BufferedImage> images = new HashMap<>();
        int totalWidth = 0;
        int maxHeight = 0;

        for (File f : files)
        {
            String name = f.getName().replace(".png", "");
            if (name.isEmpty()) continue;
            char charName = name.charAt(0);
            BufferedImage img = ImageIO.read(f);
            images.put(charName, img);
            totalWidth += img.getWidth();
            int h = img.getHeight();
            if (h > maxHeight) maxHeight = h;
        }

        totalWidth += images.size() * 4;
        ByteBuffer atlasBuffer = BufferUtils.createByteBuffer(totalWidth * maxHeight * 4);
        int currentX = 0;
        for (char c : images.keySet())
        {
            BufferedImage img = images.get(c);
            int w = img.getWidth();
            int h = img.getHeight();
            int yOffset = maxHeight - h;

            for (int y = 0; y < h; y++)
            {
                for (int x = 0; x < w; x++)
                {
                    int pixel = img.getRGB(x, y);
                    int alpha = (pixel >> 24) & 0xFF;
                    int red   = (pixel >> 16) & 0xFF;
                    int green = (pixel >> 8) & 0xFF;
                    int blue  = pixel & 0xFF;
                    int destIdx = ((y + yOffset) * totalWidth + (currentX + x)) * 4;
                    atlasBuffer.put(destIdx, (byte)red);
                    atlasBuffer.put(destIdx + 1, (byte)green);
                    atlasBuffer.put(destIdx + 2, (byte)blue);
                    atlasBuffer.put(destIdx + 3, (byte)alpha);
                }
            }

            glyphs.put(c, new Glyph(currentX, yOffset, w, h, w));
            currentX += w;
            currentX += 4;
        }

        int texId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, texId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, totalWidth, maxHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, atlasBuffer);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glBindTexture(GL_TEXTURE_2D, 0);

        texture = new Texture(texId, totalWidth, maxHeight);
        System.out.println("FontPack загружен: " + glyphs.size() + " глифов, размер " + totalWidth + "x" + maxHeight);
    }

    public Texture getTexture() { return texture; }
    public Glyph getGlyph(char c) { return glyphs.get(c); }

    public Map<Character, Glyph> getGlyphs() { return glyphs; }
    public void dispose()
    {
        if (texture != null) texture.dispose();
    }

    public static class Glyph
    {
        public final int x, y, width, height, advance;
        public Glyph(int x, int y, int w, int h, int advance)
        {
            this.x = x; this.y = y; width = w; height = h; this.advance = advance;
        }
    }
}
