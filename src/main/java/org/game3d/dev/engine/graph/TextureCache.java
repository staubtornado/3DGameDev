package org.game3d.dev.engine.graph;

import java.util.HashMap;
import java.util.Map;

public class TextureCache {
    public static final String DEFAULT_TEXTURE = "resources/textures/default_texture.png";
    private final Map<String, Texture> textureMap;

    public TextureCache() {
        this.textureMap = new HashMap<>();
        this.textureMap.put(DEFAULT_TEXTURE, new Texture(DEFAULT_TEXTURE));
    }

    public void cleanup() {
        this.textureMap.values().forEach(Texture::cleanup);
    }

    public Texture createTexture(String texturePath) {
        return this.textureMap.computeIfAbsent(texturePath, Texture::new);
    }

    public Texture getTexture(String texturePath) {
        Texture texture = null;
        if (texturePath != null) {
            texture = this.textureMap.get(texturePath);
        }
        if (texture == null) {
            texture = this.textureMap.get(DEFAULT_TEXTURE);
        }
        return texture;
    }
}
