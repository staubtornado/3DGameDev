package org.game3d.dev.engine.scene.lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class AmbientLight {
    private Vector3f color;
    private float intensity;

    public AmbientLight(Vector3f color, float intensity) {
        this.color = color;
        this.intensity = intensity;
    }

    public AmbientLight() {
        this(new Vector3f(0, 0, 0), 0);
    }
}
