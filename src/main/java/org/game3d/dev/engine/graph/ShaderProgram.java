package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.game3d.dev.engine.Utils;
import org.lwjgl.opengl.GL30;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

@Getter
public class ShaderProgram {
    private final int programId;

    public ShaderProgram(List<ShaderModuleData> shaderModuleDataList) {
        this.programId = glCreateProgram();
        if (this.programId == 0) {
            throw new RuntimeException("Could not create shader.");
        }

        List<Integer> shaderModules = new ArrayList<>();
        shaderModuleDataList.forEach(s -> shaderModules.add(createShader(Utils.readFile(s.shaderFile), s.shaderType)));
        link(shaderModules);
    }

    public void bind() {
        glUseProgram(this.programId);
    }

    public void cleanup() {
        unbind();
        if (this.programId != 0) {
            glDeleteProgram(this.programId);
        }
    }

    protected int createShader(String shaderCode, int shaderType) {
        int shaderId = glCreateShader(shaderType);
        if (shaderId == 0) {
            throw new RuntimeException(String.format("Error creating shader. Type: %d", shaderType));
        }

        glShaderSource(shaderId, shaderCode);
        glCompileShader(shaderId);

        if (glGetShaderi(shaderId, GL_COMPILE_STATUS) == 0) {
            throw new RuntimeException(String.format(
                    "Error compiling shader. Code: %s", glGetShaderInfoLog(shaderId, 1024)
            ));
        }

        glAttachShader(this.programId, shaderId);
        return shaderId;
    }

    private void link(List<Integer> shaderModules) {
        glLinkProgram(this.programId);
        if (glGetProgrami(this.programId, GL_LINK_STATUS) == 0) {
            throw new RuntimeException(String.format(
                    "Error linking shader. Code: %s", glGetProgramInfoLog(this.programId, 1024)
            ));
        }

        // Detach and delete the shader modules as they are no longer needed
        shaderModules.forEach(s -> glDetachShader(this.programId, s));
        shaderModules.forEach(GL30::glDeleteShader);
    }


    public void unbind() {
        glUseProgram(0);
    }

    public void validate() {
        glValidateProgram(this.programId);  // TODO: Remove in production
        if (glGetProgrami(this.programId, GL_VALIDATE_STATUS) == 0) {
            throw new RuntimeException(String.format(
                    "Error validating shader. Code: %s", glGetProgramInfoLog(this.programId, 1024)
            ));
        }
    }

    public record ShaderModuleData(String shaderFile, int shaderType) {}
}
