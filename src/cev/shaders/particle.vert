#version 410 core

in mediump vec2 uv;

uniform vec2 resolution;
uniform vec2 position;
uniform float size;

out mediump vec2 fragUV;

void main()
{
  vec2 screenUV = uv;
  // Make a perfect square
  screenUV.x *= resolution.y / resolution.x;

  // Scale the quad to the size in pixels
  screenUV *= size / resolution.x;

  gl_Position = vec4(position + screenUV, 0.0, 1.0);
  fragUV = uv;
}
