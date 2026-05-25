package org.narrativ27.vysshijvyvod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera
{
    private final Vector3f position;
    private final Vector3f front;
    private final Vector3f up;
    private final Vector3f right;
    private final Vector3f worldUp;

    private float yaw = -90.0f;   // смотрим вдоль -Z
    private float pitch = 0.0f;
    private float movementSpeed = 5.0f;
    private float mouseSensitivity = 0.1f;

    private float fov = 70.0f;
    private float near = 0.1f;
    private float far = 100.0f;

    public Camera()
    {
        position = new Vector3f(0.0f, 0.0f, 5.0f);
        front = new Vector3f(0.0f, 0.0f, -1.0f);
        up = new Vector3f(0.0f, 1.0f, 0.0f);
        right = new Vector3f(1.0f, 0.0f, 0.0f);
        worldUp = new Vector3f(0.0f, 1.0f, 0.0f);
        updateVectors();
    }

    // Обновление направлений из углов Эйлера
    private void updateVectors()
    {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        front.x = (float)(Math.cos(yawRad) * Math.cos(pitchRad));
        front.y = (float)(Math.sin(pitchRad));
        front.z = (float)(Math.sin(yawRad) * Math.cos(pitchRad));
        front.normalize();

        front.cross(worldUp, right).normalize();
        right.cross(front, up).normalize();
    }

    public Matrix4f getViewMatrix()
    {
        Vector3f target = new Vector3f(position).add(front);
        return new Matrix4f().lookAt(position, target, up);
    }

    public Matrix4f getProjectionMatrix(float aspectRatio)
    {
        return new Matrix4f().perspective((float) Math.toRadians(fov), aspectRatio, near, far);
    }

    public Vector3f getPosition()
    {
        return new Vector3f(position);
    }

    // Движение вперёд (по направлению взгляда)
    public void moveForward(float amount)
    {
        position.add(new Vector3f(front).mul(amount));
    }

    // Движение вбок (право)
    public void moveRight(float amount)
    {
        position.add(new Vector3f(right).mul(amount));
    }

    // Движение вверх (вдоль мировой оси Y)
    public void moveUp(float amount)
    {
        position.y += amount;
    }

    // Поворот камеры мышью
    public void rotate(float deltaYaw, float deltaPitch)
    {
        yaw += deltaYaw * mouseSensitivity;
        pitch += deltaPitch * mouseSensitivity;

        // Ограничение угла обзора
        if (pitch > 89.0f) pitch = 89.0f;
        if (pitch < -89.0f) pitch = -89.0f;

        updateVectors();
    }
}
