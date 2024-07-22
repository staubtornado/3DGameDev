package org.game3d.dev.engine.scene.lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class DirLight {
    private Vector3f color;
    private Vector3f direction;
    private float intensity;

    public DirLight(Vector3f color, Vector3f direction, float intensity) {
        this.color = color;
        this.direction = direction;
        this.intensity = intensity;
    }
}