#version 150

in vec4 vertexColor;
in vec2 texCoord;

out vec4 fragColor;

void main() {
    // Discard pixels outside the cone
    if (length(texCoord) > 1) {
        discard;
    }

    fragColor = vec4(1, 0, 0, 0.5);
}
