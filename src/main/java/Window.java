import Fonts.CFont;
import org.joml.Vector2f;
import org.lwjgl.opengl.GL;

import java.util.Random;

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

    public Window() {
        init();
        font = new CFont("C:/Windows/Fonts/Arial.ttf", 64);
    }

    private void init() {
        glfwInit();
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 3);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);

        window = glfwCreateWindow(1920, 1080, "Font Rendering", NULL, NULL);
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

    public void run() {
        Sdf.generateCodepointBitmap(
                'A', "C:/Windows/Fonts/arial.ttf", 32);

        Shader fontShader = new Shader("assets/fontShader.glsl");
        Batch batch = new Batch();
        batch.shader = fontShader;
        batch.font = font;
        batch.initBatch();

        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Random random = new Random();
        while (!glfwWindowShouldClose(window)) {
            glClear(GL_COLOR_BUFFER_BIT);
            glClearColor(1, 1, 1, 1);

            batch.addText("Hello world!", 200, 200, 1f, 0xFF00AB0);
            batch.addText("My name is Gabe!", 100, 300, 1.1f, 0xAA01BB);

            String message = "";
            for (int i=0; i < 10; i++) {
                message += (char)(random.nextInt('z' - 'a') + 'a');
            }
            batch.addText(message, 200, 400, 1.1f, 0xAA01BB);

            batch.flushBatch();

            glfwSwapBuffers(window);
            glfwPollEvents();
        }
    }
}
