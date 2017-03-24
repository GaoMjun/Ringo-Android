
precision mediump float;

varying mediump vec2 textureCoordinate;

uniform sampler2D s_texture;

void main() {

  gl_FragColor = texture2D(s_texture, textureCoordinate);

}
