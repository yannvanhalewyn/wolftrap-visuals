#version 410 core

in mediump vec2 uv;
// out mediump vec2 fragUV;
uniform vec2 position;

void main()
{
  gl_Position = vec4(position + (uv / 30.0), 0.0, 1.0);
  // fragUV = uv;
}
