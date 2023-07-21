#version 410 core

in mediump vec2 UV;
out mediump vec4 fragColor;
uniform sampler2D tex;

void main()
{
  float noise = texture(tex, UV).x;
  fragColor = vec4(vec3(noise), 1.0);
}
