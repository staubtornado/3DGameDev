package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.game3d.dev.engine.scene.Entity;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Model {
    private final String id;
    private final List<Entity> entities;
    private final List<Material> materials;
    private final List<Animation> animations;

    public Model(String id, List<Material> materials, List<Animation> animations) {
        this.id = id;
        this.materials = materials;
        this.entities = new ArrayList<>();
        this.animations = animations;
    }

    public void cleanup() {
        this.materials.forEach(Material::cleanup);
    }

    public record AnimatedFrame(Matrix4f[] boneMatrices) {}
    public record Animation(String name, double duration, List<AnimatedFrame> frames) {}
}
