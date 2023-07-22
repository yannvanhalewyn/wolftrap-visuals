#version 410 core

in mediump vec2 frag_screenUV;
in mediump vec2 frag_textureUV;

uniform sampler2D tex;
uniform vec2 resolution;

out mediump vec4 fragColor;

void main()
{
  // vec2 newPos = pos;
  // newPos.x *= resolution.y / resolution.x;
  fragColor = texture(tex, frag_textureUV);
  // fragColor = vec4(1.0);

  vec2 texUV = (frag_textureUV - 0.5) * 2;
  fragColor.a = smoothstep(1.0, 0.3, length(texUV));
  // fragColor.a = 0.5;

  // fragColor = vec4(1.0);
}
