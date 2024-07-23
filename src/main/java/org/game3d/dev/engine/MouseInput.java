package org.game3d.dev.engine;

import lombok.Getter;
import org.joml.Vector2f;

import static org.lwjgl.glfw.GLFW.*;

public class MouseInput {
    @Getter private final Vector2f currentPos;
    private final Vector2f middlePos;
    @Getter final private Vector2f displayVec;
    private boolean inWindow;
    @Getter private boolean leftButtonPressed;
    @Getter private boolean rightButtonPressed;

    private final long windowHandle;

    public MouseInput(long windowHandle, int width, int height) {
        this.currentPos = new Vector2f();
        this.middlePos = new Vector2f(width / 2f, height / 2f);
        this.displayVec = new Vector2f();
        this.inWindow = false;
        this.leftButtonPressed = false;
        this.rightButtonPressed = false;

        glfwSetCursorPosCallback(windowHandle, (_, xPos, yPos) -> {
            this.currentPos.x = (float) xPos;
            this.currentPos.y = (float) yPos;
        });
        glfwSetCursorEnterCallback(windowHandle, (_, entered) -> this.inWindow = entered);
        glfwSetMouseButtonCallback(windowHandle, (_, button, action, _) -> {
            this.leftButtonPressed = button == GLFW_MOUSE_BUTTON_1 && action == GLFW_PRESS;
            this.rightButtonPressed = button == GLFW_MOUSE_BUTTON_2 && action == GLFW_PRESS;
        });
        this.windowHandle = windowHandle;
    }

    public void resize(int width, int height) {
        this.middlePos.x = width / 2f;
        this.middlePos.y = height / 2f;
    }

    public void input() {
        this.displayVec.zero();

        if (this.inWindow) {
            double deltaX = this.currentPos.x - this.middlePos.x;
            double deltaY = this.currentPos.y - this.middlePos.y;

            boolean rotX = deltaX != 0;
            boolean rotY = deltaY != 0;

            if (rotX) {
                this.displayVec.y = (float) deltaX;
            }
            if (rotY) {
                this.displayVec.x = (float) deltaY;
            }

            if (rotX || rotY) {
                glfwSetCursorPos(this.windowHandle, this.middlePos.x, this.middlePos.y);
            }
        }
    }
}
