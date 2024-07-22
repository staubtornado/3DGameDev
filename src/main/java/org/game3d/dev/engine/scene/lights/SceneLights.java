package org.game3d.dev.engine.scene.lights;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

@Getter
public class SceneLights {
    private final AmbientLight ambientLight;
    private final DirLight dirLight;
    private final List<PointLight> pointLights;
    @Setter private List<SpotLight> spotLights;

    public SceneLights() {
        this.ambientLight = new AmbientLight();
        this.dirLight = new DirLight(new Vector3f(1, 1, 1), new Vector3f(0, 1, 0), 1.0f);
        this.pointLights = new ArrayList<>();
        this.spotLights = new ArrayList<>();
    }
}
