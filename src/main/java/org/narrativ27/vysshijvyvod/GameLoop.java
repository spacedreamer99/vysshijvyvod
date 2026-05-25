package org.narrativ27.vysshijvyvod;
import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import com.artemis.Entity;
import org.narrativ27.vysshijvyvod.ecs.components.Position;
import org.narrativ27.vysshijvyvod.ecs.components.Velocity;

public class GameLoop
{
    private final Window window;
    private final Renderer renderer;
    private final InputHandler inputHandler;
    private final PauseMenu pauseMenu;
    private final Cursor cursor;
    private final EncyclopediaMenu encyclopediaMenu;

    private long lastFpsTime = System.nanoTime();
    private int frameCount = 0;
    private String fpsText = "";

    private long lastFrameEnd = System.nanoTime();
    private long lastFrameTime = System.nanoTime(); // для deltaTime

    public GameLoop(Window window, Renderer renderer, InputHandler inputHandler,
                    PauseMenu pauseMenu, Cursor cursor, EncyclopediaMenu encyclopediaMenu)
    {
        this.window = window;
        this.renderer = renderer;
        this.inputHandler = inputHandler;
        this.pauseMenu = pauseMenu;
        this.cursor = cursor;
        this.encyclopediaMenu = encyclopediaMenu;
    }

    public void run()
    {
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LESS);

        long win = window.getHandle();

        // Создаём тестовую ECS-сущность (корабль)
        Entity ship = EcsWorld.getInstance().createEntity();
        Position shipPos = new Position();
        shipPos.x = 0; shipPos.y = 0; shipPos.z = 0;
        Velocity shipVel = new Velocity();
        shipVel.dx = 10; shipVel.dy = 10; shipVel.dz = 0;
        ship.edit().add(shipPos).add(shipVel);

        // Колбэк колеса мыши: зум в энциклопедии
        glfwSetScrollCallback(win, (w, xoffset, yoffset) -> {
            if (encyclopediaMenu.isVisible())
            {
                double[] sx = new double[1], sy = new double[1];
                glfwGetCursorPos(win, sx, sy);
                encyclopediaMenu.scrollWheel(-(float)yoffset, sx[0], sy[0]);
            }
        });

        double lastMouseX = 0, lastMouseY = 0;
        boolean firstMouse = true;

        glfwSetMouseButtonCallback(win, (w, button, action, mods) -> {
            inputHandler.mouseButtonCallback(w, button, action, mods);
        });

        while (!window.shouldClose())
        {
            if (GameState.shouldCloseRequested)
                break;

            int[] ww = new int[1], hh = new int[1];
            glfwGetFramebufferSize(win, ww, hh);
            pauseMenu.updateScreenSize(ww[0], hh[0]);
            cursor.updateScreenSize(ww[0], hh[0]);
            if (encyclopediaMenu.isVisible())
                encyclopediaMenu.updateScreenSize(ww[0], hh[0]);

            double[] mx = new double[1], my = new double[1];
            glfwGetCursorPos(win, mx, my);

            // Вычисляем deltaTime (реальное время между кадрами)
            long now = System.nanoTime();
            float deltaTime = (now - lastFrameTime) / 1e9f;
            lastFrameTime = now;
            if (deltaTime <= 0) deltaTime = 1/60f; // fallback

            // Обновляем ECS-мир (только когда игра не на паузе)
            if (!inputHandler.isPaused())
            {
                EcsWorld.process(deltaTime);
            }

            if (!inputHandler.isPaused())
            {
                if (firstMouse)
                {
                    lastMouseX = mx[0];
                    lastMouseY = my[0];
                    firstMouse = false;
                }

                float dx = (float)(mx[0] - lastMouseX);
                float dy = (float)(lastMouseY - my[0]);
                lastMouseX = mx[0];
                lastMouseY = my[0];

                if (dx != 0.0f || dy != 0.0f)
                    renderer.getCamera().rotate(dx, dy);

                float speed = 0.05f;
                Camera cam = renderer.getCamera();
                if (glfwGetKey(win, GLFW_KEY_W) == GLFW_PRESS) cam.moveForward(speed);
                if (glfwGetKey(win, GLFW_KEY_S) == GLFW_PRESS) cam.moveForward(-speed);
                if (glfwGetKey(win, GLFW_KEY_A) == GLFW_PRESS) cam.moveRight(-speed);
                if (glfwGetKey(win, GLFW_KEY_D) == GLFW_PRESS) cam.moveRight(speed);
                if (glfwGetKey(win, GLFW_KEY_SPACE) == GLFW_PRESS) cam.moveUp(speed);
                if (glfwGetKey(win, GLFW_KEY_LEFT_SHIFT) == GLFW_PRESS) cam.moveUp(-speed);

                cursor.setVisible(false);
            }
            else
            {
                firstMouse = true;
            }

            pauseMenu.updateHover(mx[0], my[0]);
            if (encyclopediaMenu.isVisible())
                encyclopediaMenu.updateHover(mx[0], my[0]);

            if (pauseMenu.isVisible())
            {
                cursor.setHover(pauseMenu.isAnyButtonHovered());
            }
            else if (encyclopediaMenu.isVisible())
            {
                cursor.setHover(encyclopediaMenu.isAnyInteractiveHovered());
            }
            else
            {
                cursor.setHover(false);
            }

            glViewport(0, 0, ww[0], hh[0]);
            renderer.updateProjection(ww[0], hh[0]);
            renderer.clear();
            renderer.render();
            pauseMenu.render();
            encyclopediaMenu.render();

            if (inputHandler.isPaused())
            {
                cursor.setVisible(true);
                cursor.render(mx[0], my[0]);
            }

            if (GameState.showFps && !inputHandler.isPaused())
            {
                long fpsNow = System.nanoTime();
                frameCount++;
                double elapsed = (fpsNow - lastFpsTime) / 1e9;
                if (elapsed >= 1.0)
                {
                    double frameTimeMs = elapsed / frameCount * 1000.0;
                    fpsText = String.format("FPS: %d", frameCount);
                    frameCount = 0;
                    lastFpsTime = fpsNow;
                }

                if (!fpsText.isEmpty())
                {
                    float scale = 0.4f;
                    float textW = pauseMenu.getTextWidth(fpsText, scale);
                    float textX = 20;
                    float textY = hh[0] - 60;
                    glDisable(GL_DEPTH_TEST);
                    glEnable(GL_BLEND);
                    glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
                    pauseMenu.drawText(fpsText, textX, textY, scale, 1.0f, 1.0f, 1.0f);
                    glEnable(GL_DEPTH_TEST);
                    glDisable(GL_BLEND);
                }
            }

            cursor.setPressed(glfwGetMouseButton(win, GLFW_MOUSE_BUTTON_LEFT) == GLFW_PRESS);

            glfwSwapBuffers(win);
            glfwPollEvents();

            int limit = GameState.fpsLimit;
            if (limit > 0 && !inputHandler.isPaused())
            {
                long limitNow = System.nanoTime();
                long targetFrameTime = TimeUnit.SECONDS.toNanos(1) / limit;
                long elapsed = limitNow - lastFrameEnd;
                long sleepNs = targetFrameTime - elapsed;
                if (sleepNs > 0)
                {
                    LockSupport.parkNanos(sleepNs);
                }
                lastFrameEnd = System.nanoTime();
            }
            else
            {
                lastFrameEnd = System.nanoTime();
            }
        }

        EcsWorld.dispose();
    }
}
