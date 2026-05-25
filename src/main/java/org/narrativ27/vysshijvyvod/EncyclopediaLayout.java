package org.narrativ27.vysshijvyvod;

public class EncyclopediaLayout {
    private static final int ABOUT_ENCY = 0, WHAT_FONTS = 1;
    private static final int ROMAN = 2, GOTHIC = 3, CALLIG = 4, RUNIC = 5;
    private static final int NODE_COUNT = 6;

    private float[] baseX = new float[NODE_COUNT];
    private float[] baseY = new float[NODE_COUNT];
    private float[] leafW = new float[NODE_COUNT];
    private float[] leafH = new float[NODE_COUNT];
    private float offsetX, offsetY;
    private float scale = 1.0f;
    private int screenW, screenH;

    public void setLeafSizes(int[] widths, int[] heights) {
        for (int i = 0; i < NODE_COUNT; i++) {
            leafW[i] = widths[i];
            leafH[i] = heights[i];
        }
    }

    public void setScreenSize(int w, int h) {
        screenW = w; screenH = h;
        calcBase();
    }

    private void calcBase() {
        float gap = 200; // отступ между листами
        float currentX = 100; // левый отступ первого листа
        float centerY = screenH / 2f; // вертикальный центр экрана

        for (int i = 0; i < NODE_COUNT; i++) {
            baseX[i] = currentX;
            baseY[i] = centerY - leafH[i] / 2f; // центрирование по вертикали
            currentX += leafW[i] + gap;
        }
    }

    public void pan(float dx, float dy) {
        offsetX -= dx / scale;
        offsetY -= dy / scale;
    }

    public void zoom(float amount, float mx, float my) {
        float old = scale;
        scale *= (amount > 0) ? 1.1f : 0.9f;
        if (scale < 0.15f) scale = 0.15f;
        if (scale > 3.0f) scale = 3.0f;
        float worldX = mx / old + offsetX;
        float worldY = (screenH - my) / old + offsetY;
        offsetX = worldX - mx / scale;
        offsetY = worldY - (screenH - my) / scale;
    }

    public float getNodeX(int i) { return (baseX[i] - offsetX) * scale; }
    public float getNodeY(int i) { return (baseY[i] - offsetY) * scale; }
    public float getNodeW(int i) { return leafW[i] * scale; }
    public float getNodeH(int i) { return leafH[i] * scale; }
    public float getScale() { return scale; }
}
