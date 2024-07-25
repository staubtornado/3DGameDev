package org.game3d.dev.engine.graph;

import org.game3d.dev.engine.scene.Entity;
import org.game3d.dev.engine.scene.Scene;
import org.game3d.dev.engine.scene.lights.*;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.lwjgl.opengl.GL30.*;

public class SceneRender {

    private static final int MAX_POINT_LIGHTS = 5;
    private static final int MAX_SPOT_LIGHTS = 5;

    private final ShaderProgram shaderProgram;

    private UniformsMap uniformsMap;

    public SceneRender() {
        List<ShaderProgram.ShaderModuleData> shaderModuleDataList = new ArrayList<>();
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.vert", GL_VERTEX_SHADER));
        shaderModuleDataList.add(new ShaderProgram.ShaderModuleData("resources/shaders/scene.frag", GL_FRAGMENT_SHADER));
        this.shaderProgram = new ShaderProgram(shaderModuleDataList);
        createUniforms();
    }

    public void cleanup() {
        this.shaderProgram.cleanup();
    }

    private void createUniforms() {
        this.uniformsMap = new UniformsMap(this.shaderProgram.getProgramId());
        this.uniformsMap.createUniform("projectionMatrix");
        this.uniformsMap.createUniform("modelMatrix");
        this.uniformsMap.createUniform("viewMatrix");
        this.uniformsMap.createUniform("txtSampler");
        this.uniformsMap.createUniform("material.ambient");
        this.uniformsMap.createUniform("material.diffuse");
        this.uniformsMap.createUniform("material.specular");
        this.uniformsMap.createUniform("material.reflectance");
        this.uniformsMap.createUniform("ambientLight.factor");
        this.uniformsMap.createUniform("ambientLight.color");

        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            String name = "pointLights[" + i + "]";
            this.uniformsMap.createUniform(name + ".position");
            this.uniformsMap.createUniform(name + ".color");
            this.uniformsMap.createUniform(name + ".intensity");
            this.uniformsMap.createUniform(name + ".att.constant");
            this.uniformsMap.createUniform(name + ".att.linear");
            this.uniformsMap.createUniform(name + ".att.exponent");
        }
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            String name = "spotLights[" + i + "]";
            this.uniformsMap.createUniform(name + ".pl.position");
            this.uniformsMap.createUniform(name + ".pl.color");
            this.uniformsMap.createUniform(name + ".pl.intensity");
            this.uniformsMap.createUniform(name + ".pl.att.constant");
            this.uniformsMap.createUniform(name + ".pl.att.linear");
            this.uniformsMap.createUniform(name + ".pl.att.exponent");
            this.uniformsMap.createUniform(name + ".conedir");
            this.uniformsMap.createUniform(name + ".cutoff");
        }

        this.uniformsMap.createUniform("dirLight.color");
        this.uniformsMap.createUniform("dirLight.direction");
        this.uniformsMap.createUniform("dirLight.intensity");

        this.uniformsMap.createUniform("fog.activeFog");
        this.uniformsMap.createUniform("fog.color");
        this.uniformsMap.createUniform("fog.density");
    }

    public void render(@NotNull Scene scene) {
        glEnable(GL_BLEND);
        glBlendEquation(GL_FUNC_ADD);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        this.shaderProgram.bind();

        this.uniformsMap.setUniform("projectionMatrix", scene.getProjection().getProjectionMatrix());
        this.uniformsMap.setUniform("viewMatrix", scene.getCamera().getViewMatrix());
        this.uniformsMap.setUniform("txtSampler", 0);

        this.uniformsMap.setUniform("fog.activeFog", scene.getFog().isActive() ? 1 : 0);
        this.uniformsMap.setUniform("fog.color", scene.getFog().getColor());
        this.uniformsMap.setUniform("fog.density", scene.getFog().getDensity());

        updateLights(scene);

        Collection<Model> models = scene.getModelMap().values();
        TextureCache textureCache = scene.getTextureCache();
        for (Model model : models) {
            List<Entity> entities = model.getEntities();

            for (Material material : model.getMaterials()) {
                this.uniformsMap.setUniform("material.ambient", material.getAmbientColor());
                this.uniformsMap.setUniform("material.diffuse", material.getDiffuseColor());
                this.uniformsMap.setUniform("material.specular", material.getSpecularColor());
                this.uniformsMap.setUniform("material.reflectance", material.getReflectance());
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
        glDisable(GL_BLEND);
    }

    private void updateLights(@NotNull Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();

        SceneLights sceneLights = scene.getSceneLights();
        AmbientLight ambientLight = sceneLights.getAmbientLight();
        this.uniformsMap.setUniform("ambientLight.factor", ambientLight.getIntensity());
        this.uniformsMap.setUniform("ambientLight.color", ambientLight.getColor());

        DirLight dirLight = sceneLights.getDirLight();
        Vector4f auxDir = new Vector4f(dirLight.getDirection(), 0);
        auxDir.mul(viewMatrix);
        Vector3f dir = new Vector3f(auxDir.x, auxDir.y, auxDir.z);
        this.uniformsMap.setUniform("dirLight.color", dirLight.getColor());
        this.uniformsMap.setUniform("dirLight.direction", dir);
        this.uniformsMap.setUniform("dirLight.intensity", dirLight.getIntensity());

        List<PointLight> pointLights = sceneLights.getPointLights();
        int numPointLights = pointLights.size();
        PointLight pointLight;
        for (int i = 0; i < MAX_POINT_LIGHTS; i++) {
            if (i < numPointLights) {
                pointLight = pointLights.get(i);
            } else {
                pointLight = null;
            }
            String name = "pointLights[" + i + "]";
            updatePointLight(pointLight, name, viewMatrix);
        }


        List<SpotLight> spotLights = sceneLights.getSpotLights();
        int numSpotLights = spotLights.size();
        SpotLight spotLight;
        for (int i = 0; i < MAX_SPOT_LIGHTS; i++) {
            if (i < numSpotLights) {
                spotLight = spotLights.get(i);
            } else {
                spotLight = null;
            }
            String name = "spotLights[" + i + "]";
            updateSpotLight(spotLight, name, viewMatrix);
        }
    }

    private void updatePointLight(PointLight pointLight, String prefix, Matrix4f viewMatrix) {
        Vector4f aux = new Vector4f();
        Vector3f lightPosition = new Vector3f();
        Vector3f color = new Vector3f();
        float intensity = 0.0f;
        float constant = 0.0f;
        float linear = 0.0f;
        float exponent = 0.0f;
        if (pointLight != null) {
            aux.set(pointLight.getPosition(), 1);
            aux.mul(viewMatrix);
            lightPosition.set(aux.x, aux.y, aux.z);
            color.set(pointLight.getColor());
            intensity = pointLight.getIntensity();
            PointLight.Attenuation attenuation = pointLight.getAttenuation();
            constant = attenuation.getConstant();
            linear = attenuation.getLinear();
            exponent = attenuation.getExponent();
        }
        this.uniformsMap.setUniform(prefix + ".position", lightPosition);
        this.uniformsMap.setUniform(prefix + ".color", color);
        this.uniformsMap.setUniform(prefix + ".intensity", intensity);
        this.uniformsMap.setUniform(prefix + ".att.constant", constant);
        this.uniformsMap.setUniform(prefix + ".att.linear", linear);
        this.uniformsMap.setUniform(prefix + ".att.exponent", exponent);
    }

    private void updateSpotLight(SpotLight spotLight, String prefix, Matrix4f viewMatrix) {
        PointLight pointLight = null;
        Vector3f coneDirection = new Vector3f();
        float cutoff = 0.0f;
        if (spotLight != null) {
            coneDirection = spotLight.getConeDirection();
            cutoff = spotLight.getCutoff();
            pointLight = spotLight.getPointLight();
        }

        this.uniformsMap.setUniform(prefix + ".conedir", coneDirection);
        this.uniformsMap.setUniform(prefix + ".cutoff", cutoff);
        updatePointLight(pointLight, prefix + ".pl", viewMatrix);
    }
}