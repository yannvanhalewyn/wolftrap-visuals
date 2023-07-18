#version 410 core

in mediump vec3 vpos;
in mediump vec3 vcol;
// in mediump vec2 texcoord;
out mediump vec3 pos;
out mediump vec3 col;

void main(void)
{
    gl_Position = vec4(vpos, 1.0);
    pos = vpos;
    col = vcol;
}
