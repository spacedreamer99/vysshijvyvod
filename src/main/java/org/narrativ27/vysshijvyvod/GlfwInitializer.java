package org.narrativ27.vysshijvyvod;

import org.lwjgl.glfw.GLFWErrorCallback;

import static org.lwjgl.glfw.GLFW.*;

public class GlfwInitializer
{
    private static GLFWErrorCallback errorCallback;

    public static void init()
    {
        errorCallback = GLFWErrorCallback.createPrint(System.err);
        errorCallback.set();

        if (!glfwInit())
        {
            throw new IllegalStateException("Невозможно инициализировать GLFW");
        }
    }

    public static void terminate()
    {
        glfwTerminate();
        if (errorCallback != null) errorCallback.free();
    }
}
