package org.game3d.dev.engine.graph;

import org.game3d.dev.engine.scene.Entity;
import org.game3d.dev.engine.scene.Scene;
import org.game3d.dev.engine.scene.SkyBox;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL20.*;
import static org.lwjgl.opengl.GL30.*;

public class SkyBoxRender {
    private final ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;
    private final Matrix4f viewMatrix;

    public SkyBoxRender()  {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/skybox.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/skybox.frag", GL_FRAGMENT_SHADER));
        this.shaderProgram = new ShaderProgram(shaderModuleDataList);
        this.viewMatrix = new Matrix4f();
        this.createUniforms();
    }

    public void cleanup() {
        this.shaderProgram.cleanup();
    }

    private void createUniforms() {
        this.uniformsMap = new UniformsMap(this.shaderProgram.getProgramId());
        this.uniformsMap.createUniform("projectionMatrix");
        this.uniformsMap.createUniform("viewMatrix");
        this.uniformsMap.createUniform("modelMatrix");
        this.uniformsMap.createUniform("diffuse");
        this.uniformsMap.createUniform("txtSampler");
        this.uniformsMap.createUniform("hasTexture");
    }

    public void render(@NotNull Scene scene) {
        SkyBox skyBox = scene.getSkyBox();
        if (skyBox == null) {
            return;
        }
        this.shaderProgram.bind();
        glDisable(GL_DEPTH_TEST);

        this.uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjectionMatrix());
        this.viewMatrix.set(scene.getCamera().getViewMatrix());
        this.viewMatrix.m30(0);
        this.viewMatrix.m31(0);
        this.viewMatrix.m32(0);
        this.uniformsMap.setUniform("viewMatrix", this.viewMatrix);
        this.uniformsMap.setUniform("txtSampler", 0);

        Model skyboxModel = skyBox.getSkyBoxModel();
        Entity skyBoxEntity = skyBox.getSkyBoxEntity();
        TextureCache textureCache = scene.getTextureCache();
        for (Material material : skyboxModel.getMaterials()) {
            Texture texture = textureCache.getTexture(material.getTexturePath());
            glActiveTexture(GL_TEXTURE0);
            texture.bind();

            this.uniformsMap.setUniform("diffuse", material.getDiffuseColor());
            this.uniformsMap.setUniform(
                    "hasTexture",
                    texture.getTexturePath().equals(TextureCache.DEFAULT_TEXTURE) ? 0 : 1
            );

            for (Mesh mesh : material.getMeshes()) {
                glBindVertexArray(mesh.getVaoId());
                this.uniformsMap.setUniform("modelMatrix", skyBoxEntity.getModelMatrix());
                glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
            }
        }
        glBindVertexArray(0);
        glEnable(GL_DEPTH_TEST);
        this.shaderProgram.unbind();
    }
}
