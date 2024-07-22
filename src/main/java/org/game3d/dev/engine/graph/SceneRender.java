package org.game3d.dev.engine.graph;

import org.game3d.dev.engine.scene.Entity;
import org.game3d.dev.engine.scene.Scene;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {
    private final ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(
                new ShaderProgram.ShaderModuleData("resources/shaders/scene.vert", GL_VERTEX_SHADER)
        );
        shaderModuleDataList.add(
                new ShaderProgram.ShaderModuleData("resources/shaders/scene.frag", GL_FRAGMENT_SHADER)
        );
        this.shaderProgram = new ShaderProgram(shaderModuleDataList);
        this.createUniforms();
    }

    public void cleanup() {
        this.shaderProgram.cleanup();
    }

    private void createUniforms() {
        this.uniformsMap = new UniformsMap(this.shaderProgram.getProgramId());
        this.uniformsMap.createUniform("projectionMatrix");
        this.uniformsMap.createUniform("modelMatrix");
        this.uniformsMap.createUniform("viewMatrix");
        this.uniformsMap.createUniform("textureSampler");
        this.uniformsMap.createUniform("material.diffuse");
    }

    public void render(@NotNull Scene scene) {
        this.shaderProgram.bind();

        this.uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjectionMatrix());
        this.uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        this.uniformsMap.setUniform("textureSampler", 0);

        Collection<Model> modelCollection = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : modelCollection) {
            List<Entity> entities = model.getEntities();

            for (Material material : model.getMaterials()) {
                this.uniformsMap.setUniform("material.diffuse", material.getDiffuseColor());

                Texture texture = textureCache.getTexture(material.getTexturePath());
                glActiveTexture(GL_TEXTURE0);
                texture.bind();

                for (Mesh mesh : material.getMeshes()) {
                    glBindVertexArray(mesh.getVaoId());
                    for (Entity entity : entities) {
                        this.uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                        glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                    }
                }
            }
        }
        glBindVertexArray(0);
        this.shaderProgram.unbind();
    }
}
