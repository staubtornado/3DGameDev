package org.game3d.dev.engine.graph;

import lombok.Getter;

import static org.lwjgl.opengl.GL11.GL_DEPTH_COMPONENT;
import static org.lwjgl.opengl.GL30.*;

@Getter
public class ShadowBuffer {
    public static final int SHADOW_MAP_WIDTH = 4096;
    public static final int SHADOW_MAP_HEIGHT = 4096;

    private final ArrTexture depthMap;
    private final int depthMapFBO;

    public ShadowBuffer() {
        this.depthMapFBO = glGenFramebuffers();
        this.depthMap = new ArrTexture(
                CascadeShadow.SHADOW_MAP_CASCADE_COUNT, SHADOW_MAP_WIDTH, SHADOW_MAP_HEIGHT, GL_DEPTH_COMPONENT
        );

        glBindFramebuffer(GL_FRAMEBUFFER, this.depthMapFBO);
        glFramebufferTexture2D(GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D, this.depthMap.getIds()[0], 0);

        glDrawBuffer(GL_NONE);
        glReadBuffer(GL_NONE);

        if (glCheckFramebufferStatus(GL_FRAMEBUFFER) != GL_FRAMEBUFFER_COMPLETE) {
            throw new RuntimeException("Could not create FrameBuffer.");
        }

        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }

    public void bindTextures(int start) {
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glActiveTexture(start + i);
            glBindTexture(GL_TEXTURE_2D, this.depthMap.getIds()[i]);
        }
    }

    public void cleanup() {
        glDeleteFramebuffers(this.depthMapFBO);
        this.depthMap.cleanup();
    }
}
