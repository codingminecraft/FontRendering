#type vertex
#version 330 core
layout(location=0) in vec2 aPos;
layout(location=1) in vec3 aColor;
layout(location=2) in vec2 aTexCoords;

out vec2 fTexCoords;
out vec3 fColor;

uniform mat4 uProjection;

void main()
{
    fTexCoords = aTexCoords;
    fColor = aColor;
    gl_Position = uProjection * vec4(aPos, -5, 1);
}

#type fragment
#version 330 core

in vec2 fTexCoords;
in vec3 fColor;

uniform sampler2D uFontTexture;

out vec4 color;

void main()
{
    float upperPointCutoff = 0.5;
    float midpointCutoff = 0.49;
    float c = texture(uFontTexture, fTexCoords).r;
    if (c > upperPointCutoff)
        color = vec4(1, 1, 1, 1) * vec4(fColor, 1);
    else if (c > midpointCutoff)
    {
        // [0.45, 5] -> [0, 1]
        float smoothC = smoothstep(midpointCutoff, upperPointCutoff, c);
        color = vec4(1, 1, 1, smoothC) * vec4(fColor, 1);
    }
    else
        color = vec4(0, 0, 0, 0);
}