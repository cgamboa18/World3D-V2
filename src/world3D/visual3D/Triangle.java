package world3D.visual3D;

public class Triangle {
	public float[][] vertices;
	
	public Triangle() {
		vertices = new float[3][3];
		
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				vertices[i][j] = 0;
			}
		}
	}
	
	public Triangle(float[] v1, float[] v2, float[] v3) {
		vertices = new float[3][3];
		vertices[0] = v1;
		vertices[1] = v2;
		vertices[2] = v3;
	}
	
	public Triangle(float[][] vertices) {
		this.vertices = vertices;
	}
	
	public float[][] getVertices() {
		return vertices;
	}
}
