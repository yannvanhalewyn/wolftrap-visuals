#version 410 core

in mediump vec3 screenUV;
in mediump vec2 textureUV;

out mediump vec2 frag_screenUV;
out mediump vec2 frag_textureUV;

void main()
{
  gl_Position = vec4(screenUV, 1);
  frag_screenUV = screenUV.xy;
  frag_textureUV = textureUV;
}
