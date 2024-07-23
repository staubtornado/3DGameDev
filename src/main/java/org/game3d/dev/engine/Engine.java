package org.game3d.dev.engine;

import org.game3d.dev.engine.graph.Render;
import org.game3d.dev.engine.scene.Scene;

public class Engine {
    public static final int TARGET_UPS = 30;
    private final IAppLogic appLogic;
    private final Window window;
    private final Render render;
    private boolean running;
    private final Scene scene;
    private final int targetFPS;
    private final int targetUps;

    public Engine(String title, Window.WindowOptions options, IAppLogic appLogic) {
        this.window = new Window(title, options, () -> {
            this.resize();
            return null;
        });
        this.targetFPS = options.fps;
        this.targetUps = options.ups;
        this.appLogic = appLogic;
        this.render = new Render(this.window);
        this.scene = new Scene(this.window.getWidth(), this.window.getHeight());
        this.appLogic.init(this.window, this.scene, this.render);
        this.running = true;
    }

    private void cleanup() {
        this.appLogic.cleanup();
        this.render.cleanup();
        this.scene.cleanup();
        this.window.cleanup();
    }

    private void resize() {
        this.scene.resize(this.window.getWidth(), this.window.getHeight());
        this.render.resize(this.window.getWidth(), this.window.getHeight());
    }

    private void run() {
        long initialTime = System.currentTimeMillis();
        float timeU = 1000.0f / this.targetUps;
        float timeR = this.targetFPS > 0 ? 1000.0f / this.targetFPS : 0;
        float deltaUpdate = 0;
        float deltaFPS = 0;

        long updateTime = initialTime;
        IGuiInstance iGuiInstance = scene.getGuiInstance();
        while (this.running && !this.window.windowShouldClose()) {
            this.window.pollEvents();

            long now = System.currentTimeMillis();
            deltaUpdate +=(now - initialTime) / timeU;
            deltaFPS += (now - initialTime) / timeR;

            if (this.targetFPS <= 0 || deltaFPS >= 1) {
                this.window.getMouseInput().input();
                boolean inputConsumed = iGuiInstance != null && iGuiInstance.handleGuiInput(this.scene, this.window);
                this.appLogic.input(this.window, scene, now - initialTime, inputConsumed);
            }

            if (deltaUpdate >= 1) {
                long diffTimeMillis = now - updateTime;
                this.appLogic.update(this.window, this.scene, diffTimeMillis);
                updateTime = now;
                deltaUpdate--;
            }

            if (this.targetFPS <= 0 || deltaFPS >= 1) {
                this.render.render(this.window, this.scene);
                deltaFPS--;
                this.window.update();
            }
            initialTime = now;
        }
        this.cleanup();
    }

    public void start() {
        this.running = true;
        this.run();
    }

    public void stop() {
        this.running = false;
    }
}
