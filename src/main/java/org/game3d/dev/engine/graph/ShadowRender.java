package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.game3d.dev.engine.scene.Entity;
import org.game3d.dev.engine.scene.Scene;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL30.*;

public class ShadowRender {
    @Getter ArrayList<CascadeShadow> cascadeShadows;
    @Getter ShadowBuffer shadowBuffer;
    private final ShaderProgram shaderProgram;
    private UniformsMap uniformsMap;

    public ShadowRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData(
                "resources/shaders/shadow.vert", GL_VERTEX_SHADER
        ));
        this.shaderProgram = new ShaderProgram(shaderModuleDataList);
        this.shadowBuffer = new ShadowBuffer();
        this.cascadeShadows = new ArrayList<>();

        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            CascadeShadow cascadeShadow = new CascadeShadow();
            this.cascadeShadows.add(cascadeShadow);
        }
        this.createUniforms();
    }

    public void cleanup() {
        this.shaderProgram.cleanup();
        this.shadowBuffer.cleanup();
    }

    private void createUniforms() {
        this.uniformsMap = new UniformsMap(this.shaderProgram.getProgramId());
        this.uniformsMap.createUniform("modelMatrix");
        this.uniformsMap.createUniform("projViewMatrix");
    }

    public void render(Scene scene) {
        CascadeShadow.updateCascadeShadows(this.cascadeShadows, scene);
        glBindFramebuffer(GL_FRAMEBUFFER, this.shadowBuffer.getDepthMapFBO());
        glViewport(0, 0, ShadowBuffer.SHADOW_MAP_WIDTH, ShadowBuffer.SHADOW_MAP_HEIGHT);

        this.shaderProgram.bind();

        Collection<Model> modelCollection = scene.getModelMap().values();
        for (int i = 0; i < CascadeShadow.SHADOW_MAP_CASCADE_COUNT; i++) {
            glFramebufferTexture2D(
                    GL_FRAMEBUFFER, GL_DEPTH_ATTACHMENT, GL_TEXTURE_2D,
                    this.shadowBuffer.getDepthMap().getIds()[i], 0
            );
            glClear(GL_DEPTH_BUFFER_BIT);
            CascadeShadow shadowCascade = this.cascadeShadows.get(i);
            this.uniformsMap.setUniform("projViewMatrix", shadowCascade.getProjectionMatrix());

            for (Model model : modelCollection) {
                List<Entity> entities = model.getEntities();
                for (Material material : model.getMaterials()) {
                    for (Mesh mesh : material.getMeshes()) {
                        glBindVertexArray(mesh.getVaoId());
                        for (Entity entity : entities) {
                            this.uniformsMap.setUniform("modelMatrix", entity.getModelMatrix());
                            glDrawElements(GL_TRIANGLES, mesh.getNumVertices(), GL_UNSIGNED_INT, 0);
                        }
                    }
                }
            }
        }

        this.shaderProgram.unbind();
        glBindFramebuffer(GL_FRAMEBUFFER, 0);
    }
}
