/*
 * shadow.fsh — Vitraux Shadows
 * Fragment shader for the shadow pass.
 *
 * Iris/OptiFine maintains two shadow depth buffers automatically:
 *   shadowtex0  — opaque geometry only  (glass/translucent NOT included)
 *   shadowtex1  — ALL geometry           (glass/translucent included)
 *
 * This shader writes the colour of any semi-transparent fragment to
 * shadowcolor0.  Opaque fragments write black with alpha=0 (no tint).
 * Fully transparent fragments are discarded.
 *
 * The terrain/water shaders then combine shadowtex0, shadowtex1 and
 * shadowcolor0 to produce coloured light projected through vitraux.
 */
#version 120

uniform sampler2D gtexture;

varying vec2 texcoord;
varying vec4 color;

/* RENDERTARGETS: 0 */

void main() {
    vec4 albedo = texture2D(gtexture, texcoord) * color;

    // Fully transparent → no shadow at all
    if (albedo.a < 0.05) discard;

    if (albedo.a < 0.95) {
        // Semi-transparent (vitraux, stained glass) → store tint colour
        // alpha encodes how strongly the colour tints the light below
        gl_FragData[0] = vec4(albedo.rgb, albedo.a);
    } else {
        // Opaque → no colour tint (plain dark shadow below)
        gl_FragData[0] = vec4(0.0, 0.0, 0.0, 0.0);
    }
}
