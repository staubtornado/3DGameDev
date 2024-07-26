package org.game3d.dev.game;

import org.game3d.dev.engine.Engine;
import org.game3d.dev.engine.IAppLogic;
import org.game3d.dev.engine.MouseInput;
import org.game3d.dev.engine.Window;
import org.game3d.dev.engine.graph.*;
import org.game3d.dev.engine.scene.*;
import org.game3d.dev.engine.scene.lights.SceneLights;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector2f;
import org.joml.Vector2i;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {
    private static final int CHUNK_SIZE = 16;
    private static final int RENDER_DISTANCE = 4;

    private Chunk[][] chunks;

    private static final float MOUSE_SENSITIVITY = 0.05f;
    private static final float MOVEMENT_SPEED = 0.05f;



    public static void main(String[] args) {
        Main main = new Main();
        Engine engine = new Engine("Game 3D Dev", new Window.WindowOptions(
                false,
                200,
                800,
                1600,
                true
        ), main);
        engine.start();
    }

    @Override
    public void cleanup() {
        // TODO: add logic
    }

    @Override
    public void init(Window window, @NotNull Scene scene, Render render) {
        String cubeModelId = "cube-model";
        Model cubeModel = ModelLoader.loadModel(
                cubeModelId,
                "resources/models/cube/cube.obj",
                scene.getTextureCache()
        );
        scene.addModel(cubeModel);

        this.chunks = new Chunk[RENDER_DISTANCE * 2 + 1][RENDER_DISTANCE * 2 + 1];
        for (int i = 0; i < RENDER_DISTANCE * 2 + 1; i++) {
            for (int j = 0; j < RENDER_DISTANCE * 2 + 1; j++) {
                int positionX = (i - RENDER_DISTANCE) * CHUNK_SIZE;
                int positionZ = (j - RENDER_DISTANCE) * CHUNK_SIZE;
                this.chunks[j][i] = new Chunk(CHUNK_SIZE, 1, new Vector2i(positionX, positionZ), i * CHUNK_SIZE + j);
                this.chunks[j][i].init();

                for (Entity entity : this.chunks[j][i].getBlocks()) {
                    scene.addEntity(entity);
                }
            }
        }

        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbientLight().setIntensity(0.3f);
        scene.setSceneLights(sceneLights);
        scene.setFog(new Fog(true, CONST.SKY_COLOR, 0.01f));

//        SkyBox skyBox = new SkyBox("resources/models/skybox/skybox.obj", scene.getTextureCache());
//        skyBox.getSkyBoxEntity().setScale(500);
//        scene.setSkyBox(skyBox);
        scene.getCamera().moveUp(1);
        this.updateTerrain(scene);
    }

    @Override
    public void input(@NotNull Window window, @NotNull Scene scene, long diffTimeMillis, boolean inputConsumed) {
        if (inputConsumed) {
            return;
        }

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
        this.updateTerrain(scene);
    }

    public void updateTerrain(@NotNull Scene scene) {
    }
}
