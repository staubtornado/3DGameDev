package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL30.*;
import static org.lwjgl.stb.STBImage.*;

public class Texture {
    private int textureId;
    @Getter private String texturePath;

    public Texture(int width, int height, ByteBuffer buffer) {
        this.texturePath = "";
        this.generateTexture(width, height, buffer);
    }

    public Texture(String texturePath) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            this.texturePath = texturePath;
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer channels = stack.mallocInt(1);

            ByteBuffer buffer = stbi_load(texturePath, w, h, channels, 4);

            if (buffer == null) {
                throw new RuntimeException(String.format(
                        "Failed to load a texture file %s: %s", this.texturePath, stbi_failure_reason())
                );
            }

            int width = w.get();
            int height = h.get();
            this.generateTexture(width, height, buffer);
            stbi_image_free(buffer);
        }
    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.textureId);
    }

    public void cleanup() {
        glDeleteTextures(this.textureId);
    }

    private void generateTexture(int width, int height, ByteBuffer buffer) {
        this.textureId = glGenTextures();

        glBindTexture(GL_TEXTURE_2D, this.textureId);
        glPixelStorei(GL_UNPACK_ALIGNMENT, 1);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, buffer);
        glGenerateMipmap(GL_TEXTURE_2D);
    }
}
