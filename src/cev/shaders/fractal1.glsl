vec3 palette(in float t) {
  vec3 a = vec3(0.5, 0.5, 0.5);
  vec3 b = vec3(0.5, 0.5, 0.5);
  vec3 c = vec3(1.0, 1.0, 1.0);
  vec3 d = vec3(0.263, 0.416, 0.557);

  // vec3 a = vec3(0.528, 0.811, 0.588);
  // vec3 b = vec3(1.068, 0.668, 1.568);
  // vec3 c = vec3(2.158, 0.088, -1.712);
  // vec3 d = vec3(-0.342, -6.611, -0.902);

  // vec3 a = vec3(0.528, 0.811, 0.588);
  // vec3 b = vec3(1.238, 3.138, 0.448);
  // vec3 c = vec3(2.158, 0.198, -1.712);
  // vec3 d = vec3(-0.342, -6.611, -0.902);


  // vec3 a = vec3(0.528, 0.811, 0.588);
  // vec3 b = vec3(0.448, 0.108, -0.282);
  // vec3 c = vec3(2.158, 0.648, -1.712);
  // vec3 d = vec3(-0.342, -6.611, -0.902);

  // vec3 a = vec3(0.590, 0.811, 0.120);
  // vec3 b = vec3(0.448, 0.618, 1.118);
  // vec3 c = vec3(1.318, 0.648, 0.138);
  // vec3 d = vec3(-4.242, -6.611, -4.045);

  return a + b*cos(6.28318 * (c * t + d));
}

void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  vec2 uv = (fragCoord * 2.0 - iResolution.xy) / iResolution.y;
  vec2 uv0 = uv;

  vec3 tint = vec3(141.0 / 256., 22.0 / 256.0, 196.0 / 256.0);
  vec3 finalColor = vec3(0.0);

  for (float i = 0.0; i < 1.0; i++) {
    uv = fract(uv * 1.5) - 0.5;
    float d = length(uv) * exp(-length(uv0));

    vec3 col = tint * smoothstep(0.0, 0.2, length(uv0) + iTime * .4);

    d = sin(d*8.0 + iTime)/8.0;
    d = abs(d);
    d = .02 / d;
    finalColor += col * d;
  }

  fragColor = vec4(finalColor,1.0);
}
