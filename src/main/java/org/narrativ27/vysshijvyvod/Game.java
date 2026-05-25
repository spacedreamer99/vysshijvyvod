package org.narrativ27.vysshijvyvod;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.opengl.GL;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Game
{
    public static void main(String[] args)
    {
        System.out.println("[DEBUG] Запуск игры");

        if (!glfwInit())
        {
            System.err.println("[ERROR] Не удалось инициализировать GLFW");
            throw new RuntimeException("Ошибка инициализации GLFW");
        }
        System.out.println("[DEBUG] GLFW инициализирован");
        glfwSetErrorCallback(GLFWErrorCallback.createPrint(System.err));

        long window = glfwCreateWindow(800, 600, "Моя игра", NULL, NULL);
        if (window == NULL)
        {
            System.err.println("[ERROR] Не удалось создать окно");
            glfwTerminate();
            throw new RuntimeException("Ошибка создания окна");
        }
        System.out.println("[DEBUG] Окно создано, хэндл: " + window);

        // Установка колбэка закрытия
        glfwSetWindowCloseCallback(window, (win) ->
        {
            System.out.println("[DEBUG] Колбэк закрытия вызван (Alt+F4, крестик и т.д.)");
            GameState.shouldCloseRequested = true;
        });
        System.out.println("[DEBUG] Колбэк закрытия установлен");

        glfwMakeContextCurrent(window);
        GL.createCapabilities();
        glClearColor(0.1f, 0.1f, 0.1f, 1.0f);

        System.out.println("[DEBUG] Вход в главный цикл");
        while (!glfwWindowShouldClose(window))
        {
            if (GameState.shouldCloseRequested)
            {
                System.out.println("[DEBUG] Обнаружен флаг закрытия, начинаем корректный выход");
                cleanup(window);
                glfwSetWindowShouldClose(window, true);
                System.out.println("[DEBUG] Установлен glfwWindowShouldClose = true, выходим из цикла");
                break;
            }

            // ... игровая логика и рендер
            glClear(GL_COLOR_BUFFER_BIT);
            // ... ваш код отрисовки ...

            glfwSwapBuffers(window);
            glfwPollEvents();
        }

        System.out.println("[DEBUG] Главный цикл завершён");
        if (!GameState.shouldCloseRequested)
        {
            System.out.println("[DEBUG] Неожиданное завершение: вызов cleanup вне флага");
        }
        cleanup(window);
        System.out.println("[DEBUG] Приложение завершает работу");
        System.exit(0);
    }

    public static void cleanup(long window)
    {
        System.out.println("[DEBUG] Начало процедуры очистки (cleanup)");
        org.lwjgl.opengl.GL11.glFinish();

        // Здесь освобождайте свои ресурсы OpenGL
        // System.out.println("[DEBUG] Удаление текстур/буферов...");
        // ... glDeleteTextures / glDeleteBuffers ...

        System.out.println("[DEBUG] Уничтожение окна (glfwDestroyWindow)");
        System.out.println("[DEBUG] Окно уничтожено");

        System.out.println("[DEBUG] Завершение GLFW (glfwTerminate)");
        glfwTerminate();
        System.out.println("[DEBUG] GLFW завершён");

        // Колбэк ошибок освобождать не нужно, это сделает GlfwInitializer.terminate()
        System.out.println("[DEBUG] Процедура очистки завершена");
    }
}
