#version 410 core

in mediump vec2 frag_uv;
out mediump vec4 fragColor;

void main()
{
  vec3 col;

  if (frag_uv.x < 0) {
    if (frag_uv.y < 0) {
      col = vec3(1.0, 0.0, 0.0);
    } else {
      col = vec3(0.0, 1.0, 0.0);
    }
  } else {
    if (frag_uv.y < 0) {
      col = vec3(1.0, 1.0, 1.0);
    } else {
      col = vec3(0.0, 0.0, 1.0);
    }
  }

  fragColor = vec4(col, 1.0);
}
