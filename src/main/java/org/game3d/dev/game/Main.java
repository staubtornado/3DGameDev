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
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Main implements IAppLogic {
    private static final int NUM_CHUNKS = 4;

    private static final float MOUSE_SENSITIVITY = 0.05f;
    private static final float MOVEMENT_SPEED = 0.05f;

    private Entity[][] terrainEntities;

    public static void main(String[] args) {
        Main main = new Main();
        Engine engine = new Engine("Game 3D Dev", new Window.WindowOptions(
                false,
                200,
                800,
                1600
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

        int numRows = NUM_CHUNKS * 2 + 1;
        int numCols = numRows;
        this.terrainEntities = new Entity[numRows][numCols];
        for (int i = 0; i < numRows; i++) {
            for (int j = 0; j < numCols; j++) {
                Entity entity = new Entity(String.format("TERRAIN_%d_%d", i, j), cubeModelId);
                this.terrainEntities[i][j] = entity;
                scene.addEntity(entity);
            }
        }
        SceneLights sceneLights = new SceneLights();
        sceneLights.getAmbientLight().setIntensity(0.2f);
        scene.setSceneLights(sceneLights);

        SkyBox skyBox = new SkyBox("resources/models/skybox/skybox.obj", scene.getTextureCache());
        skyBox.getSkyBoxEntity().setScale(50);
        scene.setSkyBox(skyBox);
        scene.getCamera().moveUp(0.1f);
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
    }

    public void updateTerrain(@NotNull Scene scene) {
        int cellSize = 10;
        Camera camera = scene.getCamera();
        Vector3f cameraPos = camera.getPosition();
        int cellCol = (int) (cameraPos.x / cellSize);
        int cellRow = (int) (cameraPos.z / cellSize);

        int numRows = NUM_CHUNKS * 2 + 1;
        int numCols = numRows;
        int zOffset = -NUM_CHUNKS;
        float scale = cellSize / 2.0f;
        for (int j = 0; j < numRows; j++) {
            int xOffset = -NUM_CHUNKS;
            for (int i = 0; i < numCols; i++) {
                Entity entity = terrainEntities[j][i];
                entity.setScale(scale);
                entity.setPosition((cellCol + xOffset) * 2.0f, 0, (cellRow + zOffset) * 2.0f);
                entity.getModelMatrix().identity().scale(scale).translate(entity.getPosition());
                xOffset++;
            }
            zOffset++;
        }
    }
}
