#version 410 core

in mediump vec2 point;
// in mediump vec2 texcoord;
out mediump vec2 UV;

void main(void)
{
    gl_Position = vec4(point, 0.0, 1.0);
    UV = point;
}
