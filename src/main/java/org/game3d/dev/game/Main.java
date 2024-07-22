package org.game3d.dev.game;

import org.game3d.dev.engine.Engine;
import org.game3d.dev.engine.IAppLogic;
import org.game3d.dev.engine.MouseInput;
import org.game3d.dev.engine.Window;
import org.game3d.dev.engine.graph.*;
import org.game3d.dev.engine.scene.Camera;
import org.game3d.dev.engine.scene.Entity;
import org.game3d.dev.engine.scene.ModelLoader;
import org.game3d.dev.engine.scene.Scene;
import org.game3d.dev.engine.scene.lights.PointLight;
import org.game3d.dev.engine.scene.lights.SceneLights;
import org.game3d.dev.engine.scene.lights.SpotLight;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {
    private Entity cube;

    private static final float MOUSE_SENSITIVITY = 0.05f;
    private static final float MOVEMENT_SPEED = 0.05f;

    public static void main(String[] args) {
        Main main = new Main();
        Engine engine = new Engine("Game 3D Dev", new Window.WindowOptions(
                false,
                200,
                600,
                600
        ), main);
        engine.start();
    }

    @Override
    public void cleanup() {
        // TODO: add logic
    }

    @Override
    public void init(Window window, @NotNull Scene scene, Render render) {
        Model cubeModel = ModelLoader.loadModel(
                "cube-model",
                "resources/models/cube/cube.obj",
                scene.getTextureCache()
        );
        scene.addModel(cubeModel);

        this.cube = new Entity("cube-entity", cubeModel.getId());
        this.cube.setPosition(0, 0, -2);
        this.cube.updateModelMatrix();
        scene.addEntity(this.cube);

        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbientLight().setIntensity(0.3f);
        scene.setSceneLights(sceneLights);
        sceneLights.getPointLights().add(new PointLight(new Vector3f(1, 1, 1),
                new Vector3f(0, 0, -1.4f), 1.0f));

        Vector3f coneDir = new Vector3f(0, 0, -1);
        sceneLights.getSpotLights().add(new SpotLight(new PointLight(new Vector3f(1, 1, 1),
                new Vector3f(0, 0, -1.4f), 0.0f), coneDir, 140.0f));

    }

    @Override
    public void input(@NotNull Window window, @NotNull Scene scene, long diffTimeMillis) {
        float move = diffTimeMillis * MOVEMENT_SPEED;
        Camera camera = scene.getCamera();
        if (window.isKeyPressed(GLFW_KEY_W)) {
            camera.moveForward(move);
        } else if (window.isKeyPressed(GLFW_KEY_S)) {
            camera.moveBackwards(move);
        }
        if (window.isKeyPressed(GLFW_KEY_A)) {
            camera.moveLeft(move);
        } else if (window.isKeyPressed(GLFW_KEY_D)) {
            camera.moveRight(move);
        }
        if (window.isKeyPressed(GLFW_KEY_SPACE)) {
            camera.moveUp(move);
        } else if (window.isKeyPressed(GLFW_KEY_LEFT_CONTROL)) {
            camera.moveDown(move);
        }

        MouseInput mouseInput = window.getMouseInput();
        Vector2f displayVec = mouseInput.getDisplayVec();
        camera.addRotation((float) Math.toRadians(-displayVec.x * MOUSE_SENSITIVITY),
                (float) Math.toRadians(-displayVec.y * MOUSE_SENSITIVITY));
    }

    @Override
    public void update(Window window, Scene scene, long diffTimeMillis) {
//        this.cube.updateModelMatrix();

    }
}
