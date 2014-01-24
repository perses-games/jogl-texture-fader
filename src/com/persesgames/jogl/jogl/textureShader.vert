#if __VERSION__ >= 130
  #define attribute in
  #define varying out
#endif

#ifdef GL_ES
precision mediump float;
precision mediump int;
#endif

uniform float alpha;
uniform mat4 projection;
uniform mat4 modelView;

attribute vec4  attribute_Position;
attribute vec2  a_texCoord;

varying   vec2  v_texCoords;

void main(void) {
    v_texCoords = a_texCoord;
    gl_Position =  modelView * projection * attribute_Position;
}
