package org.narrativ27.vysshijvyvod;

public class GameState
{
    public static volatile boolean shouldCloseRequested = false;
    public static volatile boolean showFps = false;
    public static volatile int fpsLimit = 0;
    public static volatile boolean vsync = true;
    public static volatile boolean returnToMainMenu = false;
}
