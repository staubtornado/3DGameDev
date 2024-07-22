package org.game3d.dev.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;

@Getter
public class Projection {
    private static final float FOV = (float) Math.toRadians(60.0f);
    private static final float Z_NEAR = 0.01f;
    private static final float Z_FAR = 1000.0f;

    private final Matrix4f projectionMatrix;

    public Projection(int width, int height) {
        this.projectionMatrix = new Matrix4f();
        updateProjMatrix(width, height);
    }

    public void updateProjMatrix(int width, int height) {
        this.projectionMatrix.setPerspective(FOV, (float) width / height, Z_NEAR, Z_FAR);
    }
}
