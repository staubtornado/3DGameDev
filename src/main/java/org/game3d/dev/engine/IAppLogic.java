package org.game3d.dev.engine;

import org.game3d.dev.engine.graph.Render;
import org.game3d.dev.engine.scene.Scene;

public interface IAppLogic {
    void cleanup();
    void init(Window window, Scene scene, Render render);
    void input(Window window, Scene scene, long diffTimeMillis, boolean inputConsumed);
    void update(Window window, Scene scene, long diffTimeMillis);
}
