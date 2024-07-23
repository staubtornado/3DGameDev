package org.game3d.dev.engine.scene;

import lombok.Getter;
import lombok.Setter;
import org.game3d.dev.engine.IGuiInstance;
import org.game3d.dev.engine.graph.Model;
import org.game3d.dev.engine.graph.TextureCache;
import org.game3d.dev.engine.scene.lights.SceneLights;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
public class Scene {
    private final Map<String, Model> modelMap;
    private final Projection projection;
    private final TextureCache textureCache;
    private final Camera camera;
    @Setter private SceneLights sceneLights;
    @Setter private IGuiInstance guiInstance;

    public Scene(int width, int height) {
        this.modelMap = new HashMap<>();
        this.projection = new Projection(width, height);
        this.textureCache = new TextureCache();
        this.camera = new Camera();
    }

    public void addEntity(@NotNull Entity entity) {
        String modelId = entity.getModelId();
        Model model = this.modelMap.get(modelId);
        if (model == null) {
            throw new RuntimeException(String.format("Model %s not found", modelId));
        }
        model.getEntities().add(entity);
    }

    public void addModel(@NotNull Model model) {
        this.modelMap.put(model.getId(), model);
    }

    public void cleanup() {
        this.modelMap.values().forEach(Model::cleanup);
    }

    public void resize(int width, int height) {
        this.projection.updateProjMatrix(width, height);
    }
}
