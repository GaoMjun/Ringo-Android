
#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;

void main() {

  vec4 color = texture2D(s_texture, textureCoordinate);

  gl_FragColor = vec4(1.0-color.r, 1.0-color.g, 1.0-color.b, 1.0);
}
