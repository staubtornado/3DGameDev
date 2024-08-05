package org.game3d.dev.engine.graph;

import lombok.Getter;
import org.game3d.dev.engine.scene.Scene;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;

@Getter
public class CascadeShadow {
    public static final int SHADOW_MAP_CASCADE_COUNT = 3;

    private Matrix4f projectionMatrix;
    private float splitDistance;

    public CascadeShadow() {
        this.projectionMatrix = new Matrix4f();
    }

    public static void updateCascadeShadows(List<CascadeShadow> cascadeShadows, @NotNull Scene scene) {
        Matrix4f viewMatrix = scene.getCamera().getViewMatrix();
        Matrix4f projectionMatrix = scene.getProjection().getProjectionMatrix();
        Vector4f lightPosition = new Vector4f(scene.getSceneLights().getDirLight().getDirection(), 0);

        float cascadeSplitLambda = 0.95f;
        float[] cascadeSplits = new float[SHADOW_MAP_CASCADE_COUNT];

        float nearClip = projectionMatrix.perspectiveNear();
        float farClip = projectionMatrix.perspectiveFar();
        float clipRange = farClip - nearClip;

        float maxZ = nearClip + clipRange;

        float range = maxZ - nearClip;
        float ratio = maxZ / nearClip;

        for (int i = 0; i < SHADOW_MAP_CASCADE_COUNT; i++) {
            float p = (i + 1) / (float) SHADOW_MAP_CASCADE_COUNT;
            float log = (float) (nearClip * Math.pow(ratio, p));
            float uniform = nearClip + range * p;
            float d = cascadeSplitLambda * (log - uniform) + uniform;
            cascadeSplits[i] = (d - nearClip) / clipRange;
        }

        float lastSplitDist = 0.0f;
        for (int i = 0; i < SHADOW_MAP_CASCADE_COUNT; i++) {
            float splitDist = cascadeSplits[i];

            Vector3f[] frustumCorners = new Vector3f[]{
                    new Vector3f(-1.0f, 1.0f, -1.0f),
                    new Vector3f(1.0f, 1.0f, -1.0f),
                    new Vector3f(1.0f, -1.0f, -1.0f),
                    new Vector3f(-1.0f, -1.0f, -1.0f),
                    new Vector3f(-1.0f, 1.0f, 1.0f),
                    new Vector3f(1.0f, 1.0f, 1.0f),
                    new Vector3f(1.0f, -1.0f, 1.0f),
                    new Vector3f(-1.0f, -1.0f, 1.0f),
            };

            Matrix4f invCamera = new Matrix4f(projectionMatrix).mul(viewMatrix).invert();
            for (int j = 0; j < 8; j++) {
                Vector4f invCorner = new Vector4f(frustumCorners[j], 1.0f).mul(invCamera);
                frustumCorners[j] = new Vector3f(
                        invCorner.x / invCorner.w,
                        invCorner.y / invCorner.w,
                        invCorner.z / invCorner.w
                );
            }

            for (int j = 0; j < 4; j++) {
                Vector3f dist = new Vector3f(frustumCorners[j + 4]).sub(frustumCorners[j]);
                frustumCorners[j + 4] = new Vector3f(frustumCorners[j]).add(new Vector3f(dist).mul(splitDist));
                frustumCorners[j] = new Vector3f(frustumCorners[j]).add(new Vector3f(dist).mul(lastSplitDist));
            }

            Vector3f frustumCenter = new Vector3f(0.0f);
            for (int j = 0; j < 8; j++) {
                frustumCenter.add(frustumCorners[j]);
            }
            frustumCenter.div(8.0f);

            float radius = 0.0f;
            for (int j = 0; j < 8; j++) {
                float distance = new Vector3f(frustumCorners[j]).sub(frustumCenter).length();
                radius = Math.max(radius, distance);
            }
            radius = (float) Math.ceil(radius * 16.0f) / 16.0f;

            Vector3f maxExtents = new Vector3f(radius);
            Vector3f minExtents = new Vector3f(maxExtents).mul(-1);

            Vector3f lightDir = new Vector3f(lightPosition.x, lightPosition.y, lightPosition.z).mul(-1).normalize();
            Vector3f eye = new Vector3f(frustumCenter).sub(new Vector3f(lightDir).mul(-minExtents.z));
            Vector3f up = new Vector3f().zero();
            Matrix4f lightViewMatrix = new Matrix4f().lookAt(eye, frustumCenter, up);
            Matrix4f lightOrthogonalMatrix = new Matrix4f().ortho(
                    minExtents.x, maxExtents.x,
                    minExtents.y, maxExtents.y, 0.0f, maxExtents.z - minExtents.z, true
            );

            CascadeShadow cascadeShadow = cascadeShadows.get(i);
            cascadeShadow.splitDistance = (nearClip + splitDist * clipRange) * -1.0f;
            cascadeShadow.projectionMatrix = lightOrthogonalMatrix.mul(lightViewMatrix);

            lastSplitDist = cascadeSplits[i];
        }
    }
}
