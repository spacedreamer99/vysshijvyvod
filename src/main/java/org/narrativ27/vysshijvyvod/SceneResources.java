package org.narrativ27.vysshijvyvod;
import java.io.IOException;
import org.joml.Vector3f;

public class SceneResources
{
    public ModelLoader.LoadedModel planet;
    public Vector3f lightPos = new Vector3f(2.0f, 3.0f, 4.0f);
    public Vector3f lightColor = new Vector3f(100.0f, 100.0f, 100.0f);

    public void init() throws IOException
    {
        planet = ModelLoader.load("resources/models/planets/p1/p1.gltf");
        if (planet.texture == null)
            planet.texture = new Texture("resources/models/planets/p1/p1_albedo.png");
        planet.roughness = new Texture("resources/models/planets/p1/p1_roughness.png");
    }

    public void dispose()
    {
        if (planet != null)
        {
            if (planet.mesh != null) planet.mesh.dispose();
            if (planet.texture != null) planet.texture.dispose();
            if (planet.roughness != null) planet.roughness.dispose();
        }
    }
}
