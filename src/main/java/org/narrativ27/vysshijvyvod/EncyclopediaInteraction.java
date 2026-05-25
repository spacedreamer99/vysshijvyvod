package org.narrativ27.vysshijvyvod;

public class EncyclopediaInteraction {
    private final EncyclopediaLayout layout;
    private boolean bottomHover, bottomPressed;
    private boolean panning;
    private float startMX, startMY;
    private Runnable onBack, onClose;

    public EncyclopediaInteraction(EncyclopediaLayout layout) { this.layout = layout; }

    public void setCallbacks(Runnable onBack, Runnable onClose) {
        this.onBack = onBack;
        this.onClose = onClose;
    }

    public void updateHover(double mx, double my, int screenW, int screenH) {
        float glY = screenH - (float)my;
        bottomHover = glY >= 0 && glY <= 50;
        if (panning) {
            float dx = (float)mx - startMX;
            float dy = (float)my - startMY;
            layout.pan(dx, -dy);
            startMX = (float)mx;
            startMY = (float)my;
        }
    }

    public boolean isBottomHover() { return bottomHover; }
    public boolean isBottomPressed() { return bottomPressed; }

    public void mouseAction(double mx, double my, int button, int action, int screenH) {
        if (button != 0) return;
        float glY = screenH - (float)my;
        if (action == 1) {
            if (glY >= 0 && glY <= 50) {
                bottomPressed = true;
                return;
            }
            panning = true;
            startMX = (float)mx;
            startMY = (float)my;
        } else if (action == 0) {
            if (bottomPressed && glY >= 0 && glY <= 50) {
                if (onBack != null) onBack.run();
            }
            panning = false;
            bottomPressed = false;
        }
    }

    public void scrollWheel(float amount, double mx, double my) {
        layout.zoom(-amount, (float)mx, (float)my);
    }
}
