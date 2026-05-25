package org.narrativ27.vysshijvyvod;

import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class WindowState
{
    private boolean fullscreen = false;
    private int windowedX, windowedY, windowedWidth, windowedHeight;

    public void captureWindowedBounds(long handle)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            var pX = stack.mallocInt(1);
            var pY = stack.mallocInt(1);
            var pW = stack.mallocInt(1);
            var pH = stack.mallocInt(1);
            glfwGetWindowPos(handle, pX, pY);
            glfwGetWindowSize(handle, pW, pH);
            windowedX = pX.get(0);
            windowedY = pY.get(0);
            windowedWidth = pW.get(0);
            windowedHeight = pH.get(0);
        }
    }

    public void captureInitialSize(long handle)
    {
        try (MemoryStack stack = MemoryStack.stackPush())
        {
            var pW = stack.mallocInt(1);
            var pH = stack.mallocInt(1);
            glfwGetWindowSize(handle, pW, pH);
            windowedWidth = pW.get(0);
            windowedHeight = pH.get(0);
        }
    }

    public void toggleFullscreen(long handle)
    {
        long monitor = glfwGetPrimaryMonitor();
        GLFWVidMode vidmode = glfwGetVideoMode(monitor);

        if (!fullscreen)
        {
            captureWindowedBounds(handle);
            glfwSetWindowMonitor(handle, monitor, 0, 0,
                    vidmode.width(), vidmode.height(), vidmode.refreshRate());
            fullscreen = true;
        }
        else
        {
            glfwSetWindowMonitor(handle, NULL,
                    windowedX, windowedY, windowedWidth, windowedHeight, 0);
            fullscreen = false;
        }
    }
}
