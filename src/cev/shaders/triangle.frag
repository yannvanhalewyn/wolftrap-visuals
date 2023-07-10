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

  vec3 tint = vec3(141.0 / 256., 22.0 / 256.0, 196.0 / 256.0);

  float d = length(uv);
  d = sin(d * 8.0 + time) / 8.0;
  d = abs(d);
  d = slider / d * 0.2;

  tint *= d;
  fragColor = vec4(tint, 1.0);
}
