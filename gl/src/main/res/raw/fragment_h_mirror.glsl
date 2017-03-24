
#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;

void main() {

  vec2 uv = textureCoordinate;

  if (textureCoordinate.y > 0.5) {

    uv.y = 1.0 - textureCoordinate.y;

  }

  gl_FragColor = texture2D(s_texture, uv);
}
