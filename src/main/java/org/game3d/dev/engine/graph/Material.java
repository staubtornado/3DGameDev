package org.game3d.dev.engine.graph;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class Material {
    public static final Vector4f DEFAULT_COLOR = new Vector4f(0.0f, 0.0f, 0.0f, 1.0f);

    @Setter private Vector4f ambientColor;
    @Setter private Vector4f diffuseColor;
    private final List<Mesh> meshes;
    @Setter private float reflectance;
    @Setter private Vector4f specularColor;
    @Setter private String texturePath;
    @Setter private String normalMapPath;

    public Material() {
        this.diffuseColor = DEFAULT_COLOR;
        this.ambientColor = DEFAULT_COLOR;
        this.specularColor = DEFAULT_COLOR;
        this.meshes = new ArrayList<>();
    }

    public void cleanup() {
        this.meshes.forEach(Mesh::cleanup);
    }
}
