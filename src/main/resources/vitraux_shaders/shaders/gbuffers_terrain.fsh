#version 120

uniform sampler2D gtexture;
uniform sampler2D shadowtex0;   // opaque-only depth
uniform sampler2D shadowtex1;   // all-geometry depth
uniform sampler2D shadowcolor0; // colour of translucent blocker

varying vec2 texcoord;
varying vec4 color;
varying vec4 shadowPos;

const float SHADOW_BIAS     = 0.0005;
const float TINT_STRENGTH   = 0.55;
const float SHADOW_DARKNESS = 0.40;

void main() {
    vec4 albedo = texture2D(gtexture, texcoord) * color;
    if (albedo.a < 0.1) discard;

    // Perspective divide then remap clip [-1,1] → texture [0,1]
    vec3 sc = shadowPos.xyz / shadowPos.w;
    sc = sc * 0.5 + 0.5;

    // Outside shadow frustum → no shadow applied
    if (sc.x < 0.0 || sc.x > 1.0 || sc.y < 0.0 || sc.y > 1.0) {
        gl_FragColor = vec4(albedo.rgb, albedo.a);
        return;
    }

    float refDepth = sc.z - SHADOW_BIAS;

    // Manual depth compare (avoids sampler2DShadow setup issues with Iris)
    float depth0 = texture2D(shadowtex0, sc.xy).r; // opaque blocker depth
    float depth1 = texture2D(shadowtex1, sc.xy).r; // closest blocker depth (incl. glass)

    bool fullShadow  = (refDepth > depth1);           // opaque block above
    bool coloredLight = (refDepth > depth0) && !fullShadow; // vitraux above, sky visible through

    vec3 lightColor = vec3(1.0);

    if (fullShadow) {
        lightColor = vec3(SHADOW_DARKNESS);
    } else if (coloredLight) {
        vec4 tint = texture2D(shadowcolor0, sc.xy);
        lightColor = mix(vec3(1.0), tint.rgb * 1.15, tint.a * TINT_STRENGTH);
    }

    gl_FragColor = vec4(albedo.rgb * lightColor, albedo.a);
}
