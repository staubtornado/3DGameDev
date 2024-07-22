package org.game3d.dev.engine.scene.lights;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
@Setter
public class PointLight {
    private Attenuation attenuation;
    private float intensity;
    private Vector3f color;
    private Vector3f position;

    public PointLight(Vector3f color, Vector3f position, float intensity) {
        this.attenuation = new Attenuation(0, 0, 1);
        this.color = color;
        this.position = position;
        this.intensity = intensity;
    }

    @AllArgsConstructor
    @Getter
    @Setter
    public static class Attenuation {
        private float constant;
        private float exponent;
        private float linear;
    }


}
