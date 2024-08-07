package org.game3d.dev.engine.scene;

import lombok.Getter;
import lombok.Setter;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Vector3f;

@Getter
public class Entity {
    private final String id;
    private final String modelId;
    private final Matrix4f modelMatrix;
    private final Vector3f position;
    private final Quaternionf rotation;
    @Setter private AnimationData animationData;
    @Setter private float scale;

    public Entity(String id, String modelId) {
        this.id = id;
        this.modelId = modelId;
        this.modelMatrix = new Matrix4f();
        this.position = new Vector3f();
        this.rotation = new Quaternionf();
        this.scale = 1;
    }

    public final void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
    }

    public void setRotation(float x, float y, float z, float angle) {
        this.rotation.fromAxisAngleRad(x, y, z, angle);
    }

    public void updateModelMatrix() {
        this.modelMatrix.translationRotateScale(this.position, this.rotation, this.scale);
    }
}
