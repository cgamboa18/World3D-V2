package world3D;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferStrategy;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;
import java.util.ArrayList;
import javax.swing.JFrame;

import world3D.scene.Scene;
import world3D.screen.Camera;
import world3D.screen.Screen;
import world3D.visual3D.Texture;

public class World3DApp extends JFrame implements Runnable{
	
	private static final long serialVersionUID = 1L;
	public static final int WIDTH = 800;
	public static final int HEIGHT = 600;
	
	private Thread thread;
	private boolean running;
	private BufferedImage image;
	public int[] pixels;
	public float[] zBuffer;
	public ArrayList<Texture> textures;
	public Camera camera;
	public Screen screen;
	public static Scene world;
	
	public World3DApp() {
		thread = new Thread(this);
		image = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
		pixels = ((DataBufferInt)image.getRaster().getDataBuffer()).getData();
		zBuffer = new float[WIDTH * HEIGHT];
		textures = new ArrayList<Texture>();
		camera = new Camera(0, 0, 0, 1, 0, 0, 0, -0.66f);
		world = new Scene();
		screen = new Screen(world, textures, WIDTH, HEIGHT);
		addKeyListener(camera);
		setSize(WIDTH, HEIGHT);
		setResizable(false);
		setTitle("World 3D");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBackground(Color.black);
		setLocationRelativeTo(null);
		setAlwaysOnTop(true);
		setVisible(true);
		start();
	}
	
	private synchronized void start() {
		running = true;
		thread.start();
	}
	
	public synchronized void stop() {
		running = false;
		try {
			thread.join();
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void render() {
		BufferStrategy bs = getBufferStrategy();
		if(bs == null) {
			createBufferStrategy(3);
			return;
		}
		Graphics g = bs.getDrawGraphics();
		g.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
		bs.show();
	}
	
	public void run() {
		long lastTime = System.nanoTime();
		final double ns = 1000000000.0 / 15.0;//60 times per second
		double delta = 0;
		requestFocus();
		while(running) {
			long now = System.nanoTime();
			delta = delta + ((now-lastTime) / ns);
			lastTime = now;
			while (delta >= 1)//Make sure update is only happening 60 times a second
			{
				//handles all of the logic restricted time
				screen.update(camera, pixels, zBuffer);
				camera.update(world);
				System.out.println(15 / delta);
				delta--;
			}
			
			render();//displays to the screen unrestricted time
		}
	}
	
	@SuppressWarnings("unused")
	public static void main(String [] args) {
		// Main
		World3DApp app = new World3DApp();
	}
}