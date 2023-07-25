#version 410 core

in mediump vec2 frag_uv;
out mediump vec4 fragColor;

float border = 0.03;
float edge = 0.10;

vec3 tint = vec3(141.0 / 256., 22.0 / 256.0, 196.0 / 256.0);

void main()
{
  float d = length(frag_uv);
  d = smoothstep(0.1, 0.999, 1 - d);

  fragColor = vec4(tint * d, 1.0);
}
