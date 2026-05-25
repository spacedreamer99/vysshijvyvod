package org.narrativ27.vysshijvyvod;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.*;
import org.lwjgl.*;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryUtil;

public class ModelLoader
{
    public static LoadedModel load(String path)
    {
        AIScene scene = Assimp.aiImportFile(path,
            Assimp.aiProcess_Triangulate | Assimp.aiProcess_FlipUVs | Assimp.aiProcess_GenNormals);
        if (scene == null)
            throw new RuntimeException("Не удалось загрузить модель: " + path);

        AIMesh mesh = AIMesh.create(scene.mMeshes().get(0));
        int numVertices = mesh.mNumVertices();
        int numFaces = mesh.mNumFaces();

        float[] vertices = new float[numVertices * 8];
        AIVector3D.Buffer positions = mesh.mVertices();
        AIVector3D.Buffer normals = mesh.mNormals();
        AIVector3D.Buffer texCoords = mesh.mTextureCoords(0);

        for (int i = 0; i < numVertices; i++)
        {
            AIVector3D pos = positions.get(i);
            AIVector3D nor = normals.get(i);
            int offset = i * 8;
            vertices[offset] = pos.x();
            vertices[offset + 1] = pos.y();
            vertices[offset + 2] = pos.z();
            if (texCoords != null)
            {
                vertices[offset + 3] = texCoords.get(i).x();
                vertices[offset + 4] = 1.0f - texCoords.get(i).y();
            }
            vertices[offset + 5] = nor.x();
            vertices[offset + 6] = nor.y();
            vertices[offset + 7] = nor.z();
        }

        int[] indices = new int[numFaces * 3];
        AIFace.Buffer faces = mesh.mFaces();
        for (int i = 0; i < numFaces; i++)
        {
            AIFace face = faces.get(i);
            indices[i * 3] = face.mIndices().get(0);
            indices[i * 3 + 1] = face.mIndices().get(1);
            indices[i * 3 + 2] = face.mIndices().get(2);
        }

        Mesh loadedMesh = new Mesh(vertices, indices);

        Texture texture = null;
        AIMaterial material = AIMaterial.create(scene.mMaterials().get(mesh.mMaterialIndex()));
        AIString texPath = AIString.calloc();
        if (Assimp.aiGetMaterialTexture(material, Assimp.aiTextureType_DIFFUSE, 0, texPath, (IntBuffer)null,
                null, null, null, null, null) == Assimp.aiReturn_SUCCESS)
        {
            String texFile = texPath.dataString();
            if (!texFile.startsWith("*"))
            {
                String modelDir = path.substring(0, path.lastIndexOf('/') + 1);
                try {
                    texture = new Texture(modelDir + texFile);
                } catch (RuntimeException e) {
                }
            }
        }

        Assimp.aiReleaseImport(scene);
        return new LoadedModel(loadedMesh, texture);
    }

    public static class LoadedModel
    {
        public final Mesh mesh;
        public Texture texture;
        public Texture roughness;
        public Texture normal;

        public LoadedModel(Mesh mesh, Texture texture)
        {
            this.mesh = mesh;
            this.texture = texture;
        }
    }
}
