#version 410 core

in mediump vec3 vpos;
in mediump vec3 vcol;
in mediump float vopacity;
// in mediump vec2 texcoord;
out mediump vec3 pos;
out mediump vec4 col;

void main(void)
{
    gl_Position = vec4(vpos, 1.0);
    col = vec4(vopacity);
    pos = vpos;
    col = vec4(vcol, vopacity);
}
