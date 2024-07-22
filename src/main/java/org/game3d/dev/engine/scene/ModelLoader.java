package org.game3d.dev.engine.scene;

import lombok.NoArgsConstructor;
import org.game3d.dev.engine.graph.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static org.lwjgl.assimp.Assimp.*;

@NoArgsConstructor
public class ModelLoader {
    @Contract(pure = true)
    public static @NotNull Model loadModel(String modelId, String modelPath, TextureCache textureCache) {
        return loadModel(
                modelId, modelPath, textureCache,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices
        );
    }

    @Contract(pure = true)
    public static @NotNull Model loadModel(String modelId, String modelPath, TextureCache textureCache, int flags) {
        File file = new File(modelPath);
        if (!file.exists()) {
            throw new RuntimeException("Model file not found: " + modelPath);
        }
        String modelDir = file.getParent();

        try (AIScene aiScene = aiImportFile(modelPath, flags)) {
            if (aiScene == null) {
                throw new FileNotFoundException("Error loading model: " + modelPath);
            }

            int numMaterials = aiScene.mNumMaterials();
            List<Material> materials = new ArrayList<>();
            for (int i = 0; i < numMaterials; i++) {
                try (AIMaterial aiMaterial = AIMaterial.create(Objects.requireNonNull(aiScene.mMaterials()).get(i))) {
                    materials.add(processMaterial(aiMaterial, modelDir, textureCache));
                }
            }

            int numMeshes = aiScene.mNumMeshes();
            PointerBuffer aiMeshes = aiScene.mMeshes();
            Material defaultMaterial = new Material();
            for (int i = 0; i < numMeshes; i++) {
                assert aiMeshes != null;
                try (AIMesh aiMesh = AIMesh.create(aiMeshes.get(i))) {
                    Mesh mesh = processMesh(aiMesh);
                    int materialIndex = aiMesh.mMaterialIndex();
                    Material material;
                    if (materialIndex >= 0 && materialIndex < materials.size()) {
                        material = materials.get(materialIndex);
                    } else {
                        material = defaultMaterial;
                    }
                    material.getMeshes().add(mesh);
                }
            }
            if (!defaultMaterial.getMeshes().isEmpty()) {
                materials.add(defaultMaterial);
            }
            return new Model(modelId, materials);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static int[] processIndices(@NotNull AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<>();
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.remaining() > 0) {
                indices.add(buffer.get());
            }
        }
        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    private static @NotNull Material processMaterial(
            AIMaterial aiMaterial, String modelDir, TextureCache textureCache
    ) {
        Material material = new Material();
        try (MemoryStack stack = MemoryStack.stackPush()) {
            try (AIColor4D color = AIColor4D.create()) {
                int result = aiGetMaterialColor(
                        aiMaterial,
                        AI_MATKEY_COLOR_DIFFUSE,
                        aiTextureType_NONE,
                        0, color
                );
                if (result == aiReturn_SUCCESS) {
                    material.setDiffuseColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
                }
            }

            AIString aiTexturePath = AIString.calloc(stack);
            aiGetMaterialTexture(
                    aiMaterial,
                    aiTextureType_DIFFUSE,
                    0,
                    aiTexturePath,
                    (IntBuffer) null, null, null, null, null, null
            );
            String texturePath = aiTexturePath.dataString();
            if (!texturePath.isEmpty()) {
                material.setTexturePath(modelDir + File.separator + new File(texturePath).getName());
                textureCache.createTexture(material.getTexturePath());
                material.setDiffuseColor(Material.DEFAULT_COLOR);
            }
        }
        return material;
    }

    @Contract("_ -> new")
    private static @NotNull Mesh processMesh(AIMesh aiMesh) {
        float[] vertices = processVertices(aiMesh);
        float[] textCoords = processTextCoords(aiMesh);
        int[] indices = processIndices(aiMesh);

        if (textCoords.length ==  0) {
            int numElements = vertices.length / 3 * 2;
            textCoords = new float[numElements];
        }
        return new Mesh(vertices, textCoords, indices);
    }

    private static float @NotNull [] processTextCoords(@NotNull AIMesh aiMesh) {
        AIVector3D.Buffer buffer = aiMesh.mTextureCoords(0);
        if (buffer == null) {
            return new float[0];
        }
        float[] data = new float[buffer.remaining() * 2];
        int pos = 0;
        while (buffer.remaining() > 0) {
            AIVector3D vec = buffer.get();
            data[pos++] = vec.x();
            data[pos++] = 1 - vec.y();
        }
        return data;
    }

    private static float @NotNull [] processVertices(@NotNull AIMesh aiMesh) {
        AIVector3D.Buffer buffer = aiMesh.mVertices();
        float[] data = new float[buffer.remaining() * 3];
        int pos = 0;
        while (buffer.remaining() > 0) {
            AIVector3D vec = buffer.get();
            data[pos++] = vec.x();
            data[pos++] = vec.y();
            data[pos++] = vec.z();
        }
        return data;
    }
}
