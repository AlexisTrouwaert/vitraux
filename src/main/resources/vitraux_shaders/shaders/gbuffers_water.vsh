#version 120

uniform mat4 shadowProjection;
uniform mat4 shadowModelView;
uniform mat4 gbufferModelViewInverse;

varying vec2 texcoord;
varying vec4 color;
varying vec4 shadowPos;

void main() {
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    color    = gl_Color;

    vec4 eyePos   = gl_ModelViewMatrix * gl_Vertex;
    vec4 worldPos = gbufferModelViewInverse * eyePos;
    shadowPos     = shadowProjection * (shadowModelView * worldPos);

    gl_Position = ftransform();
}
