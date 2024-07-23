package org.game3d.dev.engine.scene;

import lombok.Getter;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector2f;

public class Camera {
    @Getter private final Vector3f position;
    private final Vector2f rotation;
    private final Vector3f right;
    @Getter private final Matrix4f viewMatrix;

    public Camera() {
        this.position = new Vector3f();
        this.rotation = new Vector2f();
        this.right = new Vector3f();
        this.viewMatrix = new Matrix4f();
        this.recalculate();
    }

    public void addRotation(float x, float y) {
        if (this.rotation.x + x > Math.PI / 2) {  // Prevent camera from going upside down
            this.rotation.x = (float) (Math.PI / 2);
        } else if (this.rotation.x + x < -Math.PI / 2) {
            this.rotation.x = (float) (-Math.PI / 2);
        } else {
            this.rotation.x += x;
        }
        this.rotation.y += y;

        if (this.rotation.y > 2 * Math.PI) {  // Keep rotation within 0 to 2*PI
            this.rotation.y = y;
        } else if (this.rotation.y < 0) {
            this.rotation.y = (float) (2 * Math.PI);
        }
        this.recalculate();
    }

    public void moveForward(float distance) {
        double x = Math.sin(this.rotation.y) * -1;
        double z = Math.cos(this.rotation.y);

        Vector3f direction = new Vector3f((float) x, 0, (float) z).mul(distance).negate();
        this.position.add(direction);
        this.recalculate();
    }

    public void moveBackwards(float distance) {
        double x = Math.sin(this.rotation.y) * -1;
        double z = Math.cos(this.rotation.y);

        Vector3f direction = new Vector3f((float) x, 0, (float) z).mul(distance).negate();
        this.position.sub(direction);
        this.recalculate();
    }

    public void moveUp(float distance) {
        this.position.y += distance;
        this.recalculate();
    }

    public void moveDown(float distance) {
        this.position.y -= distance;
        this.recalculate();
    }

    public void moveLeft(float distance) {
        this.viewMatrix.positiveX(this.right).mul(distance);
        this.position.sub(this.right.x, 0, this.right.z);
        this.recalculate();
    }

    public void moveRight(float distance) {
        this.viewMatrix.positiveX(this.right).mul(distance);
        this.position.add(this.right.x, 0, this.right.z);
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
