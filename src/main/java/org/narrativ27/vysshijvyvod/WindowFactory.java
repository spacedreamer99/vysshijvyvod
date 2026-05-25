package org.narrativ27.vysshijvyvod;

import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.*;

public class WindowFactory
{
    public static Window create(PauseMenu pauseMenu, EncyclopediaMenu encyclopediaMenu)
    {
        GlfwInitializer.init();

        glfwDefaultWindowHints();
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        glfwWindowHint(GLFW_RESIZABLE, GLFW_TRUE);

        long handle = glfwCreateWindow(1920, 1080, "Высший вывод", NULL, NULL);
        if (handle == 0L)
            throw new RuntimeException("Ошибка создания окна");

        WindowState state = new WindowState();
        state.captureInitialSize(handle);

        Window window = new Window(handle, state);
        InputHandler inputHandler = new InputHandler(handle, pauseMenu, encyclopediaMenu);
        window.setInputHandler(inputHandler);

        // Колбэк закрытия окна (Alt+F4, крестик)
        glfwSetWindowCloseCallback(handle, (w) ->
        {
            GameState.shouldCloseRequested = true;
        });

        // Колбэк клавиш
        glfwSetKeyCallback(handle, (win, key, scancode, action, mods) ->
        {
            inputHandler.handle(win, key, action, window);
        });

        glfwMakeContextCurrent(handle);
        GL.createCapabilities();
        glfwSwapInterval(1);
        glfwSetInputMode(handle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        // Отключаем системное закрытие окна (крестик и Alt+F4),
        // чтобы избежать гонки состояний с XFCE
        glfwSetWindowCloseCallback(handle, (w) -> { glfwSetWindowShouldClose(w, false); });
        // Отключаем системное закрытие окна (крестик и Alt+F4),
        // чтобы избежать гонки состояний с XFCE
        glfwSetWindowCloseCallback(handle, (w) -> { glfwSetWindowShouldClose(w, false); });
        // Отключаем системное закрытие окна (крестик и Alt+F4),
        // чтобы избежать гонки состояний с XFCE
        glfwSetWindowCloseCallback(handle, (w) -> { glfwSetWindowShouldClose(w, false); });
        glfwShowWindow(handle);

        state.toggleFullscreen(handle);
        return window;
    }
}
