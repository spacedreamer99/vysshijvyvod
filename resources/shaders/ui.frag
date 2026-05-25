#version 330 core
in vec2 TexCoord;
out vec4 FragColor;

uniform sampler2D uTexture;
uniform vec3 uColor;
uniform float uAlpha;
uniform bool uUseTexture;
uniform vec3 uTextColor;

void main()
{
    if (uUseTexture)
    {
        vec4 texColor = texture(uTexture, TexCoord);
        FragColor = vec4(texColor.rgb * uTextColor, texColor.a);
    }
    else
    {
        FragColor = vec4(uColor, uAlpha);
    }
}
