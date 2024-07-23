package org.game3d.dev.engine.graph;

import imgui.ImDrawData;
import lombok.Getter;

import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

@Getter
public class GuiMesh {
    private final int indicesVBO;
    private final int vaoId;
    private final int verticesVBO;

    public GuiMesh() {
        this.vaoId = glGenVertexArrays();
        glBindVertexArray(this.vaoId);

        this.verticesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, this.verticesVBO);
        glEnableVertexAttribArray(0);
        glVertexAttribPointer(0, 2, GL_FLOAT, false, ImDrawData.SIZEOF_IM_DRAW_VERT, 0);
        glEnableVertexAttribArray(1);
        glVertexAttribPointer(1, 2, GL_FLOAT, false, ImDrawData.SIZEOF_IM_DRAW_VERT, 8);
        glEnableVertexAttribArray(2);
        glVertexAttribPointer(2, 4, GL_UNSIGNED_BYTE, true, ImDrawData.SIZEOF_IM_DRAW_VERT, 16);

        this.indicesVBO = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, 0);
        glBindVertexArray(0);
    }

    public void cleanup() {
        glDeleteBuffers(this.indicesVBO);
        glDeleteBuffers(this.verticesVBO);
        glDeleteVertexArrays(this.vaoId);
    }
}
