package org.narrativ27.vysshijvyvod;

import static org.lwjgl.glfw.Callbacks.*;
import static org.lwjgl.glfw.GLFW.*;

public class Window
{
    private final long handle;
    private final WindowState state;
    private InputHandler inputHandler;

    public Window(long handle, WindowState state)
    {
        this.handle = handle;
        this.state = state;
    }

    public void setInputHandler(InputHandler handler)
    {
        this.inputHandler = handler;
    }

    public InputHandler getInputHandler()
    {
        return inputHandler;
    }

    public boolean shouldClose()
    {
        return glfwWindowShouldClose(handle);
    }

    public long getHandle()
    {
        return handle;
    }

    public void toggleFullscreen()
    {
        state.toggleFullscreen(handle);
    }

    public void destroy()
    {
        glfwFreeCallbacks(handle);
        glfwDestroyWindow(handle);
    }
}
