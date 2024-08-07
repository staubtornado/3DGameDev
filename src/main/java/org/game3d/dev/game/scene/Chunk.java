package org.game3d.dev.game.scene;

import lombok.Getter;
import lombok.Setter;
import org.game3d.dev.engine.scene.Entity;
import org.joml.Vector2i;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
public class Chunk {
    @Setter private Vector2i position;
    private final Block[][][] entities;
    private final int id;

    public Chunk(int xzSize, int ySize, Vector2i position, int id) {
        this.entities = new Block[xzSize][ySize][xzSize];
        this.position = position;
        this.id = id;
    }

    public void init() {
        for (int x = 0; x < this.entities.length; x++) {
            for (int y = 0; y < this.entities[0].length; y++) {
                for (int z = 0; z < this.entities[0][0].length; z++) {
                    Block entity = new Block(String.format("cube-%d-%d-%d-%d", this.id, x, y, z), Block.Type.DIRT);
                    entity.setPosition((x + this.position.x), y, (z + this.position.y));
                    entity.getModelMatrix().identity().translate(entity.getPosition());
                    this.entities[x][y][z] = entity;
                }
            }
        }
    }

    public List<Block> getBlocks() {
        List<Block> blocks = new ArrayList<>();
        for (Block[][] entities : this.entities) {
            for (Block[] entity : entities) {
                blocks.addAll(Arrays.asList(entity));
            }
        }
        return blocks;
    }
}
