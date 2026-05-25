#version 330 core
in vec2 TexCoord;
out vec4 FragColor;
uniform sampler2D uTextTexture;
uniform vec3 uTextColor;
void main() {
    float alpha = texture(uTextTexture, TexCoord).r;
    FragColor = vec4(uTextColor, alpha);
}
