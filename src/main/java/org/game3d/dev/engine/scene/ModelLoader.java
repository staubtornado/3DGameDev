package org.game3d.dev.engine.scene;

import lombok.NoArgsConstructor;
import org.game3d.dev.engine.Utils;
import org.game3d.dev.engine.graph.*;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector4f;
import org.lwjgl.PointerBuffer;
import org.lwjgl.assimp.*;
import org.lwjgl.system.MemoryStack;

import java.io.File;
import java.io.FileNotFoundException;
import java.nio.IntBuffer;
import java.util.*;

import static org.lwjgl.assimp.Assimp.*;

@NoArgsConstructor
public class ModelLoader {
    public static final int MAX_BONES = 150;
    private static final Matrix4f IDENTITY_MATRIX = new Matrix4f();

    @Contract(pure = true)
    public static @NotNull Model loadModel(String modelId, String modelPath, TextureCache textureCache, boolean animation) {
        return loadModel(
                modelId, modelPath,  textureCache,
                aiProcess_GenSmoothNormals | aiProcess_JoinIdenticalVertices | aiProcess_Triangulate |
                        aiProcess_FixInfacingNormals | aiProcess_CalcTangentSpace | aiProcess_LimitBoneWeights |
                        (animation ? 0 : aiProcess_PreTransformVertices)
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
            List<Bone> bones = new ArrayList<>();

            for (int i = 0; i < numMeshes; i++) {
                assert aiMeshes != null;
                try (AIMesh aiMesh = AIMesh.create(aiMeshes.get(i))) {
                    Mesh mesh = processMesh(aiMesh, bones);
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

            List<Model.Animation> animations = new ArrayList<>();
            int numAnimations = aiScene.mNumAnimations();
            if (numAnimations > 0) {
                Node rootNode = buildNodesTree(Objects.requireNonNull(aiScene.mRootNode()), null);
                Matrix4f globalInverseTransform = toMatrix(Objects.requireNonNull(aiScene.mRootNode()).mTransformation()).invert();
                animations = processAnimations(aiScene, bones, rootNode, globalInverseTransform);
            }
            aiReleaseImport(aiScene);
            return new Model(modelId, materials, animations);

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private static @NotNull Node buildNodesTree(@NotNull AINode aiNode, Node parentNode) {
        String nodeName = aiNode.mName().dataString();
        Node node = new Node(nodeName, parentNode, toMatrix(aiNode.mTransformation()));

        int numChildren = aiNode.mNumChildren();
        PointerBuffer aiChildren = aiNode.mChildren();
        if (aiChildren == null) {
            return node;
        }
        for (int i = 0; i < numChildren; i++) {
            try (AINode aiChildNode = AINode.create(aiChildren.get(i))) {
                Node childNode = buildNodesTree(aiChildNode, node);
                node.addChild(childNode);
            }
        }
        return node;
    }

    @Contract("_ -> new")
    private static @NotNull Matrix4f toMatrix(@NotNull AIMatrix4x4 aiMatrix4x4) {
        return new Matrix4f(
                aiMatrix4x4.a1(), aiMatrix4x4.a2(), aiMatrix4x4.a3(), aiMatrix4x4.a4(),
                aiMatrix4x4.b1(), aiMatrix4x4.b2(), aiMatrix4x4.b3(), aiMatrix4x4.b4(),
                aiMatrix4x4.c1(), aiMatrix4x4.c2(), aiMatrix4x4.c3(), aiMatrix4x4.c4(),
                aiMatrix4x4.d1(), aiMatrix4x4.d2(), aiMatrix4x4.d3(), aiMatrix4x4.d4()
        );
    }

    private static @NotNull List<Model.Animation> processAnimations(
            @NotNull AIScene aiScene, List<Bone> bones, Node rootNode,
            Matrix4f globalInverseTransformation) {
        List<Model.Animation> animations = new ArrayList<>();

        int numAnimations = aiScene.mNumAnimations();
        PointerBuffer aiAnimations = aiScene.mAnimations();

        if (aiAnimations == null) {
            return animations;
        }

        for (int i = 0; i < numAnimations; i++) {
            try (AIAnimation aiAnimation = AIAnimation.create(aiAnimations.get(i))) {
                int maxFrames = calcAnimationMaxFrames(aiAnimation);

                List<Model.AnimatedFrame> frames = new ArrayList<>();
                Model.Animation animation = new Model.Animation(aiAnimation.mName().dataString(), aiAnimation.mDuration(), frames);
                animations.add(animation);

                for (int j = 0; j < maxFrames; j++) {
                    Matrix4f[] bonesMatrices = new Matrix4f[MAX_BONES];
                    Arrays.fill(bonesMatrices, IDENTITY_MATRIX);
                    Model.AnimatedFrame animatedFrame = new Model.AnimatedFrame(bonesMatrices);
                    buildFrameMatrices(aiAnimation, bones, animatedFrame, j, rootNode, rootNode.getNodeTransformation(), globalInverseTransformation);
                    frames.add(animatedFrame);
                }
            }
        }
        return animations;
    }

    private static int calcAnimationMaxFrames(@NotNull AIAnimation aiAnimation) {
        int maxFrames = 0;
        int numNodeAnimations = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();

        if (aiChannels == null) {
            return maxFrames;
        }
        for (int i = 0; i < numNodeAnimations; i++) {
            try (AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i))) {
                int numFrames = Math.max(
                        Math.max(aiNodeAnim.mNumPositionKeys(), aiNodeAnim.mNumScalingKeys()),
                        aiNodeAnim.mNumRotationKeys()
                );
                maxFrames = Math.max(maxFrames, numFrames);
            }
        }
        return maxFrames;
    }

    private static void buildFrameMatrices(
            AIAnimation aiAnimation, List<Bone> bones, Model.AnimatedFrame animatedFrame,
            int frame, @NotNull Node node, Matrix4f parentTransformation, Matrix4f globalInverseTransformation) {

        String nodeName = node.getName();
        AINodeAnim aiNodeAnim = findAIAnimNode(aiAnimation, nodeName);
        Matrix4f nodeTransform = node.getNodeTransformation();
        if (aiNodeAnim != null) {
            nodeTransform = buildNodeTransformationMatrix(aiNodeAnim, frame);
        }
        Matrix4f nodeGlobalTransform = new Matrix4f(parentTransformation).mul(nodeTransform);

        List<Bone> affectedBones = bones.stream().filter(b -> b.name().equals(nodeName)).toList();
        for (Bone bone : affectedBones) {
            Matrix4f boneTransform =  new Matrix4f(globalInverseTransformation).mul(nodeGlobalTransform)
                    .mul(bone.offsetMatrix());
            animatedFrame.boneMatrices()[bone.id()] = boneTransform;
        }

        for (Node childNode : node.getChildren()) {
            buildFrameMatrices(
                    aiAnimation, bones, animatedFrame, frame, childNode, nodeGlobalTransform,
                    globalInverseTransformation
            );
        }
    }

    private static @NotNull Matrix4f buildNodeTransformationMatrix(@NotNull AINodeAnim aiNodeAnim, int frame) {
        AIVectorKey.Buffer positionKeys = aiNodeAnim.mPositionKeys();
        AIVectorKey.Buffer scalingKeys = aiNodeAnim.mScalingKeys();
        AIQuatKey.Buffer rotationKeys = aiNodeAnim.mRotationKeys();

        AIVectorKey aiVectorKey;
        AIVector3D vector3D;

        Matrix4f nodeTransform = new Matrix4f();
        if (positionKeys == null || scalingKeys == null || rotationKeys == null) {
            return nodeTransform;
        }

        int numPositions = aiNodeAnim.mNumPositionKeys();
        if (numPositions > 0) {
            aiVectorKey = positionKeys.get(Math.min(numPositions - 1, frame));
            vector3D = aiVectorKey.mValue();
            nodeTransform.translate(vector3D.x(), vector3D.y(), vector3D.z());
        }

        int numRotations = aiNodeAnim.mNumRotationKeys();
        if (numRotations > 0) {
            AIQuatKey quatKey = rotationKeys.get(Math.min(numRotations - 1, frame));
            AIQuaternion quaternion = quatKey.mValue();
            Quaternionf quaternionf = new Quaternionf(quaternion.x(), quaternion.y(), quaternion.z(), quaternion.w());
            nodeTransform.rotate(quaternionf);
        }

        int numScalingKeys = aiNodeAnim.mNumScalingKeys();
        if (numScalingKeys > 0) {
            aiVectorKey = scalingKeys.get(Math.min(numScalingKeys  - 1, frame));
            vector3D = aiVectorKey.mValue();
            nodeTransform.scale(vector3D.x(), vector3D.y(), vector3D.z());
        }
        return nodeTransform;
    }

    private static @Nullable AINodeAnim findAIAnimNode(@NotNull AIAnimation aiAnimation, String nodeName) {
        AINodeAnim result = null;
        int numAnimNodes = aiAnimation.mNumChannels();
        PointerBuffer aiChannels = aiAnimation.mChannels();

        if (aiChannels == null) {
            return null;
        }

        for (int i = 0; i < numAnimNodes; i++) {
            AINodeAnim aiNodeAnim = AINodeAnim.create(aiChannels.get(i));
            if (nodeName.equals(aiNodeAnim.mNodeName().dataString())) {
                result = aiNodeAnim;
                break;
            }
        }
        return result;
    }

    private static int[] processIndices(@NotNull AIMesh aiMesh) {
        List<Integer> indices = new ArrayList<>();
        int numFaces = aiMesh.mNumFaces();
        AIFace.Buffer aiFaces = aiMesh.mFaces();
        for (int i = 0; i < numFaces; i++) {
            AIFace aiFace = aiFaces.get(i);
            IntBuffer buffer = aiFace.mIndices();
            while (buffer.hasRemaining()) {
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
                        AI_MATKEY_COLOR_AMBIENT,
                        aiTextureType_NONE,
                        0, color
                );
                if (result == aiReturn_SUCCESS) {
                    material.setAmbientColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
                }

                result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_DIFFUSE, aiTextureType_NONE, 0, color);
                if (result == aiReturn_SUCCESS) {
                    material.setDiffuseColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
                }

                result = aiGetMaterialColor(aiMaterial, AI_MATKEY_COLOR_SPECULAR, aiTextureType_NONE, 0, color);
                if (result == aiReturn_SUCCESS) {
                    material.setSpecularColor(new Vector4f(color.r(), color.g(), color.b(), color.a()));
                }

                float reflectance = 0.0f;
                float[] shininessFactor = new float[]{0.0f};
                int[] pMax = new int[]{1};
                result = aiGetMaterialFloatArray(
                        aiMaterial,
                        AI_MATKEY_SHININESS_STRENGTH,
                        aiTextureType_NONE,
                        0, shininessFactor, pMax
                );
                if (result != aiReturn_SUCCESS) {
                    reflectance = shininessFactor[0];
                }
                material.setReflectance(reflectance);
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

            AIString aiNormalMapPath = AIString.calloc(stack);
            Assimp.aiGetMaterialTexture(
                    aiMaterial, aiTextureType_NORMALS, 0, aiNormalMapPath,
                    (IntBuffer) null, null, null, null, null, null
            );
            String normalMapPath = aiNormalMapPath.dataString();
            if (!normalMapPath.isEmpty()) {
                material.setNormalMapPath(modelDir + File.separator + new File(normalMapPath).getName());
                textureCache.createTexture(material.getNormalMapPath());
            }
            return material;
        }
    }

    @Contract("_, _ -> new")
    private static @NotNull AnimMeshData processBones(@NotNull AIMesh aiMesh, List<Bone> bones) {
        List<Integer> boneIds = new ArrayList<>();
        List<Float> weights = new ArrayList<>();

        Map<Integer, List<VertexWeight>> weightSet = new HashMap<>();
        int numBones = aiMesh.mNumBones();
        PointerBuffer aiBones = aiMesh.mBones();

        if (aiBones == null) {
            return new AnimMeshData(new float[]{}, new int[]{});
        }

        for (int i = 0; i < numBones; i++) {
            try (AIBone aiBone = AIBone.create(aiBones.get(i))) {
                int id = bones.size();
                Bone bone = new Bone(id, aiBone.mName().dataString(), toMatrix(aiBone.mOffsetMatrix()));
                bones.add(bone);
                int numWeights = aiBone.mNumWeights();
                AIVertexWeight.Buffer aiWeights = aiBone.mWeights();

                for (int j = 0; j < numWeights; j++) {
                    AIVertexWeight aiWeight = aiWeights.get(j);
                    VertexWeight vw = new VertexWeight(id, aiWeight.mVertexId(), aiWeight.mWeight());
                    List<VertexWeight> vertexWeightList = weightSet.computeIfAbsent(
                            vw.vertexId(), _ -> new ArrayList<>()
                    );
                    vertexWeightList.add(vw);
                }
            }
        }

        int numVertices = aiMesh.mNumVertices();
        for (int i = 0; i < numVertices; i++) {
            List<VertexWeight> vertexWeightList = weightSet.get(i);
            int size = vertexWeightList != null ? vertexWeightList.size() : 0;
            for (int j = 0; j < Mesh.MAX_WEIGHTS; j++) {
                if (j < size) {
                    VertexWeight vw = vertexWeightList.get(j);
                    weights.add(vw.weight());
                    boneIds.add(vw.boneId());
                } else {
                    weights.add(0.0f);
                    boneIds.add(0);
                }
            }
        }
        return new AnimMeshData(Utils.mapFloatListToArray(weights), Utils.mapIntListToArray(boneIds));
    }

    private static @NotNull Mesh processMesh(AIMesh aiMesh, List<Bone> bones) {
        float[] vertices = processVertices(aiMesh);
        float[] normals = processNormals(aiMesh);
        float[] tangents = processTangents(aiMesh, normals);
        float[] biTangents = processBiTangents(aiMesh, normals);
        float[] textCoords = processTextCoords(aiMesh);
        int[] indices = processIndices(aiMesh);
        AnimMeshData animMeshData = processBones(aiMesh, bones);

        if (textCoords.length == 0) {
            int numElements = vertices.length / 3 * 2;
            textCoords = new float[numElements];
        }
        return new Mesh(
                vertices, normals, tangents, biTangents, textCoords, indices,
                animMeshData.boneIds, animMeshData.weights
        );
    }

    private static float @NotNull [] processNormals(@NotNull AIMesh aiMesh) {
        AIVector3D.Buffer buffer = aiMesh.mNormals();
        if (buffer == null) {
            return new float[]{};
        }
        return getData(0, buffer);
    }

    private static float @NotNull [] processTextCoords(@NotNull AIMesh aiMesh) {
        AIVector3D.Buffer buffer = aiMesh.mTextureCoords(0);
        if (buffer == null) {
            return new float[]{};
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
        return getData(0, buffer);
    }

    private static float @NotNull [] processBiTangents(@NotNull AIMesh aiMesh, float[] normals) {
        AIVector3D.Buffer buffer = aiMesh.mBitangents();
        if (buffer == null) {
            return new float[]{};
        }
        return getData(normals.length, buffer);
    }

    private static float @NotNull [] processTangents(@NotNull AIMesh aiMesh, float[] normals) {
        AIVector3D.Buffer buffer = aiMesh.mTangents();
        if (buffer == null) {
            return new float[]{};
        }
        return getData(normals.length, buffer);
    }

    private static float @NotNull [] getData(int length, AIVector3D.@NotNull Buffer buffer) {
        float[] data = new float[buffer.remaining() * 3];
        int pos = 0;
        while (buffer.hasRemaining()) {
            AIVector3D aiTangent = buffer.get();
            data[pos++] = aiTangent.x();
            data[pos++] = aiTangent.y();
            data[pos++] = aiTangent.z();
        }

        if (data.length == 0) {
            data = new float[length];
        }
        return data;
    }

    public record AnimMeshData(float[] weights, int[] boneIds) {}
    private record Bone(int id, String name, Matrix4f offsetMatrix) {}
    private record VertexWeight(int boneId, int vertexId, float weight) {}
}
