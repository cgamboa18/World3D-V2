package world3D.visual3D;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OBJParser {
	public static Triangle[] parseOBJ(String filePath) throws IOException {
		List<float[]> vertices = new ArrayList<>();
		List<Triangle> triangles = new ArrayList<>();

		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
			String line;
			while ((line = reader.readLine()) != null) {
				String[] parts = line.trim().split("\\s+");
				if (parts.length == 0)
					continue;
				switch (parts[0]) {
				case "v":
					// Vertex definition
					float[] vertex = new float[3];
					vertex[0] = Float.parseFloat(parts[1]);
					vertex[1] = Float.parseFloat(parts[2]);
					vertex[2] = Float.parseFloat(parts[3]);
					vertices.add(vertex);
					break;
				case "f":
					// Face definition
					int[] vertexIndices = new int[3];
					for (int i = 0; i < 3; i++) {
						String[] vertexParts = parts[i + 1].split("/");
						vertexIndices[i] = Integer.parseInt(vertexParts[0]) - 1; // OBJ indices are 1-based
					}
					float[] v1 = vertices.get(vertexIndices[0]);
					float[] v2 = vertices.get(vertexIndices[1]);
					float[] v3 = vertices.get(vertexIndices[2]);
					triangles.add(new Triangle(v1, v2, v3));
					break;
				}
			}
		}

		return triangles.toArray(new Triangle[0]);
	}
}
