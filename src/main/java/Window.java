import Fonts.CFont;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11C.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11C.glClear;
import static org.lwjgl.opengl.GL15.*;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL31.GL_TEXTURE_BUFFER;
import static org.lwjgl.system.MemoryUtil.NULL;

public class Window {

    private long window;
    private CFont font;
    private int vao;

    private float[] vertices = {
            // x, y,        r, g, b              ux, uy
            0.5f, 0.5f,     1.0f, 0.2f, 0.11f,    1.0f, 0.0f,
            0.5f, -0.5f,    1.0f, 0.2f, 0.11f,    1.0f, 1.0f,
            -0.5f, -0.5f,   1.0f, 0.2f, 0.11f,    0.0f, 1.0f,
            -0.5f, 0.5f,    1.0f, 0.2f, 0.11f,    0.0f, 0.0f
    };

    private int[] indices = {
            0, 1, 3,
            1, 2, 3
    };

    public Window() {
        init();
        font = new CFont("C:/Windows/Fonts/Arial.ttf", 64);
    }

    private void init() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(1080, 1920, "Font Rendering", NULL, NULL);
        if (window == NULL) {
            System.out.println("Could not create window.");
            glfwTerminate();
            return;
        }

        glfwMakeContextCurrent(window);
        glfwSwapInterval(1);
        glfwShowWindow(window);

        // Initialize gl functions for windows using GLAD
        GL.createCapabilities();
    }

    private void uploadSquare() {
        vao = glGenVertexArrays();
        glBindVertexArray(vao);

        int vbo = glGenBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, vbo);
        glBufferData(GL_ARRAY_BUFFER, vertices, GL_STATIC_DRAW);

        int ebo = glGenBuffers();
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, ebo);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);

        int stride = 7 * Float.BYTES;
        glVertexAttribPointer(0, 2, GL_FLOAT, false, stride, 0);
        glEnableVertexAttribArray(0);

        glVertexAttribPointer(1, 3, GL_FLOAT, false, stride, 2 * Float.BYTES);
        glEnableVertexAttribArray(1);

        glVertexAttribPointer(2, 2, GL_FLOAT, false, stride, 5 * Float.BYTES);
        glEnableVertexAttribArray(2);
    }

    public void run() {
        uploadSquare();

        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);
            glClearColor(1, 1, 1, 1);

            glBindVertexArray(vao);

            glDrawElements(GL_TRIANGLES, 6, GL_UNSIGNED_INT, 0);

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
