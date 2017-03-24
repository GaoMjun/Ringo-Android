
attribute vec4 vPosition;
attribute vec4 inputTextureCoordinate;

uniform mat4 transformMatrix;

varying vec2 textureCoordinate;

void main() {

    gl_Position = vPosition;

    textureCoordinate = (transformMatrix * inputTextureCoordinate).xy;
}