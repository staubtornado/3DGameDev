#version 330

layout (location=0) in vec3 position;
layout (location=1) in vec3 normal;
layout (location=2) in vec3 tangent;
layout (location=3) in vec3 bitangent;
layout (location=4) in vec2 texCoord;

uniform mat4 modelMatrix;
uniform mat4 projViewMatrix;

void main() {
    vec4 initPos = vec4(position, 1.0);
    gl_Position = projViewMatrix * modelMatrix * initPos;
}