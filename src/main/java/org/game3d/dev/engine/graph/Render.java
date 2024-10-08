package org.game3d.dev.engine.graph;

import org.game3d.dev.engine.Window;
import org.game3d.dev.engine.scene.Scene;
import org.game3d.dev.game.CONST;
import org.jetbrains.annotations.NotNull;

import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_MULTISAMPLE;

public class Render {
    private final SceneRender sceneRender;
    private final GuiRender guiRender;
    private final SkyBoxRender skyBoxRender;

    public Render(Window window) {
        createCapabilities();
        // Set the clear color to light blue
        glClearColor(CONST.SKY_COLOR.x, CONST.SKY_COLOR.y, CONST.SKY_COLOR.z, 1.0f);
        glEnable(GL_MULTISAMPLE);
        glEnable(GL_DEPTH_TEST);
//        glEnable(GL_CULL_FACE);
//        glCullFace(GL_BACK);
//        glFrontFace(GL_CCW);
        this.sceneRender = new SceneRender();
        this.guiRender = new GuiRender(window);
        this.skyBoxRender = new SkyBoxRender();
    }

    public void cleanup() {
        this.sceneRender.cleanup();
        this.guiRender.cleanup();
        this.skyBoxRender.cleanup();
    }

    public void render(@NotNull Window window, @NotNull Scene scene) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, window.getWidth(), window.getHeight());

        this.skyBoxRender.render(scene);
        this.sceneRender.render(scene);
        this.guiRender.render(scene);
    }

    public void resize(int width, int height) {
        this.guiRender.resize(width, height);
    }
}
