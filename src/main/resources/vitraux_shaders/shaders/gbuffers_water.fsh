/*
 * gbuffers_water.fsh — Vitraux Shadows
 * Fragment shader for translucent geometry (vitraux panes, stained glass, water).
 *
 * The vitraux themselves are rendered here.  We just output their colour and
 * alpha as-is, letting the GL blending stack handle layering.
 * The coloured light projection is applied in gbuffers_terrain.fsh on the
 * blocks *below* the vitraux.
 */
#version 120

uniform sampler2D gtexture;
uniform sampler2D lightmap;

varying vec2 texcoord;
varying vec4 color;

void main() {
    vec4 albedo = texture2D(gtexture, texcoord) * color;
    if (albedo.a < 0.02) discard;

    gl_FragColor = albedo;
}
