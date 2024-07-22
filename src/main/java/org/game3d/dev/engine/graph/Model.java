package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.game3d.dev.engine.scene.Entity;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Model {
    private final String id;
    private final List<Entity> entities;
    private final List<Material> materials;

    public Model(String id, List<Material> materials) {
        this.id = id;
        this.materials = materials;
        this.entities = new ArrayList<>();
    }

    public void cleanup() {
        this.materials.forEach(Material::cleanup);
    }
}
