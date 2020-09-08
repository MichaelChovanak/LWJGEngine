/**
 * @file PathTracer.java
 * @author Michael Chovanak
 * @date Aug 19, 2020
 *
 */

package engine.shaders;

import static org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BYTE;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL11.GL_NEAREST;
import static org.lwjgl.opengl.GL11.GL_RGBA;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MAG_FILTER;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_MIN_FILTER;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.opengl.GL11.glTexImage2D;
import static org.lwjgl.opengl.GL11.glTexParameteri;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_READ_WRITE;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_WRITE_ONLY;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glGenBuffers;
import static org.lwjgl.opengl.GL20.GL_COMPILE_STATUS;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_LINK_STATUS;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;
import static org.lwjgl.opengl.GL20.glAttachShader;
import static org.lwjgl.opengl.GL20.glBindAttribLocation;
import static org.lwjgl.opengl.GL20.glCompileShader;
import static org.lwjgl.opengl.GL20.glCreateProgram;
import static org.lwjgl.opengl.GL20.glCreateShader;
import static org.lwjgl.opengl.GL20.glDeleteProgram;
import static org.lwjgl.opengl.GL20.glDeleteShader;
import static org.lwjgl.opengl.GL20.glDetachShader;
import static org.lwjgl.opengl.GL20.glDisableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glGetProgramInfoLog;
import static org.lwjgl.opengl.GL20.glGetProgrami;
import static org.lwjgl.opengl.GL20.glGetProgramiv;
import static org.lwjgl.opengl.GL20.glGetShaderInfoLog;
import static org.lwjgl.opengl.GL20.glGetShaderi;
import static org.lwjgl.opengl.GL20.glGetUniformLocation;
import static org.lwjgl.opengl.GL20.glLinkProgram;
import static org.lwjgl.opengl.GL20.glShaderSource;
import static org.lwjgl.opengl.GL20.glUniform1i;
import static org.lwjgl.opengl.GL20.glUniform3f;
import static org.lwjgl.opengl.GL20.glUseProgram;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.GL_RGBA32F;
import static org.lwjgl.opengl.GL30.glBindFragDataLocation;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glGenVertexArrays;
import static org.lwjgl.opengl.GL42.GL_SHADER_IMAGE_ACCESS_BARRIER_BIT;
import static org.lwjgl.opengl.GL42.glBindImageTexture;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_SHADER;
import static org.lwjgl.opengl.GL43.GL_COMPUTE_WORK_GROUP_SIZE;
import static org.lwjgl.opengl.GL43.glDispatchCompute;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;

import engine.io.Camera;
import engine.math.Vector3f;
import main.Main;

public class RayMarcher{

	private static final String PATH_TRACER_FILE = "src/engine/shaders/RayTracer.glslcs";
	private static final String QUAD_VERTEX_FILE = "src/engine/shaders/quad.vs";
	private static final String QUAD_FRAGMENT_FILE = "src/engine/shaders/quad.fs";
	
	private int textureID;
	public int computeProgramID;
	public int quadProgramID;
	private int vaoID;
	
	private int computeShaderID;
	private int quadVShaderID;
	private int quadFShaderID;
	
	private int location_eye;
	private int location_ray00;
	private int location_ray01;
	private int location_ray10;
	private int location_ray11;

	private int workGroupSizeX;
	private int workGroupSizeY;
	
	private Vector3f eyeRay = new Vector3f();
	private Camera camera = Main.camera;
	
