import Fonts.CFont;
import Fonts.CharInfo;
import org.joml.Matrix4f;
import org.lwjgl.opengl.GL15;

import static org.lwjgl.opengl.GL11.GL_STACK_OVERFLOW;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL15C.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15C.glGenBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;

public class Batch {
//    private float[] vertices = {
//            // x, y,        r, g, b              ux, uy
//            0.5f, 0.5f,     1.0f, 0.2f, 0.11f,    1.0f, 0.0f,
//            0.5f, -0.5f,    1.0f, 0.2f, 0.11f,    1.0f, 1.0f,
//            -0.5f, -0.5f,   1.0f, 0.2f, 0.11f,    0.0f, 1.0f,
//            -0.5f, 0.5f,    1.0f, 0.2f, 0.11f,    0.0f, 0.0f
//    };

    private int[] indices = {
            0, 1, 3,
            1, 2, 3
    };

    // 25 quads
    public static int BATCH_SIZE = 100;
    public static int VERTEX_SIZE = 7;
    public float[] vertices = new float[BATCH_SIZE * VERTEX_SIZE];
    public int size = 0;
    private Matrix4f projection = new Matrix4f();

    public int vao;
    public int vbo;
    public Shader shader;
    public Shader sdfShader;
    public CFont font;

    public void generateEbo() {
        int elementSize = BATCH_SIZE * 3;
        int[] elementBuffer = new int[elementSize];

        for (int i=0; i < elementSize; i++) {
            elementBuffer[i] = indices[(i % 6)] + ((i / 6) * 4);
        }

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL15.GL_ELEMENT_ARRAY_BUFFER, elementBuffer, GL_STATIC_DRAW);
    }

    public void initBatch() {
        projection.identity();
        projection.ortho(0, 800, 0, 600, 1f, 100f);

        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * VERTEX_SIZE * BATCH_SIZE, GL_DYNAMIC_DRAW);

        generateEbo();

        int stride = 7 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    public void flushBatch() {
        // Clear the buffer on the GPU, and then upload the CPU contents, and then draw
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, Float.BYTES * VERTEX_SIZE * BATCH_SIZE, GL_DYNAMIC_DRAW);
        glBufferSubData(GL_ARRAY_BUFFER, 0, vertices);

        // Draw the buffer that we just uploaded
        shader.use();
        //sdfShader.use();
        glActiveTexture(GL_TEXTURE0);
        glBindTexture(GL_TEXTURE_BUFFER, font.textureId);
        //glBindTexture(GL_TEXTURE_BUFFER, Sdf.textureId);
        sdfShader.uploadTexture("uFontTexture", 0);
        sdfShader.uploadMat4f("uProjection", projection);

        glBindVertexArray(vao);

        glDrawElements(GL_TRIANGLES, size * 6, GL_UNSIGNED_INT, 0);

        // Reset batch for use on next draw call
        size = 0;
    }

    public void addCharacter(float x, float y, float scale, CharInfo charInfo, int rgb) {
        // If we have no more room in the current batch, flush it and start with a fresh batch
        if (size >= BATCH_SIZE - 4) {
            flushBatch();
        }

        float r = (float)((rgb >> 16) & 0xFF) / 255.0f;
        float g = (float)((rgb >> 8) & 0xFF) / 255.0f;
        float b = (float)((rgb >> 0) & 0xFF) / 255.0f;

        float x0 = x;
        float y0 = y;
        float x1 = x + scale * charInfo.width;
        float y1 = y + scale * charInfo.height;

        float ux0 = charInfo.textureCoordinates[0].x; float uy0 = charInfo.textureCoordinates[0].y;
        float ux1 = charInfo.textureCoordinates[1].x; float uy1 = charInfo.textureCoordinates[1].y;

        int index = size * 7;
        vertices[index] = x1;      vertices[index + 1] = y0;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux1; vertices[index + 6] = uy0;

        index += 7;
        vertices[index] = x1;      vertices[index + 1] = y1;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux1; vertices[index + 6] = uy1;

        index += 7;
        vertices[index] = x0;      vertices[index + 1] = y1;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux0; vertices[index + 6] = uy1;

        index += 7;
        vertices[index] = x0;      vertices[index + 1] = y0;
        vertices[index + 2] = r;   vertices[index + 3] = g;  vertices[index + 4] = b;
        vertices[index + 5] = ux0; vertices[index + 6] = uy0;

        size += 4;
    }

    public void addText(String text, int x, int y, float scale, int rgb) {
        for (int i=0; i < text.length(); i++) {
            char c = text.charAt(i);

            CharInfo charInfo = font.getCharacter(c);
            if (charInfo.width == 0) {
                System.out.println("Unknown character " + c);
                continue;
            }

            float xPos = x;
            float yPos = y;
            addCharacter(xPos, yPos, scale, charInfo, rgb);
            x += charInfo.width * scale;
        }
    }
}
