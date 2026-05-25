package org.narrativ27.vysshijvyvod;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import java.nio.FloatBuffer;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;

public class EncyclopediaRenderer {
    private UIRenderer uiRenderer;
    private FontPack runicFont;
    private Shader textShader;
    private int textVAO, textVBO;
    private int screenW, screenH;

    private int[] leafTextures = new int[6];
    private int[] leafWidths = new int[6];
    private int[] leafHeights = new int[6];

    private static final int PADDING = 40;
    private static final int DESIRED_TEXT_WIDTH = 1600;

    public EncyclopediaRenderer(UIRenderer uiRenderer) { this.uiRenderer = uiRenderer; }

    public int[] getLeafWidths() { return leafWidths; }
    public int[] getLeafHeights() { return leafHeights; }

    public void init() throws Exception {
        runicFont = new FontPack("resources/fonts/runic");
        textShader = new Shader("resources/shaders/ui.vert", "resources/shaders/ui.frag");
        textVAO = glGenVertexArrays();
        textVBO = glGenBuffers();
        buildLeafTextures();
    }

    private void buildLeafTextures() {
        float scale = 0.5f;
        int lineHeight = 50;  // увеличенный интервал
        float maxGlyphHeight = getTextHeight(scale);

        for (int i = 0; i < 6; i++) {
            String text = EncyclopediaContent.TEXTS[i];
            if (text == null) text = "";

            String[] words = text.split(" ");
            float maxWordWidth = 0;
            for (String w : words) {
                float ww = getTextWidth(w, scale, runicFont);
                if (ww > maxWordWidth) maxWordWidth = ww;
            }

            float textAreaWidth = Math.max(DESIRED_TEXT_WIDTH, maxWordWidth);

            List<String> lines = new ArrayList<>();
            StringBuilder line = new StringBuilder();
            for (String word : words) {
                String test = line.length() > 0 ? line.toString() + " " + word : word;
                float testW = getTextWidth(test, scale, runicFont);
                if (testW > textAreaWidth && line.length() > 0) {
                    lines.add(line.toString());
                    line = new StringBuilder(word);
                } else {
                    if (line.length() > 0) line.append(" ");
                    line.append(word);
                }
            }
            if (line.length() > 0) {
                lines.add(line.toString());
            }

            int leafWidth = (int) Math.ceil(textAreaWidth + PADDING * 2);
            int leafHeight = (int) Math.ceil(lines.size() * lineHeight + PADDING * 2 + maxGlyphHeight);

            leafWidths[i] = leafWidth;
            leafHeights[i] = leafHeight;

            // Создаём текстуру и FBO
            int fbo = glGenFramebuffers();
            leafTextures[i] = glGenTextures();
            glBindTexture(GL_TEXTURE_2D, leafTextures[i]);
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, leafWidth, leafHeight, 0, GL_RGBA, GL_UNSIGNED_BYTE, (ByteBuffer)null);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            glBindFramebuffer(GL_FRAMEBUFFER, fbo);
            glFramebufferTexture2D(GL_FRAMEBUFFER, GL_COLOR_ATTACHMENT0, GL_TEXTURE_2D, leafTextures[i], 0);

            glViewport(0, 0, leafWidth, leafHeight);
            glClearColor(0,0,0,0);
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
            glDisable(GL_DEPTH_TEST);

            float curY = leafHeight - PADDING - maxGlyphHeight;
            float lineX = PADDING;

            for (String ln : lines) {
                drawTextStatic(ln, lineX, curY, scale, 1,1,1, runicFont, textShader, textVAO, textVBO, leafWidth, leafHeight);
                curY -= lineHeight;
            }

            glBindFramebuffer(GL_FRAMEBUFFER, 0);
            glEnable(GL_DEPTH_TEST);
            glDeleteFramebuffers(fbo);
        }
    }

    private float getTextWidth(String text, float scale, FontPack font) {
        float w = 0;
        for (char c : text.toUpperCase().toCharArray()) {
            if (c == ' ') {
                FontPack.Glyph spaceG = font.getGlyph('A');
                w += (spaceG != null ? spaceG.advance : 20) * scale + 6;
                continue;
            }
            FontPack.Glyph g = font.getGlyph(c);
            w += (g != null ? g.advance : 20) * scale + 6;
        }
        return w;
    }

    private float getTextHeight(float scale) {
        if (runicFont == null) return 20 * scale;
        FontPack.Glyph g = runicFont.getGlyph('A');
        if (g == null) {
            for (FontPack.Glyph any : runicFont.getGlyphs().values()) { g = any; break; }
        }
        if (g == null) return 20 * scale;
        return g.height * scale;
    }

    private void drawTextStatic(String text, float x, float y, float scale, float r, float g, float b,
                                FontPack font, Shader shader, int vao, int vbo, int w, int h) {
        if (font == null) return;
        Texture tex = font.getTexture();
        if (tex == null) return;
        shader.use();
        FloatBuffer mat = BufferUtils.createFloatBuffer(16);
        new Matrix4f().ortho(0, w, 0, h, -1, 1).get(mat);
        shader.setUniformMatrix4fv("uProjection", mat);
        tex.bind(0);
        shader.setUniform1i("uTexture", 0);
        shader.setUniform1i("uUseTexture", 1);
        shader.setUniform3f("uTextColor", r, g, b);
        float curX = x;
        FloatBuffer verts = BufferUtils.createFloatBuffer(text.length() * 6 * 4);
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toUpperCase(text.charAt(i));
            if (c == ' ') {
                FontPack.Glyph spaceGlyph = font.getGlyph('A');
                curX += (spaceGlyph != null ? spaceGlyph.advance : 20) * scale + 6;
                continue;
            }
            FontPack.Glyph glyph = font.getGlyph(c);
            if (glyph == null) continue;
            float tw = glyph.width * scale, th = glyph.height * scale;
            float u0 = glyph.x / (float)tex.width, v0 = (glyph.y + glyph.height) / (float)tex.height;
            float u1 = (glyph.x + glyph.width) / (float)tex.width, v1 = glyph.y / (float)tex.height;
            verts.put(new float[]{ curX,y, u0,v0, curX+tw,y, u1,v0, curX+tw,y+th, u1,v1, curX,y+th, u0,v1 });
            curX += glyph.advance * scale + 6;
        }
        verts.flip();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4*Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4*Float.BYTES, 2*Float.BYTES);
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_QUADS, 0, verts.limit()/4);
        glBindVertexArray(0);
    }

    public void setScreenSize(int w, int h) { screenW = w; screenH = h; }
    public void begin() { uiRenderer.begin(); }
    public void drawDimOverlay() { uiRenderer.drawDimOverlay(); }
    public void drawButton(float x, float y, float w, float h, float r, float g, float b, float a) {
        uiRenderer.drawButton(x, y, w, h, r, g, b, a);
    }

    public void renderForest(EncyclopediaLayout layout) {
        for (int i = 0; i < 6; i++) {
            drawSheet(i, layout);
        }
    }

    private void drawSheet(int index, EncyclopediaLayout layout) {
        float x = layout.getNodeX(index);
        float y = layout.getNodeY(index);
        float w = layout.getNodeW(index);
        float h = layout.getNodeH(index);
        uiRenderer.drawButton(x, y, w, h, 0.0f, 0.0f, 0.0f, 0.7f);
        if (leafTextures[index] != 0) {
            uiRenderer.getShader().use();
            uiRenderer.getShader().setUniform1i("uUseTexture", 1);
            uiRenderer.getShader().setUniform3f("uTextColor", 1,1,1);
            glActiveTexture(GL_TEXTURE0);
            glBindTexture(GL_TEXTURE_2D, leafTextures[index]);
            uiRenderer.getShader().setUniform1i("uTexture", 0);
            float[] verts = { x,y, 0,0,  x,y+h, 0,1,  x+w,y+h, 1,1,  x+w,y, 1,0 };
            FloatBuffer buf = BufferUtils.createFloatBuffer(verts.length);
            buf.put(verts).flip();
            int vao = glGenVertexArrays();
            glBindVertexArray(vao);
            int vbo = glGenBuffers();
            glBindBuffer(GL_ARRAY_BUFFER, vbo);
            glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
            glVertexAttribPointer(0, 2, GL_FLOAT, false, 4*Float.BYTES, 0);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(1, 2, GL_FLOAT, false, 4*Float.BYTES, 2*Float.BYTES);
            glEnableVertexAttribArray(1);
            glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
            glDeleteBuffers(vbo);
            glDeleteVertexArrays(vao);
        }
    }

    public void drawText(String text, float x, float y, float scale, float r, float g, float b) {
        drawTextStatic(text, x, y, scale, r, g, b, runicFont, textShader, textVAO, textVBO, screenW, screenH);
    }

    public void dispose() {
        for (int tex : leafTextures) if (tex != 0) glDeleteTextures(tex);
        if (runicFont != null) runicFont.dispose();
        if (textShader != null) textShader.dispose();
        glDeleteBuffers(textVBO);
        glDeleteVertexArrays(textVAO);
    }
}
