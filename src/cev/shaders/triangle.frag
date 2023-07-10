#version 410 core

in mediump vec2 UV;
uniform vec2 resolution;
uniform float slider;
uniform float time;
out mediump vec4 fragColor;

void main(void)
{
  vec2 uv = UV;
  uv.x *= resolution.x / resolution.y;
  float d = length(uv);
  d = sin(d * 8.0 + time) / 8.0;
  d = abs(d);
  d = slider / d * 0.2;
  fragColor = vec4(d, d, d, 1.0);
}
