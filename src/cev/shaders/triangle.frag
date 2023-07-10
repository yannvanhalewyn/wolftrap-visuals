#version 410 core

in mediump vec2 UV;
uniform vec2 resolution;
uniform float slider;
uniform float time;
out mediump vec4 fragColor;

float iterations = 3.0;
float complexity = 8.3;
float speed = .5;
float brightness = .1;

void main(void)
{
  vec2 uv = UV;
  uv.x *= resolution.x / resolution.y;
  vec2 uv0 = uv;

  vec3 tint = vec3(141.0 / 256., 22.0 / 256.0, 196.0 / 256.0);
  vec3 finalColor = vec3(0.0);

  for (float i = 0.0; i < iterations; i++) {
    uv = fract(uv * 2.) - 0.5;
    float d = length(uv) * exp(-length(uv0) * 2.0);

    vec3 col = tint * exp(-length(uv0) * 0.4);


    d = sin(d * complexity + time * speed)/12.0;
    //d = abs(d);
    d = brightness / d;
    finalColor += col * d;
  }

  // fragColor = vec4(uv, 0.0,1.0);
  fragColor = vec4(finalColor,1.0);
}
