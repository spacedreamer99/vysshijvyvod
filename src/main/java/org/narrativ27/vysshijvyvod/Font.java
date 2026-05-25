package org.narrativ27.vysshijvyvod;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;

public class Font
{
    private int textureId;
    private final int atlasWidth = 512;
    private final int atlasHeight = 512;
    private final float fontSize;
    private final Map<Integer, float[]> charUV = new HashMap<>();
    private final Map<Integer, float[]> charWH = new HashMap<>();

    public Font(String ttfPath, float fontSize)
    {
        this.fontSize = fontSize;
        java.awt.Font awtFont;
        try {
            awtFont = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, new java.io.File(ttfPath))
                             .deriveFont(fontSize);
        } catch (Exception e) {
            throw new RuntimeException("Не удалось загрузить шрифт: " + ttfPath, e);
        }
        BufferedImage image = new BufferedImage(atlasWidth, atlasHeight, BufferedImage.TYPE_BYTE_GRAY);
        Graphics2D g = image.createGraphics();
        g.setFont(awtFont);
        g.setColor(java.awt.Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        int x = 0, y = 0, rowH = 0;
        for (int c = 32; c < 128; c++) {
            String str = Character.toString((char)c);
            FontMetrics fm = g.getFontMetrics();
            int w = fm.stringWidth(str);
            int h = fm.getHeight();
            if (x + w >= atlasWidth) { x = 0; y += rowH + 1; rowH = 0; }
            g.drawString(str, x, y + fm.getAscent());
            charUV.put(c, new float[]{
                (float)x / atlasWidth,
                (float)y / atlasHeight,
                (float)(x + w) / atlasWidth,
                (float)(y + h) / atlasHeight
            });
            charWH.put(c, new float[]{ (float)w, (float)h });
            x += w + 1;
            if (h > rowH) rowH = h;
        }
        g.dispose();
        int[] pixels = new int[atlasWidth * atlasHeight];
        image.getRGB(0, 0, atlasWidth, atlasHeight, pixels, 0, atlasWidth);
        ByteBuffer atlas = BufferUtils.createByteBuffer(atlasWidth * atlasHeight);
        for (int i = 0; i < pixels.length; i++) {
            atlas.put((byte)(pixels[i] & 0xFF));
        }
        atlas.flip();
        textureId = glGenTextures();
        glBindTexture(GL_TEXTURE_2D, textureId);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RED, atlasWidth, atlasHeight, 0, GL_RED, GL_UNSIGNED_BYTE, atlas);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_S, GL_CLAMP_TO_EDGE);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_WRAP_T, GL_CLAMP_TO_EDGE);
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void bind(int unit) { glActiveTexture(GL_TEXTURE0 + unit); glBindTexture(GL_TEXTURE_2D, textureId); }

    public float getTextWidth(String text) {
        float w = 0;
        for (int i = 0; i < text.length(); i++) {
            int c = text.charAt(i);
            if (c >= 32 && c < 128 && charWH.containsKey(c)) w += charWH.get(c)[0];
        }
        return w;
    }

    public FloatBuffer createTextMesh(String text, float x, float y) {
        int len = text.length();
        FloatBuffer buffer = BufferUtils.createFloatBuffer(len * 6 * 4);
        float curX = x;
        for (int i = 0; i < len; i++) {
            int c = text.charAt(i);
            if (c < 32 || c >= 128 || !charWH.containsKey(c)) continue;
            float[] wh = charWH.get(c); float w = wh[0]; float h = wh[1];
            float[] uv = charUV.get(c);
            // Инвертируем V для переворота текста
            float u0 = uv[0], v0 = 1.0f - uv[1];
            float u1 = uv[2], v1 = 1.0f - uv[3];
            buffer.put(new float[]{
                curX,     y,    u0, v0,
                curX + w, y,    u1, v0,
                curX + w, y + h, u1, v1,
                curX,     y,    u0, v0,
                curX + w, y + h, u1, v1,
                curX,     y + h, u0, v1
            });
            curX += w;
        }
        buffer.flip();
        return buffer;
    }

    public void dispose() { glDeleteTextures(textureId); }
}
