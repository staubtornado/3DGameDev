package org.game3d.dev.engine.scene.lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class AmbientLight {
    private Vector3f color;
    private float intensity;

    public AmbientLight(float intensity, Vector3f color) {
        this.color = color;
        this.intensity = intensity;
    }

    public AmbientLight() {
        this(1.0f, new Vector3f(1.0f, 1.0f, 1.0f));
    }
}
