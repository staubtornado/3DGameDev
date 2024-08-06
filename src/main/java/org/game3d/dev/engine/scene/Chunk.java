package org.game3d.dev.engine.scene;

import lombok.Getter;
import lombok.Setter;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Chunk {
    @Setter private Vector2i position;
    private final Entity[][][] entities;
    private final int id;

    public Chunk(int xzSize, int ySize, Vector2i position, int id) {
        this.entities = new Entity[xzSize][ySize][xzSize];
        this.position = position;
        this.id = id;
    }

    public void init() {
        for (int x = 0; x < this.entities.length; x++) {
            for (int y = 0; y < this.entities[0].length; y++) {
                for (int z = 0; z < this.entities[0][0].length; z++) {
                    Entity entity = new Entity(String.format("cube-%d-%d-%d-%d", this.id, x, y, z), "cube-model");
                    entity.setPosition((x + this.position.x), y, (z + this.position.y));
                    entity.getModelMatrix().identity().translate(entity.getPosition());
                    this.entities[x][y][z] = entity;
                }
            }
        }
    }

    public List<Entity> getBlocks() {
        List<Entity> blocks = new ArrayList<>();
        for (Entity[][] entities : this.entities) {
            for (Entity[] entity : entities) {
                blocks.addAll(Arrays.asList(entity));
            }
        }
        return blocks;
    }
}
