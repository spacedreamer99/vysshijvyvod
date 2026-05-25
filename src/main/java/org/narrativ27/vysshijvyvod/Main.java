package org.narrativ27.vysshijvyvod;
import java.io.FileOutputStream;
import java.io.PrintStream;

public class Main
{
    public static void main(String[] args) throws Exception
    {
        try { System.setOut(new PrintStream(new FileOutputStream("game_debug.log"))); System.setErr(System.out); } catch (Exception e) { e.printStackTrace(); }
        System.out.println("Запуск 'Высший вывод'...");

        UIRenderer uiRenderer = null;
        PauseMenu pauseMenu = null;
        EncyclopediaMenu encyclopediaMenu = null;
        Cursor cursor = null;
        Window window = null;
        Renderer renderer = null;
        InputHandler inputHandler = null;
        GameLoop loop = null;

        try {
            // 1. Создаём объекты (конструкторы без OpenGL)
            uiRenderer = new UIRenderer();
            pauseMenu = new PauseMenu(uiRenderer);
            encyclopediaMenu = new EncyclopediaMenu(uiRenderer);
            cursor = new Cursor();
            renderer = new Renderer();

            // 2. Создаём окно (активирует контекст OpenGL)
            window = WindowFactory.create(pauseMenu, encyclopediaMenu);
            pauseMenu.setToggleFullscreenAction(window::toggleFullscreen);

            // 3. Инициализируем всё, что требует графический контекст
            uiRenderer.init();
            pauseMenu.init();
            encyclopediaMenu.init();
            cursor.init();
            renderer.init();

            // 4. Настраиваем обработчик ввода и игровой цикл
            inputHandler = new InputHandler(window.getHandle(), pauseMenu, encyclopediaMenu);
            loop = new GameLoop(window, renderer, inputHandler, pauseMenu, cursor, encyclopediaMenu);
            loop.run();
        } finally {
            if (cursor != null) cursor.dispose();
            if (encyclopediaMenu != null) encyclopediaMenu.dispose();
            if (uiRenderer != null) uiRenderer.dispose();
            if (renderer != null) renderer.dispose();
            Game.cleanup(window.getHandle());
        }
    }
}
