/**
 * @file Main.java
 * @author Michael Chovanak
 * @date Aug 18, 2020
 * 
 */

package main;


import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_PRESS;
import static org.lwjgl.glfw.GLFW.GLFW_RELEASE; 
import static org.lwjgl.glfw.GLFW.glfwGetCursorPos; 
import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback;
import static org.lwjgl.glfw.GLFW.glfwSetKeyCallback;
import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL.createCapabilities;
import static org.lwjgl.opengl.GL11.glViewport;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL30.glBindVertexArray;

import java.nio.DoubleBuffer;

import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;

import engine.io.Camera;
import engine.io.Window;
import engine.math.Vector3f;
import engine.shaders.RayMarcher;

public class Main implements Runnable {
	public static Thread game;
	public static Window window;
	public static Camera camera;
	
	private static RayMarcher rayMarcher;
	private static GLFWErrorCallback errFun;
	private static GLFWKeyCallback keyFun;
	private static GLFWCursorPosCallback cursFun;
	
	
	public static int WIDTH = 1024;
	public static int HEIGHT = 768;
	private static float targetFPS = 60;
	private static float targetUPS = 60;
	private static boolean DISPLAY_RENDER_TIME = true;
	
	
	public void start() {
		game = new Thread(this, "game");
		game.start();
	}

	public void run() {
		init();
		loop();
		
		cleanup();
		
	}
	
