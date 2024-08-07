package org.game3d.dev.game.scene;

import lombok.Getter;
import lombok.Setter;
import org.game3d.dev.engine.scene.Entity;

@Setter
@Getter
public class Block extends Entity {
    private Type type;
    private static final String MODEL_ID = "cube-model";

    public Block(String id, Type type) {
        super(id, MODEL_ID);
        this.type = type;
    }

    public enum Type {
        DIRT,
        AIR
    }
}
