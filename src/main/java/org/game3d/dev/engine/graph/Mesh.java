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
    @Getter private int numVertices;
    @Getter private int vaoId;
    private final List<Integer> vboIdList;

    public Mesh(
            float @NotNull [] positions,
            float @NotNull [] normals,
            float @NotNull [] tangents,
            float @NotNull [] biTangents,
            float @NotNull [] textCoords,
            int @NotNull [] indices
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
