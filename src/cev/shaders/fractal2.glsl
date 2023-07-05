void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  vec2 uv = (fragCoord * 2.0 - iResolution.xy) / iResolution.y;
  vec2 uv0 = uv;

  vec3 tint = vec3(141.0 / 256., 22.0 / 256.0, 196.0 / 256.0);
  vec3 finalColor = vec3(0.0);

  for (float i = 0.0; i < 3.0; i++) {
    uv = fract(uv * 2.) - 0.5;
    float d = length(uv) * exp(-length(uv0) * 2.0);

    vec3 col = tint * exp(-length(uv0) * .4);


    d = sin(d*8.0 + iTime)/8.0;
    d = abs(d);
    d = .02 / d;
    finalColor += col * d;

  }

  // fragColor = vec4(uv, 0.0,1.0);
  fragColor = vec4(finalColor,1.0);
}

// This one is gorgeous;
void mainImage( out vec4 fragColor, in vec2 fragCoord )
{
  vec2 uv = (fragCoord * 2.0 - iResolution.xy) / iResolution.y;
  vec2 uv0 = uv;

  vec3 tint = vec3(141.0 / 256., 22.0 / 256.0, 196.0 / 256.0);
  vec3 finalColor = vec3(0.0);

  for (float i = 0.0; i < 4.0; i++) {
    uv = fract(uv * 2.) - 0.5;
    float d = length(uv) * exp(-length(uv0) * 2.0);

    vec3 col = tint * exp(-length(uv0) * 0.4);


    d = sin(d*12.0 + iTime * 2.0)/12.0;
    d = abs(d);
    d = .01 / d;
    finalColor += col * d;

  }

  // fragColor = vec4(uv, 0.0,1.0);
  fragColor = vec4(finalColor,1.0);
}
