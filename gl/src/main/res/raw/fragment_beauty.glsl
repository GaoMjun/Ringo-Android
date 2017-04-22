
#extension GL_OES_EGL_image_external : require

precision mediump float;

varying mediump vec2 textureCoordinate;

uniform samplerExternalOES s_texture;

uniform mediump vec2 singleStepOffset;

uniform int beautifiy;

const mediump float params = 0.33;

const mediump vec3 W = vec3(0.299, 0.587, 0.114);

const mediump mat3 rgb2yuv = mat3(0.299,-0.147,0.615,0.587,-0.289,-0.515,0.114,0.436,-0.1);

vec2 blurCoordinates[20];

float hardLight(float color)
{
	if(color <= 0.5)
		color = color * color * 2.0;
	else
		color = 1.0 - ((1.0 - color)*(1.0 - color) * 2.0);
	return color;
}

void main() {

    vec3 centralColor = texture2D(s_texture, textureCoordinate).rgb;

    vec3 yuv = rgb2yuv * centralColor;
    if(yuv.g<-0.225 || yuv.g>0.0 || yuv.b<0.022 || yuv.b>0.206) {
        gl_FragColor = vec4(centralColor.rgb, 1.0);
        return;
    }

//    if (beautifiy == 0) {
//        gl_FragColor = vec4(centralColor.rgb, 1.0);
//        return;
//    }

    blurCoordinates[0] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -10.0);
    blurCoordinates[1] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 10.0);
    blurCoordinates[2] = textureCoordinate.xy + singleStepOffset * vec2(-10.0, 0.0);
    blurCoordinates[3] = textureCoordinate.xy + singleStepOffset * vec2(10.0, 0.0);
    blurCoordinates[4] = textureCoordinate.xy + singleStepOffset * vec2(5.0, -8.0);
    blurCoordinates[5] = textureCoordinate.xy + singleStepOffset * vec2(5.0, 8.0);
    blurCoordinates[6] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, 8.0);
    blurCoordinates[7] = textureCoordinate.xy + singleStepOffset * vec2(-5.0, -8.0);
    blurCoordinates[8] = textureCoordinate.xy + singleStepOffset * vec2(8.0, -5.0);
    blurCoordinates[9] = textureCoordinate.xy + singleStepOffset * vec2(8.0, 5.0);
    blurCoordinates[10] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, 5.0);
    blurCoordinates[11] = textureCoordinate.xy + singleStepOffset * vec2(-8.0, -5.0);
    blurCoordinates[12] = textureCoordinate.xy + singleStepOffset * vec2(0.0, -6.0);
    blurCoordinates[13] = textureCoordinate.xy + singleStepOffset * vec2(0.0, 6.0);
    blurCoordinates[14] = textureCoordinate.xy + singleStepOffset * vec2(6.0, 0.0);
    blurCoordinates[15] = textureCoordinate.xy + singleStepOffset * vec2(-6.0, 0.0);
    blurCoordinates[16] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, -4.0);
    blurCoordinates[17] = textureCoordinate.xy + singleStepOffset * vec2(-4.0, 4.0);
    blurCoordinates[18] = textureCoordinate.xy + singleStepOffset * vec2(4.0, -4.0);
    blurCoordinates[19] = textureCoordinate.xy + singleStepOffset * vec2(4.0, 4.0);

    float sampleColor = centralColor.g * 20.0;
    sampleColor += texture2D(s_texture, blurCoordinates[0]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[1]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[2]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[3]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[4]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[5]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[6]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[7]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[8]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[9]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[10]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[11]).g;
    sampleColor += texture2D(s_texture, blurCoordinates[12]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[13]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[14]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[15]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[16]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[17]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[18]).g * 2.0;
    sampleColor += texture2D(s_texture, blurCoordinates[19]).g * 2.0;

    sampleColor = sampleColor / 48.0;

    float highPass = centralColor.g - sampleColor + 0.5;

    for(int i = 0; i < 5; i++)
    {
        highPass = hardLight(highPass);
    }
    float luminance = dot(centralColor, W);

    float alpha = pow(luminance, params);

    vec3 smoothColor = centralColor + (centralColor-vec3(highPass))*alpha*0.1;

    gl_FragColor = vec4(mix(smoothColor.rgb, max(smoothColor, centralColor), alpha), 1.0);

}