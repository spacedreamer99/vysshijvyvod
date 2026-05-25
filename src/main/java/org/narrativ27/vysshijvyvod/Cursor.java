package org.narrativ27.vysshijvyvod;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class Cursor {
    private Texture cursorTexture;
    private Shader uiShader;
    private int screenW = 1920, screenH = 1080;
    private static final float CUR_W = 32.0f, CUR_H = 32.0f;
    private boolean visible = true, hovered, pressed;
    private int vao, vbo;

    public void init() throws Exception {
        cursorTexture = new Texture("resources/textures/m1.png");
        uiShader = new Shader("resources/shaders/ui.vert", "resources/shaders/ui.frag");
        vao = glGenVertexArrays();
        vbo = glGenBuffers();
        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, 4 * 4 * Float.BYTES, GL_DYNAMIC_DRAW);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, 4 * Float.BYTES, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, 4 * Float.BYTES, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glBindVertexArray(0);
    }

    public void setVisible(boolean v) { visible = v; }
    public void setHover(boolean h) { hovered = h; }
    public void setPressed(boolean p) { pressed = p; }
    public void updateScreenSize(int w, int h) { screenW = w; screenH = h; }

    public void render(double mouseX, double mouseY) {
        if (!visible) return;
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
        new Matrix4f().ortho(0, screenW, 0, screenH, -1, 1).get(matBuf);
        uiShader.use();
        uiShader.setUniformMatrix4fv("uProjection", matBuf);
        uiShader.setUniform1i("uUseTexture", 1);

        float r = pressed ? 0.4f : (hovered ? 2.0f : 1.0f);
        float g = pressed ? 0.4f : (hovered ? 2.0f : 1.0f);
        float b = pressed ? 0.4f : (hovered ? 2.2f : 1.0f);
        uiShader.setUniform3f("uTextColor", r, g, b);
        cursorTexture.bind(0);

        float x = (float) mouseX;
        float y = screenH - (float) mouseY - CUR_H;
        float[] verts = { x, y, 0, 0,  x, y + CUR_H, 0, 1,  x + CUR_W, y + CUR_H, 1, 1,  x + CUR_W, y, 1, 0 };
        FloatBuffer buf = BufferUtils.createFloatBuffer(verts.length);
        buf.put(verts).flip();

        glBindVertexArray(vao);
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferSubData(GL_ARRAY_BUFFER, 0, buf);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        glBindVertexArray(0);

        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    public void dispose() {
        if (cursorTexture != null) cursorTexture.dispose();
        if (uiShader != null) uiShader.dispose();
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }
}
