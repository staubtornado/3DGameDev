#version 330

in vec2 outTextCoord;

out vec4 fragColor;

struct Material {
    vec4 diffuse;
};

uniform sampler2D textureSampler;
uniform Material material;

void main() {
    fragColor = texture(textureSampler, outTextCoord) + material.diffuse;
}