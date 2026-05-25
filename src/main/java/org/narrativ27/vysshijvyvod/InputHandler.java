package org.narrativ27.vysshijvyvod;
import static org.lwjgl.glfw.GLFW.*;

public class InputHandler
{
    private long windowHandle;
    private PauseMenu pauseMenu;
    private EncyclopediaMenu encyclopediaMenu;
    private int pressedButtonIndex = -1;

    public InputHandler(long windowHandle, PauseMenu pauseMenu, EncyclopediaMenu encyclopediaMenu)
    {
        this.windowHandle = windowHandle;
        this.pauseMenu = pauseMenu;
        this.encyclopediaMenu = encyclopediaMenu;
        encyclopediaMenu.setOnClose(this::resumeGame);
        encyclopediaMenu.setOnBackToMenu(this::closeEncyclopedia);   // <-- добавлено
    }

    public void handle(long win, int key, int action, Window window)
    {
        if (action != GLFW_RELEASE) return;
        if (key == GLFW_KEY_ESCAPE)
        {
            if (encyclopediaMenu.isVisible())
                resumeGame();
            else
                togglePause();
        }
        else if (key == GLFW_KEY_F11)
            window.toggleFullscreen();
    }

    public void togglePause()
    {
        if (pauseMenu.isVisible())
        {
            pauseMenu.hide();
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        }
        else
        {
            pauseMenu.show();
            glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        }
        clearAllPressed();
    }

    public void openEncyclopedia()
    {
        pauseMenu.hide();
        encyclopediaMenu.setVisible(true);
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
        clearAllPressed();
    }

    public void closeEncyclopedia()
    {
        encyclopediaMenu.setVisible(false);
        pauseMenu.show();
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
    }

    public void resumeGame()
    {
        encyclopediaMenu.setVisible(false);
        pauseMenu.hide();
        glfwSetInputMode(windowHandle, GLFW_CURSOR, GLFW_CURSOR_DISABLED);
        clearAllPressed();
    }

    public boolean isPaused()
    {
        return pauseMenu.isVisible() || encyclopediaMenu.isVisible();
    }

    public void mouseButtonCallback(long win, int button, int action, int mods)
    {
        if (button != GLFW_MOUSE_BUTTON_LEFT) return;
        if (!isPaused()) return;

        double[] xpos = new double[1], ypos = new double[1];
        glfwGetCursorPos(win, xpos, ypos);
        double mx = xpos[0], my = ypos[0];

        if (encyclopediaMenu.isVisible())
        {
            encyclopediaMenu.mouseAction(mx, my, button, action);
            return;
        }

        if (action == GLFW_PRESS)
        {
            pressedButtonIndex = pauseMenu.getButtonIndex(mx, my);
            if (pressedButtonIndex >= 0)
                pauseMenu.setButtonPressed(pressedButtonIndex, true);
        }
        else if (action == GLFW_RELEASE)
        {
            int releaseIndex = pauseMenu.getButtonIndex(mx, my);
            if (pressedButtonIndex >= 0)
                pauseMenu.setButtonPressed(pressedButtonIndex, false);

            if (pressedButtonIndex >= 0 && releaseIndex == pressedButtonIndex)
            {
                switch (pressedButtonIndex)
                {
                    case 0: togglePause(); break;          // Continue
                    case 1: openEncyclopedia(); break;     // Encyclopedia
                    case 2: pauseMenu.doAction(2); break;  // Window/Fullscreen
                    case 3: pauseMenu.doAction(3); break;  // FPS counter toggle
                    case 4: pauseMenu.doAction(4); break;  // FPS lock cycle
                    case 5: // Exit с подтверждением
                        if (pauseMenu.tryExit())
                            GameState.shouldCloseRequested = true;
                        break;
                }
            }
            else if (pressedButtonIndex == -1 && releaseIndex == -1)
            {
                togglePause();
            }
            else
            {
                pauseMenu.resetExitConfirmation();
            }

            clearAllPressed();
            pressedButtonIndex = -1;
        }
    }

    private void clearAllPressed()
    {
        for (int i = 0; i < 6; i++)
            pauseMenu.setButtonPressed(i, false);
    }
}
