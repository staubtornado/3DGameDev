package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL30;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class Mesh {
    @Getter private final int numVertices;
    @Getter private final int vaoId;
    private final List<Integer> vboIdList;

    public static final int MAX_WEIGHTS = 4;

    public Mesh(
            float[] positions,
            float[] normals,
            float[] tangents,
            float[] biTangents,
            float[] textCoords,
            int[] indices) {
        this(
                positions, normals, tangents, biTangents, textCoords, indices,
                new int[Mesh.MAX_WEIGHTS * positions.length / 3], new float[Mesh.MAX_WEIGHTS * positions.length / 3]
        );
    }

    public Mesh(
            float @NotNull [] positions,
            float @NotNull [] normals,
            float @NotNull [] tangents,
            float @NotNull [] biTangents,
            float @NotNull [] textCoords,
            int @NotNull [] indices,
            int[] boneIndices,
            float[] weights
    ) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.numVertices = indices.length;
            this.vboIdList = new ArrayList<>();

            this.vaoId = glGenVertexArrays();
            glBindVertexArray(this.vaoId);

            // positions
            int vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            FloatBuffer positionsBuffer = stack.callocFloat(positions.length);
            positionsBuffer.put(0, positions);
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, positionsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(0);
            glVertexAttribPointer(0, 3, GL_FLOAT, false, 0, 0);

            // normals
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            FloatBuffer normalsBuffer = stack.callocFloat(normals.length);
            normalsBuffer.put(0, normals);
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, normalsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(1);
            glVertexAttribPointer(1, 3, GL_FLOAT, false, 0, 0);

            // tangents
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            FloatBuffer tangentsBuffer = stack.callocFloat(tangents.length);
            tangentsBuffer.put(0, tangents);
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, tangentsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(2);
            glVertexAttribPointer(2, 3, GL_FLOAT, false, 0, 0);

            // biTangents
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            FloatBuffer biTangentsBuffer = stack.callocFloat(biTangents.length);
            biTangentsBuffer.put(0, biTangents);
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, biTangentsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(3);
            glVertexAttribPointer(3, 3, GL_FLOAT, false, 0, 0);

            // texture coordinates
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            FloatBuffer textureCoordsBuffer = stack.callocFloat(textCoords.length);
            textureCoordsBuffer.put(0, textCoords);
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, textureCoordsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(4);
            glVertexAttribPointer(4, 2, GL_FLOAT, false, 0, 0);

            //Bone weights
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            FloatBuffer weightsBuffer = stack.callocFloat(weights.length);
            weightsBuffer.put(weights).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, weightsBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(5);
            glVertexAttribPointer(5, 4, GL_FLOAT, false, 0, 0);

            //Bone indices
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            IntBuffer boneIndicesBuffer = stack.callocInt(boneIndices.length);
            boneIndicesBuffer.put(boneIndices).flip();
            glBindBuffer(GL_ARRAY_BUFFER, vboId);
            glBufferData(GL_ARRAY_BUFFER, boneIndicesBuffer, GL_STATIC_DRAW);
            glEnableVertexAttribArray(6);
            glVertexAttribPointer(6, 4, GL_FLOAT, false, 0, 0);

            // indices
            vboId = glGenBuffers();
            this.vboIdList.add(vboId);
            IntBuffer indicesBuffer = stack.callocInt(indices.length);
            indicesBuffer.put(0, indices);
            glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, vboId);
            glBufferData(GL_ELEMENT_ARRAY_BUFFER, indicesBuffer, GL_STATIC_DRAW);

            glBindBuffer(GL_ARRAY_BUFFER, 0);
            glBindVertexArray(0);
        }
    }

    public void cleanup() {
        this.vboIdList.forEach(GL30::glDeleteBuffers);
        glDeleteVertexArrays(this.vaoId);
    }
}