	public RayMarcher() {
		try {
			init();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * @param computeFile
	 */
	public void init() throws Exception {
		textureID = createFramebufferTexture(); //match
		vaoID = quadFullScreenVao(); //match
		computeProgramID = createComputeProgram(); //createShader() possibly differs
		initComputeProgram(); // match
		quadProgramID = createQuadProgram(); // match
		initQuadProgram();
	}
	
	private int createFramebufferTexture() {
		int texure = glGenTextures();
		glBindTexture(GL_TEXTURE_2D, texure);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
		glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
		ByteBuffer black = null;
		glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA32F, Main.WIDTH, Main.HEIGHT, 0, GL_RGBA, GL_FLOAT, black);
		glBindTexture(GL_TEXTURE_2D, 0);
		return texure;
	}
	
	private int quadFullScreenVao() {
		int vao = glGenVertexArrays(); // Vertex Array Object can have up to 16 attributes (VBO's) assigned to it.
		int vbo = glGenBuffers(); // Vertex Buffer Object is an array buffer stored on GPU memory. 
								  // The GPU can access and modify this data.
		glBindVertexArray(vao);
		glBindBuffer(GL_ARRAY_BUFFER, vbo);
		ByteBuffer bb = BufferUtils.createByteBuffer(2 * 6); // Generates byte buffer to store compute data.
		bb.put((byte) -1).put((byte) -1);
		bb.put((byte) 1).put((byte) -1);
		bb.put((byte) 1).put((byte) 1);
		bb.put((byte) 1).put((byte) 1);
		bb.put((byte) -1).put((byte) 1);
		bb.put((byte) -1).put((byte) -1);
		bb.flip();
		glBufferData(GL_ARRAY_BUFFER, bb, GL_STATIC_DRAW);
		glEnableVertexAttribArray(0); // Put the VBO in the VAO's attributes list at index 0.
		glVertexAttribPointer(0, 2, GL_BYTE, false, 0, 0L);
		glBindVertexArray(0); // Unbind the VAO.
		return vao;
	}
	
	/**
	 * Create the tracing compute shader program.
	 * 
	 * @return that program id
	 * @throws IOException
	 */
	private int createComputeProgram() throws IOException {
		computeProgramID = glCreateProgram();
		computeShaderID = createShader(PATH_TRACER_FILE, GL_COMPUTE_SHADER);
		glAttachShader(computeProgramID, computeShaderID);
		glLinkProgram(computeProgramID);
		int linked = glGetProgrami(computeProgramID, GL_LINK_STATUS);
		String programLog = glGetProgramInfoLog(computeProgramID);
		if (programLog.trim().length() > 0) {
			System.err.println(programLog);
		}
		if (linked == 0) {
			throw new AssertionError("Could not link program");
		}
		return computeProgramID;
	}
	
	
	/**
	 * Create a shader object from the given classpath resource.
	 * 
	 * @param resource
	 *            the class path
	 * @param type
	 *            the shader type
	 * @return the shader object id
	 * @throws IOException
	 */
	
	private int createShader(String fileName, int type) throws IOException {
		StringBuilder shaderSource = new StringBuilder();
		try {
			File file = new File(fileName);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			String line;
			while((line = reader.readLine())!=null){
				shaderSource.append(line).append("//\n");
			}
			reader.close();
		} catch(Exception e){
			e.printStackTrace();
			glfwSetWindowShouldClose(Main.window.getLong(), true);
		}
		int shaderID = glCreateShader(type);
		glShaderSource(shaderID, shaderSource);
		glCompileShader(shaderID);
		if(glGetShaderi(shaderID, GL_COMPILE_STATUS )== GL_FALSE){
			System.out.println(glGetShaderInfoLog(shaderID, 500)); // 500 is the max character count for the error info.
			System.err.println("Could not compile shader: " + fileName);
			glfwSetWindowShouldClose(Main.window.getLong(), true);
		}
		return shaderID;
	}
	
	/**
	 * Initialize the compute shader.
	 */
	private void initComputeProgram() {
		glUseProgram(computeProgramID);
		IntBuffer workGroupSize = BufferUtils.createIntBuffer(3);
		glGetProgramiv(computeProgramID, GL_COMPUTE_WORK_GROUP_SIZE, workGroupSize);
		workGroupSizeX = workGroupSize.get(0);
		workGroupSizeY = workGroupSize.get(1);
		location_eye = glGetUniformLocation(computeProgramID, "eye");
		location_ray00 = glGetUniformLocation(computeProgramID, "ray00");
		location_ray01 = glGetUniformLocation(computeProgramID, "ray01");
		location_ray10 = glGetUniformLocation(computeProgramID, "ray10");
		location_ray11 = glGetUniformLocation(computeProgramID, "ray11");
		glUseProgram(0);
	}
	
	
	/**
	 * Create the full-screen quad shader.
	 * 
	 * @return that program id
	 * @throws IOException
	 */
	private int createQuadProgram() throws IOException {
		quadProgramID = glCreateProgram();
		quadVShaderID = createShader(QUAD_VERTEX_FILE, GL_VERTEX_SHADER);
		quadFShaderID = createShader(QUAD_FRAGMENT_FILE, GL_FRAGMENT_SHADER);
		glAttachShader(quadProgramID, quadVShaderID);
		glAttachShader(quadProgramID, quadFShaderID);
		glBindAttribLocation(quadProgramID, 0, "vertex");
		glBindFragDataLocation(quadProgramID, 0, "color");
		glLinkProgram(quadProgramID);
		int linked = glGetProgrami(quadProgramID, GL_LINK_STATUS);
		String programLog = glGetProgramInfoLog(quadProgramID);
		if (programLog.trim().length() > 0) {
			System.err.println(programLog);
		}
		if (linked == 0) {
			throw new AssertionError("Could not link program");
		}
		return quadProgramID;
	}
	
	/**
	 * Initialize the full-screen-quad program.
	 */
	private void initQuadProgram() {
		glUseProgram(quadProgramID);
		int location_tex = glGetUniformLocation(quadProgramID, "tex");
		glUniform1i(location_tex, 0);
		glUseProgram(0);
	}
	
	/**
	 * Compute one frame by tracing the scene using our compute shader and
	 * presenting that image on the screen.
	 */
	public void trace() {
		glUseProgram(computeProgramID);
		/* Set viewing frustum corner rays in shader */
		loadRays();

		/* Bind level 0 of framebuffer texture as writable image in the shader. */
		glBindImageTexture(0, textureID, 0, false, 0, GL_WRITE_ONLY, GL_RGBA32F);

		/* Compute appropriate invocation dimension. */
		int worksizeX = 1024;
		int worksizeY = 1024;

		/* Invoke the compute shader. */
		glDispatchCompute(worksizeX / workGroupSizeX, worksizeY / workGroupSizeY, 1);

		/* Reset image binding. */
		glBindImageTexture(0, 0, 0, false, 0, GL_READ_WRITE, GL_RGBA32F);
		glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
		glUseProgram(0);

		/*
		 * Draw the rendered image on the screen using textured full-screen
		 * quad.
		 */
		glUseProgram(quadProgramID);
		glBindVertexArray(vaoID);
		glBindTexture(GL_TEXTURE_2D, textureID);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
		glUseProgram(0);
	}
	
	public void render() {
		/*
		 * Draw the rendered image on the screen using textured full-screen
		 * quad.
		 */
		glUseProgram(quadProgramID);
		glBindVertexArray(vaoID);
		glBindTexture(GL_TEXTURE_2D, textureID);
		glDrawArrays(GL_TRIANGLES, 0, 6);
		glBindTexture(GL_TEXTURE_2D, 0);
		glBindVertexArray(0);
		glUseProgram(0);
	}
	
	
	/*
	 protected void loadRays() {
		camera.refresh();
		glUniform3f(location_eye, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		camera.getEyeRay(-1, -1, eyeRay);
		glUniform3f(location_ray00, eyeRay.x(), eyeRay.y(), eyeRay.z());
		camera.getEyeRay(-1, 1, eyeRay);
		glUniform3f(location_ray01, eyeRay.x(), eyeRay.y(), eyeRay.z());
		camera.getEyeRay(1, -1, eyeRay);
		glUniform3f(location_ray10, eyeRay.x(), eyeRay.y(), eyeRay.z());
		camera.getEyeRay(1, 1, eyeRay);
		glUniform3f(location_ray11, eyeRay.x(), eyeRay.y(), eyeRay.z());
	}
	 */
	protected void loadRays() {
		camera.refresh();
		glUniform3f(location_eye, camera.getPosition().x, camera.getPosition().y, camera.getPosition().z);
		camera.getEyeRay(-1, -1, eyeRay);
		glUniform3f(location_ray00, eyeRay.x, eyeRay.y, eyeRay.z);
		camera.getEyeRay(-1, 1, eyeRay);
		glUniform3f(location_ray01, eyeRay.x, eyeRay.y, eyeRay.z);
		camera.getEyeRay(1, -1, eyeRay);
		glUniform3f(location_ray10, eyeRay.x, eyeRay.y, eyeRay.z);
		camera.getEyeRay(1, 1, eyeRay);
		glUniform3f(location_ray11, eyeRay.x, eyeRay.y, eyeRay.z);
	}
	
	
	public void cleanup() {
		glDetachShader(computeProgramID, computeShaderID);
		glDeleteShader(computeShaderID);
		glDeleteProgram(computeProgramID);
		
		glDetachShader(quadProgramID, quadVShaderID);
		glDeleteShader(quadVShaderID);
		glDetachShader(quadProgramID, quadFShaderID);
		glDeleteShader(quadFShaderID);
		glDeleteProgram(quadProgramID);
		
		// Disable the VBO index from the VAO attributes list
		glDisableVertexAttribArray(0);
		// Delete the VBO
		glBindBuffer(GL_ARRAY_BUFFER, 0);
		// Delete the VAO
		glBindVertexArray(0);
	}

}
