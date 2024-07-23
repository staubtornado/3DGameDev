package org.game3d.dev.engine;

import org.game3d.dev.engine.scene.Scene;

public interface IGuiInstance {
    void drawGui();
    boolean handleGuiInput(Scene scene, Window window);
}
