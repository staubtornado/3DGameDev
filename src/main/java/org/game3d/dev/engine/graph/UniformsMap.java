package org.game3d.dev.engine.graph;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

import java.nio.FloatBuffer;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.opengl.GL20.*;

public class UniformsMap {
    private final int programId;
    private final Map<String, Integer> uniforms;

    public UniformsMap(int programId) {
        this.programId = programId;
        this.uniforms = new HashMap<>();
    }

    public void createUniform(String uniformName) {
        int uniformLocation = glGetUniformLocation(this.programId, uniformName);
        if (uniformLocation < 0) {
            throw new RuntimeException("Could not find uniform:" + uniformName);
        }
        this.uniforms.put(uniformName, uniformLocation);
    }

    private int getUniformLocation(String uniformName) {
        Integer location = this.uniforms.get(uniformName);
        if (location == null) {
            throw new RuntimeException(String.format("Uniform %s not found", uniformName));
        }
        return location;
    }

    public void setUniform(String uniformName, int value) {
        glUniform1i(this.getUniformLocation(uniformName), value);
    }

    public void setUniform(String uniformName, @NotNull Matrix4f value) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            glUniformMatrix4fv(
                    this.getUniformLocation(uniformName),
                    false,
                    value.get(stack.mallocFloat(16))
            );
        }
    }

    public void setUniform(String uniformName, float value) {
        glUniform1f(this.getUniformLocation(uniformName), value);
    }

    public void setUniform(String uniformName, @NotNull Vector2f value) {
        glUniform2f(this.getUniformLocation(uniformName), value.x, value.y);
    }

    public void setUniform(String uniformName, @NotNull Vector3f value) {
        glUniform3f(this.getUniformLocation(uniformName), value.x, value.y, value.z);
    }

    public void setUniform(String uniformName, @NotNull Vector4f value) {
        glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }

    public void setUniform(String uniformName, Matrix4f[] matrices) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            int length = matrices != null ? matrices.length : 0;
            FloatBuffer floatBuffer = stack.mallocFloat(16 * length);
            for (int i = 0; i < length; i++) {
                matrices[i].get(16 * i, floatBuffer);
            }
            glUniformMatrix4fv(this.uniforms.get(uniformName), false, floatBuffer);
        }
    }
}
