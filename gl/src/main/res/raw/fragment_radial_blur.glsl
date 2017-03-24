
#extension GL_OES_EGL_image_external : require

precision mediump float;

varying vec2 textureCoordinate;
uniform samplerExternalOES s_texture;

const float sampleDist = 1.0;
const float sampleStrength = 2.2;

void main(){

   float samples[10];

   samples[0] = -0.08;
   samples[1] = -0.05;
   samples[2] = -0.03;
   samples[3] = -0.02;
   samples[4] = -0.01;
   samples[5] = 0.01;
   samples[6] = 0.02;
   samples[7] = 0.03;
   samples[8] = 0.05;
   samples[9] = 0.08;

    // 0.5,0.5 is the center of the screen
    // so substracting vTextureCoord from it will result in
    // a vector pointing to the middle of the screen
    vec2 dir = 0.5 - textureCoordinate;

    // calculate the distance to the center of the screen
    float dist = sqrt(dir.x*dir.x + dir.y*dir.y);

    // normalize the direction (reuse the distance)
    dir = dir/dist;

    // this is the original colour of this fragment
    // using only this would result in a nonblurred version
    vec4 color = texture2D(s_texture,textureCoordinate);

    vec4 sum = color;

    // take 10 additional blur samples in the direction towards
    // the center of the screen
    for (int i = 0; i < 10; i++)
    {
      sum += texture2D( s_texture, textureCoordinate + dir * samples[i] * sampleDist );
    }

    // we have taken eleven samples
    sum *= 1.0/11.0;

    // weighten the blur effect with the distance to the
    // center of the screen ( further out is blurred more)
    float t = dist * sampleStrength;
    t = clamp( t ,0.0,1.0); //0<= t <= 1

    //Blend the original color with the averaged pixels
    gl_FragColor = mix(color, sum, t);
}