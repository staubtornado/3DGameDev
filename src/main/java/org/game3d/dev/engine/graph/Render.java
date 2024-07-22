package org.game3d.dev.engine.graph;

import org.game3d.dev.engine.Window;
import org.game3d.dev.engine.scene.Scene;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.glfw.GLFW.glfwSetCursorPos;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;

public class Render {
    private final SceneRender sceneRender;
    private final Window window;

    public Render(Window window) {
        createCapabilities();
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_CULL_FACE);
        glCullFace(GL_BACK);
        this.sceneRender = new SceneRender();
        this.window = window;
    }

    public void cleanup() {
        this.sceneRender.cleanup();
    }

    public void render(@NotNull Window window, @NotNull Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        this.sceneRender.render(scene);
    }
}
