package org.game3d.dev.engine.scene;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.joml.Vector3f;

@AllArgsConstructor
@Getter
@Setter
public class Fog {
    private boolean active;
    private Vector3f color;
    private float density;

    public Fog() {
        this.active = false;
        this.color = new Vector3f();
        this.density = 0;
    }
}
