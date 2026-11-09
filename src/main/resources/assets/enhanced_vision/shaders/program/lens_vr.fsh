#version 150

uniform sampler2D DiffuseSampler;
uniform float Overscan;
uniform float ProjectionScaleX;
uniform float ProjectionScaleY;
uniform float ProjectionOffsetX;
uniform float ProjectionOffsetY;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    vec2 scale = vec2(ProjectionScaleX, ProjectionScaleY);
    vec2 offset = vec2(ProjectionOffsetX, ProjectionOffsetY);
    vec2 tangent = (p + offset) / scale;

    // Shared angular units for both eyes. Separate axes keep the vertical
    // mapping independent of horizontal stereo disparity in parallel views.
    vec2 squared = tangent * tangent;
    vec2 coverage = vec2(1.0) + (Overscan - 1.0) * squared / (vec2(1.0) + squared);
    vec2 sourceNdc = (p + offset) * coverage / Overscan - offset;
    vec2 sourceUv = (sourceNdc + 1.0) * 0.5;
    fragColor = vec4(texture(DiffuseSampler, sourceUv).rgb, 1.0);
}
