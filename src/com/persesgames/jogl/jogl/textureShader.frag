#if __VERSION__ >= 130
  #define varying in
  out vec4 mgl_FragColor;
  #define texture2D texture
  #define gl_FragColor mgl_FragColor
#endif

precision mediump float;
precision mediump int;

uniform sampler2D u_texture;
uniform float alpha;

varying vec2 v_texCoords;

void main (void) {
    gl_FragColor = texture2D(u_texture, v_texCoords);
    //gl_FragColor.r = 1.0;
    gl_FragColor.a = alpha;
}
