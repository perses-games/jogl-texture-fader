#if __VERSION__ >= 130
  #define attribute in
  #define varying out
#endif

precision mediump float;
precision mediump int;

uniform float alpha;
uniform mat4 projection;
uniform mat4 modelView;

attribute vec4  attribute_Position;

varying   vec2  v_texCoords;

void main(void) {
    float tx = attribute_Position.x + 1.0;
    float ty = attribute_Position.y + 1.0;

    tx = tx / 2.0;
    ty = ty / 2.0;

    v_texCoords = vec2(tx, ty);
    gl_Position = projection * modelView * vec4(attribute_Position.x, -attribute_Position.y, 0, 1);
}
