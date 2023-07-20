#version 410 core

in mediump vec3 vpos;
in mediump vec3 vcol;
out mediump vec3 pos;
out mediump vec4 col;

void main(void)
{
    gl_Position = vec4(vpos, 1.0);
    pos = vpos;
    col = vec4(vcol, 1.0);
}
