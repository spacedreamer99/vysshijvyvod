package org.narrativ27.vysshijvyvod;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;

public class MainMenu {
    private UIRenderer uiRenderer;
    private FontPack runicFont;
    private Shader textShader;
    private int textVAO, textVBO;
    private int screenW = 1920, screenH = 1080;

    private float btn1X, btn1Y, btn1W = 600, btn1H = 80;
    private float btn2X, btn2Y, btn2W = 600, btn2H = 80;
    private boolean hover1, hover2;
    private boolean press1, press2;
    private boolean playClicked = false;

    // Подтверждение выхода
    private boolean exitConfirmation = false;
    private long confirmStartTime = 0;
    private static final long CONFIRM_TIMEOUT = 2_000_000_000L;

    public MainMenu(UIRenderer uiRenderer) {
        this.uiRenderer = uiRenderer;
    }

    public void init() throws Exception {
        runicFont = new FontPack("resources/fonts/runic");
        textShader = new Shader("resources/shaders/ui.vert", "resources/shaders/ui.frag");
        textVAO = glGenVertexArrays();
        textVBO = glGenBuffers();
        recalc();
    }

    public void setScreenSize(int w, int h) {
        screenW = w; screenH = h;
        uiRenderer.updateScreenSize(w, h);
        recalc();
    }

    private void recalc() {
        float cx = screenW / 2f;
        float cy = screenH / 2f;
        btn1X = cx - btn1W/2; btn1Y = cy + 20;
        btn2X = cx - btn2W/2; btn2Y = cy - 100;
    }

    public boolean isPlayClicked() { return playClicked; }
    public void reset() { playClicked = false; exitConfirmation = false; recalc(); }

    public void updateHover(double mx, double my) {
        float glY = screenH - (float)my;
        hover1 = mx >= btn1X && mx <= btn1X + btn1W && glY >= btn1Y && glY <= btn1Y + btn1H;
        hover2 = mx >= btn2X && mx <= btn2X + btn2W && glY >= btn2Y && glY <= btn2Y + btn2H;
    }

    public void mouseButton(double mx, double my, int button, int action) {
        if (button != 0) return;
        float glY = screenH - (float)my;
        if (action == 1) {
            press1 = hover1;
            press2 = hover2;
        } else if (action == 0) {
            if (press1 && hover1) {
                playClicked = true;
                exitConfirmation = false; // сброс подтверждения
            }
            else if (press2 && hover2) {
                // Подтверждение выхода
                if (!exitConfirmation) {
                    exitConfirmation = true;
                    confirmStartTime = System.nanoTime();
                } else {
                    long now = System.nanoTime();
                    if (now - confirmStartTime <= CONFIRM_TIMEOUT) {
                        GameState.shouldCloseRequested = true;
                    } else {
                        // таймаут истёк — начинаем заново
                        exitConfirmation = true;
                        confirmStartTime = now;
                    }
                }
            } else {
                // Клик мимо кнопок сбрасывает подтверждение
                exitConfirmation = false;
            }
            press1 = press2 = false;
        }
    }

    public void render() {
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        uiRenderer.begin();
        uiRenderer.drawDimOverlay();

        // Автосброс подтверждения по таймауту
        if (exitConfirmation && System.nanoTime() - confirmStartTime > CONFIRM_TIMEOUT) {
            exitConfirmation = false;
        }

        drawButton(btn1X, btn1Y, btn1W, btn1H, "PLAY GAME", press1, hover1);
        drawButton(btn2X, btn2Y, btn2W, btn2H, exitConfirmation ? "PRESS AGAIN" : "QUIT GAME", press2, hover2);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void drawButton(float x, float y, float w, float h, String label, boolean p, boolean hv) {
        float r = p ? 0.1f : (hv ? 0.35f : 0.2f);
        uiRenderer.drawButton(x, y, w, h, r, r, r, 0.7f);
        float scale = 0.5f;
        float tw = getTextWidth(label, scale);
        float th = getTextHeight(scale);
        drawText(label, x + (w - tw)/2, y + (h - th)/2, scale, 1,1,1);
    }

    private void drawText(String text, float x, float y, float scale, float r, float g, float b) {
        if (runicFont == null) return;
        Texture tex = runicFont.getTexture();
        if (tex == null) return;
        textShader.use();
        FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
        new Matrix4f().ortho(0, screenW, 0, screenH, -1, 1).get(matBuf);
        textShader.setUniformMatrix4fv("uProjection", matBuf);
        tex.bind(0);
        textShader.setUniform1i("uTexture", 0);
        textShader.setUniform1i("uUseTexture", 1);
        textShader.setUniform3f("uTextColor", r, g, b);

        float curX = x;
        FloatBuffer verts = BufferUtils.createFloatBuffer(text.length() * 6 * 4);
        for (int i = 0; i < text.length(); i++) {
            char c = Character.toUpperCase(text.charAt(i));
            if (c == ' ') {
                FontPack.Glyph spaceG = runicFont.getGlyph('A');
                curX += (spaceG != null ? spaceG.advance : 20) * scale + 6;
                continue;
            }
            FontPack.Glyph glyph = runicFont.getGlyph(c);
            if (glyph == null) continue;
            float tw = glyph.width * scale, th = glyph.height * scale;
            float u0 = glyph.x / (float)tex.width, v0 = (glyph.y + glyph.height) / (float)tex.height;
            float u1 = (glyph.x + glyph.width) / (float)tex.width, v1 = glyph.y / (float)tex.height;
            verts.put(new float[]{ curX, y, u0, v0, curX+tw, y, u1, v0, curX+tw, y+th, u1, v1, curX, y+th, u0, v1 });
            curX += glyph.advance * scale + 6;
        }
        verts.flip();
        glBindVertexArray(textVAO);
        glBindBuffer(GL_ARRAY_BUFFER, textVBO);
        glBufferData(GL_ARRAY_BUFFER, verts, GL_STATIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_QUADS, 0, verts.limit() / 4);
        glBindVertexArray(0);
    }

    private float getTextWidth(String text, float scale) {
        float w = 0;
        for (char c : text.toUpperCase().toCharArray()) {
            if (c == ' ') { w += 15; continue; }
            FontPack.Glyph g = runicFont.getGlyph(c);
            w += (g != null ? g.advance : 20) * scale + 6;
        }
        return w;
    }

    private float getTextHeight(float scale) {
        FontPack.Glyph g = runicFont.getGlyph('A');
        if (g == null) return 20 * scale;
        return g.height * scale;
    }

    public void dispose() {
        if (runicFont != null) runicFont.dispose();
        if (textShader != null) textShader.dispose();
        glDeleteBuffers(textVBO);
        glDeleteVertexArrays(textVAO);
    }
}
