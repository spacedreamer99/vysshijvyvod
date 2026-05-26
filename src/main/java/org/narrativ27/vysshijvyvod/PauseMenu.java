package org.narrativ27.vysshijvyvod;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;

public class PauseMenu
{
    private UIRenderer uiRenderer;
    private boolean visible = false;
    private int screenW = 1920, screenH = 1080;

    private FontPack runicFont;
    private Shader textShader;
    private int textVAO, textVBO;

    private static final int BTN_COUNT = 8;  // увеличено до 8
    private float[] btnX = new float[BTN_COUNT];
    private float[] btnY = new float[BTN_COUNT];
    private float[] btnW = new float[BTN_COUNT];
    private float[] btnH = new float[BTN_COUNT];
    private boolean[] hovered = new boolean[BTN_COUNT];
    private boolean[] pressed = new boolean[BTN_COUNT];

    private Runnable toggleFullscreenAction;
    private Runnable toggleVsyncAction;

    private boolean exitConfirmation = false;
    private long confirmStartTime = 0;
    private static final long CONFIRM_TIMEOUT = 2_000_000_000L;

    public PauseMenu(UIRenderer uiRenderer)
    {
        this.uiRenderer = uiRenderer;
    }

    public void init() throws Exception
    {
        runicFont = new FontPack("resources/fonts/runic");
        textShader = new Shader("resources/shaders/ui.vert", "resources/shaders/ui.frag");
        textVAO = glGenVertexArrays();
        textVBO = glGenBuffers();
        recalcButtons();
    }

    public void setToggleFullscreenAction(Runnable action) { this.toggleFullscreenAction = action; }
    public Runnable getToggleFullscreenAction() { return toggleFullscreenAction; }
    public void setToggleVsyncAction(Runnable action) { this.toggleVsyncAction = action; }

    public void show() { visible = true; exitConfirmation = false; }
    public void hide()
    {
        visible = false;
        exitConfirmation = false;
        for (int i = 0; i < BTN_COUNT; i++) { hovered[i] = false; pressed[i] = false; }
    }
    public boolean isVisible() { return visible; }

    public void updateScreenSize(int w, int h)
    {
        screenW = w; screenH = h;
        uiRenderer.updateScreenSize(w, h);
        recalcButtons();
    }

    public void updateHover(double mouseX, double mouseY)
    {
        if (!visible) return;
        float glY = screenH - (float)mouseY;
        for (int i = 0; i < BTN_COUNT; i++)
        {
            hovered[i] = mouseX >= btnX[i] && mouseX <= btnX[i] + btnW[i] &&
                          glY >= btnY[i] && glY <= btnY[i] + btnH[i];
        }
    }

    public void setButtonPressed(int index, boolean p)
    {
        if (index >= 0 && index < BTN_COUNT)
            pressed[index] = p;
    }

    public boolean tryExit()
    {
        long now = System.nanoTime();
        if (!exitConfirmation)
        {
            exitConfirmation = true;
            confirmStartTime = now;
            return false;
        }
        else
        {
            if (now - confirmStartTime <= CONFIRM_TIMEOUT)
            {
                return true;
            }
            else
            {
                exitConfirmation = true;
                confirmStartTime = now;
                return false;
            }
        }
    }

    public void resetExitConfirmation() { exitConfirmation = false; }

    public void doAction(int index)
    {
        // Сброс подтверждения для всех кнопок, кроме VSYNC, MAIN MENU и EXIT
        if (index != 5 && index != 6 && index != 7) resetExitConfirmation();

        switch (index)
        {
            case 0: break; // Continue
            case 1: break; // Encyclopedia
            case 2: if (toggleFullscreenAction != null) toggleFullscreenAction.run(); break;
            case 3: GameState.showFps = !GameState.showFps; break;
            case 4: // FPS Lock cycle
                    int cur = GameState.fpsLimit;
                    if (cur == 0) GameState.fpsLimit = 30;
                    else if (cur == 30) GameState.fpsLimit = 60;
                    else if (cur == 60) GameState.fpsLimit = 90;
                    else if (cur == 90) GameState.fpsLimit = 120;
                    else GameState.fpsLimit = 0;
                    break;
            case 5: // VSYNC toggle
                    GameState.vsync = !GameState.vsync;
                    if (toggleVsyncAction != null) toggleVsyncAction.run();
                    break;
            case 6: // MAIN MENU
                    GameState.returnToMainMenu = true;
                    break;
            case 7: // Exit
                    break;
        }
    }

    public int getButtonIndex(double mouseX, double mouseY)
    {
        if (!visible) return -1;
        float glY = screenH - (float)mouseY;
        for (int i = 0; i < BTN_COUNT; i++)
        {
            if (mouseX >= btnX[i] && mouseX <= btnX[i] + btnW[i] &&
                glY >= btnY[i] && glY <= btnY[i] + btnH[i])
                return i;
        }
        return -1;
    }

