package org.narrativ27.vysshijvyvod;
import java.io.FileOutputStream;
import java.io.PrintStream;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;

public class Main {
    public static void main(String[] args) throws Exception {
        try { System.setOut(new PrintStream(new FileOutputStream("game_debug.log"))); System.setErr(System.out); } catch (Exception e) { e.printStackTrace(); }
        System.out.println("Запуск 'Высший вывод'...");

        GlfwInitializer.init();
        long windowHandle = glfwCreateWindow(1920, 1080, "Высший вывод", glfwGetPrimaryMonitor(), 0L);
        glfwMakeContextCurrent(windowHandle);
        org.lwjgl.opengl.GL.createCapabilities();
        glfwSwapInterval(GameState.vsync ? 1 : 0);

        UIRenderer uiRenderer = new UIRenderer();
        uiRenderer.init();

        // Главное меню (создаётся один раз)
        MainMenu mainMenu = new MainMenu(uiRenderer);
        mainMenu.init();
        mainMenu.setScreenSize(1920, 1080);

        // Колбэк мыши для главного меню
        glfwSetMouseButtonCallback(windowHandle, (win, button, action, mods) -> {
            double[] xpos = new double[1], ypos = new double[1];
            glfwGetCursorPos(win, xpos, ypos);
            mainMenu.mouseButton(xpos[0], ypos[0], button, action);
        });

        while (!GameState.shouldCloseRequested) {
            // Цикл главного меню
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_NORMAL);
            while (!mainMenu.isPlayClicked() && !GameState.shouldCloseRequested) {
                glfwPollEvents();
                double[] mx = new double[1], my = new double[1];
                glfwGetCursorPos(windowHandle, mx, my);
                mainMenu.updateHover(mx[0], my[0]);

                glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
                mainMenu.render();
                glfwSwapBuffers(windowHandle);
            }

            if (GameState.shouldCloseRequested) break;

            // Запуск игры
            PauseMenu pauseMenu = new PauseMenu(uiRenderer);
            EncyclopediaMenu encyclopediaMenu = new EncyclopediaMenu(uiRenderer);
            Cursor cursor = new Cursor();
            Renderer renderer = new Renderer();

            pauseMenu.init();
            encyclopediaMenu.init();
            cursor.init();
            renderer.init();

            Window window = new Window(windowHandle, new WindowState());
            pauseMenu.setToggleFullscreenAction(window::toggleFullscreen);
            pauseMenu.setToggleVsyncAction(() -> {
                glfwSwapInterval(GameState.vsync ? 1 : 0);
            });

            InputHandler inputHandler = new InputHandler(windowHandle, pauseMenu, encyclopediaMenu);
            glfwSetKeyCallback(windowHandle, (win, key, scancode, action, mods) -> { inputHandler.handle(win, key, action, window); });

            GameLoop loop = new GameLoop(window, renderer, inputHandler, pauseMenu, cursor, encyclopediaMenu);
            loop.run();

            cursor.dispose();
            encyclopediaMenu.dispose();
            pauseMenu.dispose();
            renderer.dispose();

            // Если вернулись из игры по кнопке MAIN MENU
            if (GameState.returnToMainMenu) {
                GameState.returnToMainMenu = false;
                mainMenu.reset();
                // Восстанавливаем колбэк мыши для главного меню
                glfwSetMouseButtonCallback(windowHandle, (win, button, action, mods) -> {
                    double[] xpos = new double[1], ypos = new double[1];
                    glfwGetCursorPos(win, xpos, ypos);
                    mainMenu.mouseButton(xpos[0], ypos[0], button, action);
                });
                continue;
            }
        }

        mainMenu.dispose();
        uiRenderer.dispose();
        Game.cleanup(windowHandle);
        System.exit(0);
    }
}
