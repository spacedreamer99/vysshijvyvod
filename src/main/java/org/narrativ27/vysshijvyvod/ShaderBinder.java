package org.narrativ27.vysshijvyvod;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL13.*;

public class ShaderBinder
{
    public void sendMatrices(Shader shader, FloatBuffer model, FloatBuffer view, FloatBuffer proj)
    {
        shader.setUniformMatrix4fv("uModel", model);
        shader.setUniformMatrix4fv("uView", view);
        shader.setUniformMatrix4fv("uProjection", proj);
    }

    public void sendTextures(Shader shader, ModelLoader.LoadedModel model)
    {
        // Albedo (юнит 0)
        if (model.texture != null)
        {
            glActiveTexture(GL_TEXTURE0);
            model.texture.bind(0);
            shader.setUniform1i("uAlbedoMap", 0);
        }
        // Roughness (юнит 1)
        if (model.roughness != null)
        {
            glActiveTexture(GL_TEXTURE1);
            model.roughness.bind(1);
            shader.setUniform1i("uRoughnessMap", 1);
        }
        else
        {
            shader.setUniform1i("uHasMetallicMap", 0);
        }
        // Важно: всегда передаём uMetallic как запасное значение
        shader.setUniform1f("uMetallic", 0.0f);
    }

    public void sendLightAndPbr(Shader shader, SceneResources scene, Camera cam)
    {
        shader.setUniform3f("uLightPos", scene.lightPos.x, scene.lightPos.y, scene.lightPos.z);
        shader.setUniform3f("uLightColor", scene.lightColor.x, scene.lightColor.y, scene.lightColor.z);
        shader.setUniform3f("uViewPos", cam.getPosition().x, cam.getPosition().y, cam.getPosition().z);
    }
}
