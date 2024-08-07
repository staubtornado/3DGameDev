package org.game3d.dev.engine.scene;

import lombok.Getter;
import org.game3d.dev.engine.graph.Model;
import org.joml.Matrix4f;

import java.util.Arrays;

@Getter
public class AnimationData {
    public static final Matrix4f[] DEFAULT_BONE_MATRICES = new Matrix4f[ModelLoader.MAX_BONES];

    static {
        Matrix4f zeroMatrix = new Matrix4f().zero();
        Arrays.fill(DEFAULT_BONE_MATRICES, zeroMatrix);
    }

    private Model.Animation currentAnimation;
    private int currentFrameIdx;

    public AnimationData(Model.Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
        this.currentFrameIdx = 0;
    }

    public Model.AnimatedFrame getCurrentFrame() {
        return this.currentAnimation.frames().get(this.currentFrameIdx);
    }

    public void setCurrentAnimation(Model.Animation currentAnimation) {
        this.currentAnimation = currentAnimation;
        this.currentFrameIdx = 0;
    }

    public void nextFrame() {
        int nextFrame = this.currentFrameIdx + 1;
        if (nextFrame > this.currentAnimation.frames().size() - 1) {
            this.currentFrameIdx = 0;
        } else {
            this.currentFrameIdx = nextFrame;
        }
    }
}