	private void init() {
		try {
			System.out.println("Initializing Game!");
			// Sets an error callback. The default implementation will print the error message in System.err.
			errFun = GLFWErrorCallback.createPrint(System.err).set();
			window = new Window(WIDTH, HEIGHT, "LwjgEngine3");
			setCallbacks(); // initializes Key and Mouse callbacks
			
			// This line is critical for LWJGL's interoperation with GLFW's
			// OpenGL context, or any context that is managed externally.
			// LWJGL detects the context that is current in the current thread,
			// creates the GLCapabilities instance and makes the OpenGL
			// bindings available for use.
			createCapabilities();
						
			/* Setup camera */
			camera = new Camera();
			camera.setFrustumPerspective(90.0f, (float) WIDTH / HEIGHT, 0.2f, 0.7f);
			camera.setLookAt(new Vector3f(3.0f, 2.0f, 7.0f), new Vector3f(0.0f, 0.5f, 0.0f), new Vector3f(0.0f, 1.0f, 0.0f));
			
			// Initialize Ray Marching Shader
			rayMarcher = new RayMarcher();
			
			
			
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	
	private boolean w = false, a = false, s = false, d = false, 
					space = false, leftShift = false;
	private double lastX = 0, lastY = 0, deltaX = 0, deltaY = 0;
	/**
	 * Set up a key callback. invoke() will be called each time a key is pressed, repeated, or released.
	 */
	public void setCallbacks() {
		System.out.println(1);
		if (glfwRawMouseMotionSupported())
		    glfwSetInputMode(window.getLong(), GLFW_RAW_MOUSE_MOTION, GLFW_TRUE);
		System.out.println(2);
		glfwSetInputMode(window.getLong(), GLFW.GLFW_CURSOR, GLFW_CURSOR_DISABLED); // GLFW_CURSOR_NORMAL, GLFW_CURSOR_HIDDEN
		System.out.println(3);
		glfwSetCursorPosCallback(window.getLong(), cursFun = new GLFWCursorPosCallback() {
			public void invoke(long window, double xpos, double ypos) {
				deltaX = xpos - lastX;
				deltaY = ypos - lastY;
				if(deltaX > 40)
					deltaX = 0;
				if(deltaY > 40)
					deltaY = 0;
				lastX = xpos;
				lastY = ypos;
			}
		});
		System.out.println(5);
		
		glfwSetKeyCallback(window.getLong(), keyFun = new GLFWKeyCallback() {
			public void invoke(long window, int key, int scancode, int action, int mods) {
				if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE)
					glfwSetWindowShouldClose(window, true);
				
				// WASD Player Movement
				if(key == GLFW_KEY_W && action == GLFW_PRESS) {
					w = true;
				}
				if(key == GLFW_KEY_W && action == GLFW_RELEASE) {
					w = false;
				}
				if(key == GLFW_KEY_S && action == GLFW_PRESS) {
					s = true;
				}
				if(key == GLFW_KEY_S && action == GLFW_RELEASE) {
					s = false;
				}
				if(key == GLFW_KEY_A && action == GLFW_PRESS) {
					a = true;
				}
				if(key == GLFW_KEY_A && action == GLFW_RELEASE) {
					a = false;
				}
				if(key == GLFW_KEY_D && action == GLFW_PRESS) {
					d = true;
				}
				if(key == GLFW_KEY_D && action == GLFW_RELEASE) {
					d = false;
				}
				
				// SPACE to ascend, 
				if(key == GLFW_KEY_SPACE && action == GLFW_PRESS) {
					space = true;
				}
				if(key == GLFW_KEY_SPACE && action == GLFW_RELEASE) {
					space = false;
				}
				if(key == GLFW_KEY_LEFT_SHIFT && action == GLFW_PRESS) {
					leftShift = true;
				}
				if(key == GLFW_KEY_LEFT_SHIFT && action == GLFW_RELEASE) {
					leftShift = false;
				}
				
				
			}
		});
	}
	
	
	private double delta;
	
	private void loop() {
		long initialTime = System.nanoTime();
		final double nanosec = 1000000000;
		final double timeU = nanosec / targetUPS;
		final double timeF = nanosec / targetFPS;
		double deltaU = 0, deltaF = 0;
		int frames = 0, ticks = 0;
		long timer = System.currentTimeMillis();
		
		    while (!glfwWindowShouldClose(window.getLong())) {

		        long currentTime = System.nanoTime();
		        delta = (currentTime - initialTime) / nanosec;
		        deltaU += (currentTime - initialTime) / timeU;
		        deltaF += (currentTime - initialTime) / timeF;
		        initialTime = currentTime;

		        
		        if (deltaU >= 1) {
		            inputs();
		            update();
		            ticks++;
		            deltaU--;
		        }

		        if (deltaF >= 1) {
		            render();
		            frames++;
		            deltaF--;
		        }

		        if (System.currentTimeMillis() - timer > 1000) {
		            if (DISPLAY_RENDER_TIME) {
		                System.out.println(String.format("UPS: %s, FPS: %s", ticks, frames));
		            }
		            frames = 0;
		            ticks = 0;
		            timer += 1000;
		        }
		    }
	}
	
	private void update() {
		glfwPollEvents();
		glViewport(0, 0, WIDTH, HEIGHT);
		rayMarcher.trace();
		glfwSwapBuffers(window.getLong());
		//window.update();
		//timer.update();
		
	}
	
	private void render() {
		//System.out.println("Rendering!");
		rayMarcher.render();
	}
	
	private Vector3f forward = new Vector3f(), right = new Vector3f(), up = new Vector3f();
	private float speed = 10f;
	private float sensitivity = 40f;
	private float pitch = 0; // Up/Down angle
	private void inputs() {
		forward.set(camera.getDirection());
		forward.mult((float)(speed*delta));
		right.set(camera.getRight());
		right.mult((float)(speed*delta));
		up.set(camera.getUp());
		up.mult((float)(speed*delta));
		if(w && !s) {
			camera.setPosition(camera.getPosition().add(forward));
		}
		if(s && !w) {
			camera.setPosition(camera.getPosition().sub(forward));
		}
		if(a && !d) {
			camera.setPosition(camera.getPosition().sub(right));
		}
		if(d && !a) {
			camera.setPosition(camera.getPosition().add(right));
		}
		if(space && !leftShift) {
			camera.setPosition(camera.getPosition().add(up));
		}
		if(leftShift && !space) {
			camera.setPosition(camera.getPosition().sub(up));
		}
		
		if(pitch < -50 && deltaY >= 0) {
			//pitch = -90;
		} else if (pitch > 50 && deltaY <= 0) {
			//pitch = 90;
		} else {
			camera.rotate((float)(sensitivity * -deltaY * delta), camera.getRight());
			pitch += (sensitivity * -deltaY * delta);
		}
		//System.out.println(pitch + " " + deltaY);

		//camera.refresh();
		camera.rotate((float)(sensitivity * -deltaX * delta), camera.getPermUp());
		deltaX = 0;
		deltaY = 0;
		
	}
	
	private void cleanup() {
		try {
		errFun.free();
		keyFun.free();
		window.cleanup();
		rayMarcher.cleanup();

		// Disable the VBO index from the VAO attributes list
		glDisableVertexAttribArray(0);
		 
		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		 
		// Delete the VAO
		glBindVertexArray(0);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Main().start();
	}
	
	
}
