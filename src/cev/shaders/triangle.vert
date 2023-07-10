#version 410 core

in mediump vec3 point;
in mediump vec2 texcoord;
// out mediump vec2 UV;

void main(void)
{
    gl_Position = vec4(point, 1.0);
    // UV = texcoord;
}
