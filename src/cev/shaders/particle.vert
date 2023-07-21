#version 410 core

in mediump vec2 uv;
out mediump vec2 fragUV;

uniform vec2 resolution;
uniform vec2 position;
uniform float size;

void main()
{
  vec2 uv2 = uv;
  uv2.x *= resolution.y / resolution.x;

  gl_Position = vec4(position + uv2 / resolution.x * size, 0.0, 1.0);
  fragUV = uv;
}
