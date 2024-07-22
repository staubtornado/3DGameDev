package org.game3d.dev.engine.graph;

import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector4f;
import org.lwjgl.system.MemoryStack;

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

    public int getUniformLocation(String uniformName) {
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

    public void setUniform(String uniformName, @NotNull Vector4f value) {
        glUniform4f(this.getUniformLocation(uniformName), value.x, value.y, value.z, value.w);
    }
}
