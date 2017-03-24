
#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;

void main() {

  vec2 uv = textureCoordinate;

  if (textureCoordinate.x > 0.5){

    uv.x = 1.0 - textureCoordinate.x;

  }

  gl_FragColor = texture2D(s_texture, uv);
}
