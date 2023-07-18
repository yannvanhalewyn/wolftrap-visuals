#version 410 core

in mediump vec2 pos;
// in mediump vec2 texcoord;
out mediump vec2 UV;

void main(void)
{
    gl_Position = vec4(pos, 0.0, 1.0);
    UV = pos;
}