    public boolean isAnyButtonHovered()
    {
        for (boolean h : hovered) if (h) return true;
        return false;
    }

    private void recalcButtons()
    {
        float width = 1200;
        float height = 80;
        float centerX = screenW / 2.0f;
        float gap = 110;

        float totalHeight = height + (BTN_COUNT - 1) * gap;
        float startY = screenH / 2.0f + totalHeight / 2.0f - height;

        for (int i = 0; i < BTN_COUNT; i++)
        {
            btnX[i] = centerX - width / 2;
            btnY[i] = startY - i * gap;
            btnW[i] = width;
            btnH[i] = height;
        }
    }

    public void render()
    {
        if (!visible) return;

        if (exitConfirmation && System.nanoTime() - confirmStartTime > CONFIRM_TIMEOUT)
        {
            exitConfirmation = false;
        }

        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        uiRenderer.begin();
        uiRenderer.drawDimOverlay();

        drawButton(0, "CONTINUE PLAY GAME");
        drawButton(1, "ENCYCLOPEDIA OF GAME");
        drawButton(2, "WINDOW OR FULLSCREEN REGIME");
        drawButton(3, GameState.showFps ? "FRAME PER SECOND COUNTER: ON" : "FRAME PER SECOND COUNTER: OFF");
        drawButton(4, "FRAME PER SECOND MAXIMUM: " + (GameState.fpsLimit == 0 ? "OFF" : String.valueOf(GameState.fpsLimit)));
        drawButton(5, "VERTICAL SYNCHRONIZATION: " + (GameState.vsync ? "ON" : "OFF"));
        drawButton(6, "BACK TO MAIN MENU");
        drawButton(7, exitConfirmation ? "PRESS AGAIN" : "QUIT GAME");

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    private void drawButton(int idx, String label)
    {
        float r, g, b, a = 0.7f;
        if (pressed[idx])
        {
            r = 0.1f; g = 0.1f; b = 0.1f;
        }
        else if (hovered[idx])
        {
            r = 0.35f; g = 0.35f; b = 0.35f;
        }
        else
        {
            r = 0.2f; g = 0.2f; b = 0.2f;
        }
        uiRenderer.drawButton(btnX[idx], btnY[idx], btnW[idx], btnH[idx], r, g, b, a);

        float scale = 0.5f;
        float textW = getTextWidth(label, scale);
        float textH = getTextHeight(scale);
        float textX = btnX[idx] + (btnW[idx] - textW) / 2;
        float textY = btnY[idx] + (btnH[idx] - textH) / 2;
        drawText(label, textX, textY, scale, 1.0f, 1.0f, 1.0f);
    }

    public void drawText(String text, float x, float y, float scale, float r, float g, float b)
    {
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
        for (int i = 0; i < text.length(); i++)
        {
            char c = Character.toUpperCase(text.charAt(i));
            if (c == ' ')
            {
                FontPack.Glyph spaceG = runicFont.getGlyph('A');
                float spaceW = (spaceG != null) ? spaceG.advance * scale + 6 : 20;
                curX += spaceW;
                continue;
            }
            FontPack.Glyph glyph = runicFont.getGlyph(c);
            if (glyph == null) continue;
            float tw = glyph.width * scale;
            float th = glyph.height * scale;
            float u0 = glyph.x / (float)tex.width;
            float v0 = (glyph.y + glyph.height) / (float)tex.height;
            float u1 = (glyph.x + glyph.width) / (float)tex.width;
            float v1 = glyph.y / (float)tex.height;

            verts.put(new float[]{
                curX,      y,       u0, v0,
                curX+tw,   y,       u1, v0,
                curX+tw,   y+th,    u1, v1,
                curX,      y+th,    u0, v1
            });
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

    public float getTextWidth(String text, float scale)
    {
        if (runicFont == null) return 0;
        float w = 0;
        for (int i = 0; i < text.length(); i++)
        {
            char c = Character.toUpperCase(text.charAt(i));
            if (c == ' ')
            {
                FontPack.Glyph spaceG = runicFont.getGlyph('A');
                w += (spaceG != null) ? spaceG.advance * scale + 6 : 20;
                continue;
            }
            FontPack.Glyph g = runicFont.getGlyph(c);
            if (g != null) w += g.advance * scale + 6;
        }
        return w;
    }

    private float getTextHeight(float scale)
    {
        if (runicFont == null) return 0;
        FontPack.Glyph g = runicFont.getGlyph('A');
        if (g == null)
        {
            for (FontPack.Glyph any : runicFont.getGlyphs().values()) { g = any; break; }
        }
        if (g == null) return 0;
        return g.height * scale;
    }

    public void dispose()
    {
        if (runicFont != null) runicFont.dispose();
        if (textShader != null) textShader.dispose();
        glDeleteBuffers(textVBO);
        glDeleteVertexArrays(textVAO);
    }
}
