package org.narrativ27.vysshijvyvod;

public class Button
{
    private Texture texture;
    private float x, y, width, height;
    private boolean hover = false;

    public Button(Texture texture, float width, float height)
    {
        this.texture = texture;
        this.width = width;
        this.height = height;
    }

    public void setPosition(float x, float y) { this.x = x; this.y = y; }
    public float getX() { return x; }
    public float getY() { return y; }
    public float getWidth() { return width; }
    public float getHeight() { return height; }

    public void updateHover(double mouseX, double mouseY, int screenH)
    {
        double glY = screenH - mouseY;
        hover = mouseX >= x && mouseX <= x + width &&
                glY >= y && glY <= y + height;
    }

    public boolean isHover() { return hover; }
    public boolean contains(double mouseX, double mouseY, int screenH)
    {
        double glY = screenH - mouseY;
        return mouseX >= x && mouseX <= x + width &&
               glY >= y && glY <= y + height;
    }

    public Texture getTexture() { return texture; }
    public void dispose() { if (texture != null) texture.dispose(); }
}
