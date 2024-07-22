package org.game3d.dev.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector2f;

public class Camera {
    private final Vector3f direction;
    @Getter private final Vector3f position;
    private final Vector2f rotation;
    private final Vector3f up;
    private final Vector3f right;
    @Getter private final Matrix4f viewMatrix;

    public Camera() {
        this.direction = new Vector3f();
        this.position = new Vector3f();
        this.rotation = new Vector2f();
        this.up = new Vector3f();
        this.right = new Vector3f();
        this.viewMatrix = new Matrix4f();
    }

    public void addRotation(float x, float y) {
        this.rotation.add(x, y);
        this.recalculate();
    }

    public void moveBackwards(float distance) {
        this.viewMatrix.positiveZ(this.direction).negate().mul(distance);
        this.position.sub(this.direction);
        this.recalculate();
    }

    public void moveDown(float distance) {
        this.viewMatrix.positiveY(this.up).mul(distance);
        this.position.sub(this.up);
        this.recalculate();
    }

    public void moveForward(float distance) {
        this.viewMatrix.positiveZ(this.direction).negate().mul(distance);
        this.position.add(this.direction);
        this.recalculate();
    }

    public void moveLeft(float distance) {
        this.viewMatrix.positiveX(this.right).mul(distance);
        this.position.sub(this.right);
        this.recalculate();
    }

    public void moveRight(float distance) {
        this.viewMatrix.positiveX(this.right).mul(distance);
        this.position.add(this.right);
        this.recalculate();
    }

    public void moveUp(float distance) {
        this.viewMatrix.positiveY(this.up).mul(distance);
        this.position.add(this.up);
        this.recalculate();
    }

    private void recalculate() {
        this.viewMatrix.identity()
                .rotateX(this.rotation.x)
                .rotateY(this.rotation.y)
                .translate(-this.position.x, -this.position.y, -this.position.z);
    }

    public void setPosition(float x, float y, float z) {
        this.position.set(x, y, z);
        this.recalculate();
    }

    public void setRotation(float x, float y) {
        this.rotation.set(x, y);
        this.recalculate();
    }
}
