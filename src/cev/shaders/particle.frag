#version 410 core

in mediump vec2 fragUV;
out mediump vec4 fragColor;

float border = 0.03;
float edge = 0.10;

void main()
{
  if (abs(fragUV.x) > 1 - border ||
      abs(fragUV.y) > 1 - border ||
      abs(abs(fragUV.x) - abs(fragUV.y)) > 1 - edge) {
    fragColor = vec4(0.2, 0.7, 0.3, 1.0);
  } else {
    fragColor = vec4(1.4, 0.1, 0.8, 1.0);
  }
}
