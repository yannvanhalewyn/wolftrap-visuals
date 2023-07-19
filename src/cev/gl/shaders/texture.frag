#version 410 core

in mediump vec2 UV;
out mediump vec4 fragColor;
uniform sampler2D tex;

void main()
{
  fragColor = texture(tex, UV);
}
