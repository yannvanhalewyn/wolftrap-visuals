#version 410 core

in mediump vec2 UV;
uniform vec2 resolution;
out mediump vec4 fragColor;

void main(void)
{
  vec2 uv = UV;
  uv.x *= resolution.x / resolution.y;
  float d = length(uv);
  fragColor = vec4(d, d, 0.0, 1.0);
}
