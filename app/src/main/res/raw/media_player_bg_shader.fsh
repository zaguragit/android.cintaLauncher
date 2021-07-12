#version 300 es

precision mediump float;

uniform vec4 background_color;
uniform float aspectRatio;

layout(location = 0) uniform sampler2D albedo;

uniform vec2 resolution;

in vec2 textureCoord;
out vec4 fragColor;

vec4 render_stretched(in vec2 textureCoord) {
    ivec2 albedo_resolution = textureSize(albedo, 0);
    float m = float(albedo_resolution.x) / float(albedo_resolution.y);
    vec2 newUV = vec2(-((1.0 - textureCoord.x) / m * resolution.x / resolution.y), textureCoord.y);
    float stretch = -newUV.x + 0.2;
    return (stretch > 1.0) ? texture(albedo, vec2(newUV.x / stretch, newUV.y)) : texture(albedo, newUV);
}

void main() {
    const float uBlurSize = 64.0;

    ivec2 albedo_resolution = textureSize(albedo, 0);
    float m = float(albedo_resolution.x) / float(albedo_resolution.y);

    vec2 newUV = vec2(-((1.0 - textureCoord.x) / m * resolution.x / resolution.y), textureCoord.y);
    float stretch = -newUV.x + 0.2;

    vec4 color = vec4(0.0);

    float s = max(stretch, 1.0) - 1.0;
    int b = max(int(sqrt(uBlurSize * s) * sqrt(uBlurSize)), 1);

    float halfOffset = float(b) / 2.0;

    for (int x = 0; x < b; x++) {
        for (int y = 0; y < b; y++) {
            vec2 offset = vec2(float(x) - halfOffset, float(y) - halfOffset) / resolution;
            color += render_stretched(textureCoord + offset);
        }
    }

    color = color / float(b * b);

    fragColor = mix(color, background_color, clamp(-newUV.x * 0.9 - 0.1, 0.0, 0.9));
}