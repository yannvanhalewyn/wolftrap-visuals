#version 410 core

in mediump vec2 uv;

uniform vec2 resolution;
uniform vec2 position;
uniform float size;

out mediump vec2 frag_uv;

void main()
{
  // Find the corner using UV
  vec2 pos = position + uv * size / 2;

  // pos goes from 0 to resolution.
  // We want it from -1 to 1
  pos = (pos * 2) / resolution - 1;

  // And then scale back down
  gl_Position = vec4(pos, 0.0, 1.0);
  // gl_Position = vec4(uv * 2, 0.0, 1.0);
  frag_uv = uv;
}
