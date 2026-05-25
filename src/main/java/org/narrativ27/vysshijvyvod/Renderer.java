package org.narrativ27.vysshijvyvod;
import java.io.IOException;
import java.nio.FloatBuffer;
import org.lwjgl.BufferUtils;
import org.joml.Matrix4f;
import static org.lwjgl.opengl.GL11.*;

public class Renderer
{
    private Shader meshShader;
    private SceneResources scene;
    private Camera camera;
    private ShaderBinder binder = new ShaderBinder();
    private FloatBuffer modelBuf = BufferUtils.createFloatBuffer(16);
    private FloatBuffer viewBuf  = BufferUtils.createFloatBuffer(16);
    private FloatBuffer projBuf  = BufferUtils.createFloatBuffer(16);
    private Matrix4f projectionMatrix = new Matrix4f();

    public Renderer()
    {
    }

    public void init() throws IOException
    {
        meshShader = new Shader("resources/shaders/mesh3d.vert", "resources/shaders/pbr.frag");
        scene = new SceneResources();
        scene.init();
        camera = new Camera();
    }

    public void updateProjection(int w, int h)
    {
        projectionMatrix = camera.getProjectionMatrix((float)w / h);
    }

    public void clear()
    {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glClearColor(0.0f, 0.0f, 0.0f, 1.0f);
    }

    public void render()
    {
        Matrix4f model1 = new Matrix4f();
        model1.get(modelBuf);
        camera.getViewMatrix().get(viewBuf);
        projectionMatrix.get(projBuf);

        meshShader.use();
        binder.sendTextures(meshShader, scene.planet);
        binder.sendMatrices(meshShader, modelBuf, viewBuf, projBuf);
        binder.sendLightAndPbr(meshShader, scene, camera);
        meshShader.setUniform1f("uExposure", 1.0f); // фиксированная экспозиция

        scene.planet.mesh.draw();

        Matrix4f model2 = new Matrix4f().translate(5.0f, 0.0f, -3.0f);
        model2.get(modelBuf);
        binder.sendMatrices(meshShader, modelBuf, viewBuf, projBuf);
        scene.planet.mesh.draw();
    }

    public Camera getCamera() { return camera; }

    public void dispose()
    {
        if (meshShader != null) meshShader.dispose();
        if (scene != null) scene.dispose();
    }
}
