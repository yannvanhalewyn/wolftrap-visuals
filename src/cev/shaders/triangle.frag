#version 410 core

in mediump vec3 pos;
in mediump vec4 col;
uniform vec2 resolution;
uniform float time;

out mediump vec4 fragColor;

void main(void)
{
  vec2 uv = pos.xy;
  uv.x *= resolution.x / resolution.y;
  vec2 uv0 = uv;
  fragColor = col;
}
