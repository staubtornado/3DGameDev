package org.game3d.dev.engine.scene.lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@Getter
public class SpotLight {
    @Setter private Vector3f coneDirection;
    private float cutoff;
    private float cutoffAngle;
    @Setter private PointLight pointLight;

    public SpotLight(PointLight pointLight, Vector3f coneDirection, float cutoffAngle) {
        this.pointLight = pointLight;
        this.coneDirection = coneDirection;
        this.setCutoffAngle(cutoffAngle);

    }

    public final void setCutoffAngle(float cutoffAngle) {
        this.cutoffAngle = cutoffAngle;
        this.cutoff = (float) Math.cos(Math.toRadians(cutoffAngle));
    }
}
