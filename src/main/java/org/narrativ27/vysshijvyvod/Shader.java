package org.narrativ27.vysshijvyvod;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.FloatBuffer;
import static org.lwjgl.opengl.GL20.*;

public class Shader
{
    public final int programId;

    public Shader(String vertexPath, String fragmentPath) throws IOException
    {
        String vertexCode = Files.readString(Paths.get(vertexPath));
        String fragmentCode = Files.readString(Paths.get(fragmentPath));

        int vertexShader = glCreateShader(GL_VERTEX_SHADER);
        glShaderSource(vertexShader, vertexCode);
        glCompileShader(vertexShader);
        checkShaderCompile(vertexShader, "VERTEX");

        int fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
        glShaderSource(fragmentShader, fragmentCode);
        glCompileShader(fragmentShader);
        checkShaderCompile(fragmentShader, "FRAGMENT");

        programId = glCreateProgram();
        glAttachShader(programId, vertexShader);
        glAttachShader(programId, fragmentShader);
        glLinkProgram(programId);
        checkProgramLink(programId);

        glDeleteShader(vertexShader);
        glDeleteShader(fragmentShader);
    }

    public void use() { glUseProgram(programId); }

    public void setUniform4f(String name, float r, float g, float b, float a)
    {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1) glUniform4f(loc, r, g, b, a);
    }

    public void setUniform1i(String name, int value)
    {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1) glUniform1i(loc, value);
    }

    public void setUniform1f(String name, float value)
    {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1) glUniform1f(loc, value);
    }

    public void setUniform3f(String name, float x, float y, float z)
    {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1) glUniform3f(loc, x, y, z);
    }

    public void setUniform2f(String name, float x, float y)
    {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1) glUniform2f(loc, x, y);
    }

    public void setUniformMatrix4fv(String name, FloatBuffer matrix)
    {
        int loc = glGetUniformLocation(programId, name);
        if (loc != -1) glUniformMatrix4fv(loc, false, matrix);
    }

    public void dispose() { glDeleteProgram(programId); }

    private void checkShaderCompile(int shader, String type)
    {
        if (glGetShaderi(shader, GL_COMPILE_STATUS) == GL_FALSE)
            System.err.println("ОШИБКА компиляции шейдера (" + type + "): " + glGetShaderInfoLog(shader));
    }

    private void checkProgramLink(int program)
    {
        if (glGetProgrami(program, GL_LINK_STATUS) == GL_FALSE)
            System.err.println("ОШИБКА линковки программы: " + glGetProgramInfoLog(program));
    }
}
