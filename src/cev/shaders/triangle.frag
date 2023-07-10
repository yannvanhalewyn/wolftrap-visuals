#version 410 core

in mediump vec2 UV;
uniform vec2 resolution;
out mediump vec4 fragColor;

void main(void)
{
  vec2 uv = UV;
  uv.x *= resolution.x / resolution.y;
  float d = length(uv);
  d -= 0.5;
  d = abs(d);
  d = smoothstep(0.0, 0.1, d);
  fragColor = vec4(d, d, d, 1.0);
}
