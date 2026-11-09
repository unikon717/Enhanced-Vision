#version 150

uniform sampler2D DiffuseSampler;
uniform vec2 InSize;
uniform float Overscan;

in vec2 texCoord;
out vec4 fragColor;

void main() {
    vec2 p = texCoord * 2.0 - 1.0;
    // Measure radius in actual pixels, including on ultrawide or portrait displays.
    vec2 metric = p * InSize / min(InSize.x, InSize.y);
    // Spread the change smoothly across the whole view, with no transition ring.
    float radiusSquared = dot(metric, metric);
    float easing = radiusSquared / (1.0 + radiusSquared);
    float coverage = mix(1.0, Overscan, easing);
    // The source world was rendered with its projection tangent multiplied by Overscan.
    // Preserve the center-point scale and gradually retain more peripheral coverage.
    vec2 sourceUv = (p * coverage / Overscan + 1.0) * 0.5;
    fragColor = vec4(texture(DiffuseSampler, sourceUv).rgb, 1.0);
}
