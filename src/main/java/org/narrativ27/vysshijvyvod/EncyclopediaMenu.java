package org.narrativ27.vysshijvyvod;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL30.*;

public class EncyclopediaMenu {
    private EncyclopediaLayout layout;
    private EncyclopediaInteraction interaction;
    private EncyclopediaRenderer renderer;
    private boolean visible;
    private int screenW, screenH;
    private Runnable onClose;

    public EncyclopediaMenu(UIRenderer uiRenderer) {
        layout = new EncyclopediaLayout();
        interaction = new EncyclopediaInteraction(layout);
        renderer = new EncyclopediaRenderer(uiRenderer);
        interaction.setCallbacks(() -> {},
                                 () -> { if (onClose != null) onClose.run(); });
    }

    public void setOnClose(Runnable r) { onClose = r; interaction.setCallbacks(() -> {}, onClose); }
    public void setOnBackToMenu(Runnable r) { interaction.setCallbacks(r, onClose); }

    public void init() throws Exception {
        renderer.init();
        layout.setLeafSizes(renderer.getLeafWidths(), renderer.getLeafHeights());
        layout.setScreenSize(1920, 1080);
        renderer.setScreenSize(1920, 1080);
    }

    public void setVisible(boolean v) { visible = v; }
    public boolean isVisible() { return visible; }

    public void updateScreenSize(int w, int h) {
        screenW = w; screenH = h;
        layout.setScreenSize(w, h);
        renderer.setScreenSize(w, h);
    }

    public void updateHover(double mx, double my) { interaction.updateHover(mx, my, screenW, screenH); }
    public boolean isAnyInteractiveHovered() { return interaction.isBottomHover(); }
    public void mouseAction(double mx, double my, int btn, int act) { interaction.mouseAction(mx, my, btn, act, screenH); }
    public void scrollWheel(float amount, double mx, double my) { interaction.scrollWheel(amount, mx, my); }

    public void render() {
        if (!visible) return;
        glDisable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        renderer.begin();
        renderer.drawDimOverlay();
        renderer.renderForest(layout);
        float br = interaction.isBottomPressed() ? 0.1f : (interaction.isBottomHover() ? 0.35f : 0.2f);
        renderer.drawButton(0, 0, screenW, 50, br, br, br, 0.9f);
        renderer.drawText("BACK", screenW/2f - 50, 10, 0.5f, 1,1,1);
        glEnable(GL_DEPTH_TEST);
        glDisable(GL_BLEND);
    }

    public void dispose() { renderer.dispose(); }
}
