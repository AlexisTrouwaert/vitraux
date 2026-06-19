/*
 * shadow.vsh — Vitraux Shadows
 * Vertex shader for the shadow pass.
 * Passes texture coordinates and vertex color to the fragment shader.
 */
#version 120

varying vec2 texcoord;
varying vec4 color;

void main() {
    texcoord = (gl_TextureMatrix[0] * gl_MultiTexCoord0).xy;
    color    = gl_Color;
    gl_Position = ftransform();
}
