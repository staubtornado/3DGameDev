package org.game3d.dev.engine;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;

import java.util.concurrent.Callable;

import static org.lwjgl.glfw.Callbacks.glfwFreeCallbacks;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {
    @Getter private final long windowHandle;
    private final Callable<Void> windowResizeCallback;
    @Getter private int width;
    @Getter private int height;
    @Getter private MouseInput mouseInput;

    @Contract(pure = true)
    public Window(String title, @NotNull WindowOptions options, Callable<Void> windowResizeCallback) {
        this.windowResizeCallback = windowResizeCallback;
        if (!glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW.");
        }

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE);

        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        if (options.compatibleProfile) {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_COMPAT_PROFILE);
        } else {
            glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
            glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GL_TRUE);
        }

        // Enable anti-aliasing
        glfwWindowHint(GLFW_SAMPLES, 4);

        if (options.width > 0 && options.height > 0) {
            this.width = options.width;
            this.height = options.height;
        } else {
            glfwWindowHint(GLFW_MAXIMIZED, GLFW_TRUE);
            GLFWVidMode vidMode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            if (vidMode == null) {
                throw new IllegalStateException("Failed to get video mode.");
            }
            this.width = vidMode.width();
            this.height = vidMode.height();
        }

        this.windowHandle = glfwCreateWindow(this.width, this.height, title, NULL, NULL);
        if (this.windowHandle == NULL) {
            throw new IllegalStateException("Failed to create window.");
        }

        glfwSetInputMode(this.windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        glfwSetFramebufferSizeCallback(this.windowHandle, (_, width, height) -> resized(width, height));
        glfwSetErrorCallback((int errorCode, long msgPtr) -> {
            throw new IllegalStateException("GLFW error: " + errorCode + " - " + msgPtr);
        });
        glfwSetKeyCallback(this.windowHandle, (_, key, _, action, _) -> this.keyCallback(key, action));

        glfwMakeContextCurrent(this.windowHandle);

        if (options.fps > 0) {
            glfwSwapInterval(0);
        } else {
            glfwSwapInterval(1);
        }
        glfwShowWindow(this.windowHandle);

        int[] arrWidth = new int[1];
        int[] arrHeight = new int[1];
        glfwGetFramebufferSize(this.windowHandle, arrWidth, arrHeight);
        this.width = arrWidth[0];
        this.height = arrHeight[0];

        this.mouseInput = new MouseInput(this.windowHandle, this.width, this.height);
    }

    public void cleanup() {
        glfwFreeCallbacks(this.windowHandle);
        glfwDestroyWindow(this.windowHandle);
        glfwTerminate();
        GLFWErrorCallback callback = glfwSetErrorCallback(null);
        if (callback != null) {
            callback.free();
        }
    }

    public boolean isKeyPressed(int keyCode) {
        return glfwGetKey(this.windowHandle, keyCode) == GLFW_PRESS;
    }

    public void keyCallback(int key, int action) {
        if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
            glfwSetWindowShouldClose(this.windowHandle, true);
        }
    }

    public void pollEvents() {
        glfwPollEvents();
    }

    protected void resized(int width, int height) {
        this.width = width;
        this.height = height;
        try {
            this.windowResizeCallback.call();
        } catch (Exception e) {
            throw new IllegalStateException("Failed to call window resize callback.", e);
        }
        this.mouseInput.resize(width, height);
    }

    public void update() {
        glfwSwapBuffers(this.windowHandle);
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(this.windowHandle);
    }

    @RequiredArgsConstructor
    public static class WindowOptions {
        public final boolean compatibleProfile;
        public final int fps;
        public final int height;
        public final int ups = Engine.TARGET_UPS;
        public final int width;
    }
}
