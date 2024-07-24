package org.game3d.dev.engine.scene;

import lombok.Getter;
import org.game3d.dev.engine.graph.Model;
import org.game3d.dev.engine.graph.TextureCache;

@Getter
public class SkyBox {
    private final Entity skyBoxEntity;
    private final Model skyBoxModel;

    public SkyBox(String skyBoxModelPath, TextureCache textureCache) {
        this.skyBoxModel = ModelLoader.loadModel("skybox-model", skyBoxModelPath, textureCache);
        this.skyBoxEntity = new Entity("skybox-entity", this.skyBoxModel.getId());
    }
}
