package org.narrativ27.vysshijvyvod;
import java.nio.FloatBuffer;
import org.joml.Matrix4f;
import org.lwjgl.BufferUtils;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.*;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class UIRenderer
{
    private Shader uiShader;
    private FloatBuffer matBuf = BufferUtils.createFloatBuffer(16);
    private int screenW = 1920, screenH = 1080;

    public void init() throws Exception
    {
        uiShader = new Shader("resources/shaders/ui.vert", "resources/shaders/ui.frag");
    }

    public void updateScreenSize(int w, int h) { screenW = w; screenH = h; }

    public int getScreenWidth() { return screenW; }
    public int getScreenHeight() { return screenH; }

    public void begin()
    {
        Matrix4f ortho = new Matrix4f().ortho(0, screenW, 0, screenH, -1, 1);
        ortho.get(matBuf);
        uiShader.use();
        uiShader.setUniformMatrix4fv("uProjection", matBuf);
    }

    public void drawDimOverlay()
    {
        uiShader.setUniform1i("uUseTexture", 0);
        uiShader.setUniform3f("uColor", 0, 0, 0);
        uiShader.setUniform1f("uAlpha", 0.5f);
        drawQuad(0, 0, screenW, screenH);
    }

    public void drawButton(float x, float y, float w, float h, float r, float g, float b, float a)
    {
        uiShader.setUniform1i("uUseTexture", 0);
        uiShader.setUniform3f("uColor", r, g, b);
        uiShader.setUniform1f("uAlpha", a);
        drawQuad(x, y, w, h);
    }

    public void drawQuad(float x, float y, float w, float h)
    {
        int vao = glGenVertexArrays();
        glBindVertexArray(vao);
        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        float[] verts = { x, y, 0, 0, x, y + h, 0, 1, x + w, y + h, 1, 1, x + w, y, 1, 0 };
        FloatBuffer buf = BufferUtils.createFloatBuffer(verts.length);
        buf.put(verts).flip();
        glBufferData(GL_ARRAY_BUFFER, buf, GL_STATIC_DRAW);
        int stride = 4 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);
        glDrawArrays(GL_TRIANGLE_FAN, 0, 4);
        glDeleteBuffers(vbo);
        glDeleteVertexArrays(vao);
    }

    public Shader getShader() { return uiShader; }

    public void dispose()
    {
        if (uiShader != null) uiShader.dispose();
    }
}
