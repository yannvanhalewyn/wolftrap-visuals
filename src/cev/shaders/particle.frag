#version 410 core

// in mediump vec2 UV;
out mediump vec4 fragColor;
// uniform sampler2D tex;

void main()
{
  fragColor = vec4(1.0, 0.0, 0.0, 1.0);
  // float noise = texture(tex, UV).x;
  // fragColor = vec4(vec3(noise), 1.0);
}
