package world3D.screen;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.Arrays;

import world3D.scene.Scene;
import world3D.visual3D.Texture;
import world3D.visual3D.Triangle;

public class Screen {
	public Scene world;
	public int width, height;
	public ArrayList<Texture> textures;

	public Screen(Scene world, ArrayList<Texture> textures, int width, int height) {
		this.world = world;
		this.textures = textures;
		this.width = width;
		this.height = height;
	}

	// Updates the pixels on the screen
	public int[] update(Camera camera, int[] pixels, float[] zBuffer) {
		// Reset background
		clearbg(pixels, zBuffer);

		// Create entity pixel layer
		renderTriangles(pixels, zBuffer, camera, world.getVisibleTriangles(camera));

		return pixels;
	}

	// Get and convert entity models into triangles, draw onto new layer of pixels
	private void renderTriangles(int[] pixels, float[] zBuffer, Camera camera, Triangle[] triangles) {
		int numCores = Runtime.getRuntime().availableProcessors();
		ExecutorService executor = Executors.newFixedThreadPool(numCores);

		for (int triangleIndex = 0; triangleIndex < triangles.length; triangleIndex++) {
			final int index = triangleIndex;
			//rasterizeTriangle(pixels, zBuffer, triangles[index], camera); // Mess
			
			executor.submit(() -> rasterizeTriangle(pixels, zBuffer, triangles[index], camera));
		}

		executor.shutdown();
		try {
			if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
				executor.shutdownNow();
			}
		} catch (InterruptedException e) {
			executor.shutdownNow();
			Thread.currentThread().interrupt();
		}
	}

	private void rasterizeTriangle(int[] pixels, float[] zBuffer, Triangle triangle, Camera camera) {
		ArrayList<Triangle> clippedTranslatedTriangle = clipTriangle(triangle.getVertices(), camera);
		for (int triangleDivisions = 0; triangleDivisions < clippedTranslatedTriangle.size(); triangleDivisions++) {
			float[][] triangleVertices3D = clippedTranslatedTriangle.get(triangleDivisions).getVertices();
			int[][] triangleVertices2D = getVertices2D(triangleVertices3D, camera);

			for (int i = 0; i < 3; i++) {
				for (int j = i + 1; j < 3; j++) {
					if (triangleVertices2D[i][1] > triangleVertices2D[j][1]) {
						int[] temp2D = triangleVertices2D[i];
						triangleVertices2D[i] = triangleVertices2D[j];
						triangleVertices2D[j] = temp2D;
						float[] temp3D = triangleVertices3D[i];
						triangleVertices3D[i] = triangleVertices3D[j];
						triangleVertices3D[j] = temp3D;
					}
				}
			}
			int x0 = triangleVertices2D[0][0];
			int y0 = triangleVertices2D[0][1];
			int x1 = triangleVertices2D[1][0];
			int y1 = triangleVertices2D[1][1];
			int x2 = triangleVertices2D[2][0];
			int y2 = triangleVertices2D[2][1];
			// Calculate slopes and handle potential division by zero
			float slope1 = (y1 != y0) ? (float) (x1 - x0) / (y1 - y0) : 0;
			float slope2 = (y2 != y0) ? (float) (x2 - x0) / (y2 - y0) : 0;
			float slope3 = (y2 != y1) ? (float) (x2 - x1) / (y2 - y1) : 0;

			float curx1 = x0;
			float curx2 = x0;

			int yTop = y0;
			int yBottom = y2;

			if (yTop < 0)
				yTop = 0;
			if (yBottom >= height)
				yBottom = height - 1;
			if (yBottom < 0 || yTop >= height)
				continue;

			for (int yScan = yTop; yScan <= yBottom; yScan++) {

				if (yScan < triangleVertices2D[1][1]) {
					curx1 = x0 + slope1 * (yScan - y0);
					curx2 = x0 + slope2 * (yScan - y0);
				} else {
					curx1 = x1 + slope3 * (yScan - y1);
					curx2 = x0 + slope2 * (yScan - y0);
				}

				int xLeft = (int) Math.min(curx1, curx2);
				int xRight = (int) Math.max(curx1, curx2);

				if (xLeft < 0)
					xLeft = 0;
				if (xRight >= width)
					xRight = width - 1;
				if (xRight < 0 || xLeft >= width)
					continue;
				// Fill the pixels between xLeft and xRight on the current scanline
				for (int x = xLeft; x < xRight; x++) {
					int[] point2D = { x, yScan };
					float[] point3D = getSurfacePoint3D(triangleVertices2D, point2D, triangleVertices3D);
					float pixelDistance = point3D[2]; // depth value of the point
					if (zBuffer[x + yScan * width] == -1 || zBuffer[x + yScan * width] > pixelDistance) {
						pixels[x + yScan * width] = distanceToHexColor(pixelDistance);
						zBuffer[x + yScan * width] = pixelDistance;
					}
				}
			}
		}
	}

	// Clip the triangle against the near plane z = 0 and return the resulting list
	// of triangles
	private ArrayList<Triangle> clipTriangle(float[][] vertices3D, Camera camera) {
		float[] cameraPosition = {
				camera.positionAttributes.xPos,
				camera.positionAttributes.yPos,
				camera.positionAttributes.zPos };
		float[] cameraOrientation = {
				camera.positionAttributes.xDir,
				camera.positionAttributes.yDir,
				camera.positionAttributes.zDir };
		float[][] rotatedVertices3D = new float[3][3];
		for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
			rotatedVertices3D[vertexIndex] = convertToCameraSpace(vertices3D[vertexIndex], cameraPosition, cameraOrientation);
		}
		ArrayList<Triangle> clippedTriangles = new ArrayList<>();
		ArrayList<float[]> insideVertices = new ArrayList<>();
		ArrayList<float[]> outsideVertices = new ArrayList<>();
		// Separate vertices into inside and outside based on the near plane (z = 0.01)
		for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
			if (rotatedVertices3D[vertexIndex][2] >= 0.01) {
				insideVertices.add(rotatedVertices3D[vertexIndex]);
			} else {
				outsideVertices.add(rotatedVertices3D[vertexIndex]);
			}
		}
		// All vertices are inside, no clipping needed
		if (outsideVertices.isEmpty()) {
			clippedTriangles.add(new Triangle(rotatedVertices3D));
			return clippedTriangles;
		}
		// All vertices are outside, triangle is completely clipped
		if (insideVertices.isEmpty()) {
			return clippedTriangles;
		}
		// Handle cases where some vertices are inside and some are outside
		ArrayList<float[]> newVertices = new ArrayList<>();
		for (int i = 0; i < rotatedVertices3D.length; i++) {
			float[] currentVertex = rotatedVertices3D[i];
			float[] nextVertex = rotatedVertices3D[(i + 1) % rotatedVertices3D.length];

			if (currentVertex[2] >= 0.01) {
				newVertices.add(currentVertex);
			}

			if ((currentVertex[2] >= 0.01 && nextVertex[2] < 0.01) || (currentVertex[2] < 0.01 && nextVertex[2] >= 0.01)) {
				float t = (0.01f - currentVertex[2]) / (nextVertex[2] - currentVertex[2]);
				float[] intersection = {
						currentVertex[0] + t * (nextVertex[0] - currentVertex[0]),
						currentVertex[1] + t * (nextVertex[1] - currentVertex[1]),
						0.01f
				};
				newVertices.add(intersection);
			}
		}
		// Create new triangles from the resulting vertices
		if (newVertices.size() >= 3) {
			for (int i = 1; i < newVertices.size() - 1; i++) {
				float[][] newTriangleVertices = {
						newVertices.get(0),
						newVertices.get(i),
						newVertices.get(i + 1)
				};
				clippedTriangles.add(new Triangle(newTriangleVertices));
			}
		}

		return clippedTriangles;
	}

	// Gets the 2D vertices of a given triangle and perspective
	private int[][] getVertices2D(float[][] triangleVertices3D, Camera camera) {
		int[][] triangleVertices2D = new int[3][2];
		for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
			float[] point2D = convertDimension(triangleVertices3D[vertexIndex]);
			if (point2D != null) {
				int xPixel = (int) point2D[0] + width / 2;
				int yPixel = (int) point2D[1] + height / 2;
				triangleVertices2D[vertexIndex][0] = xPixel;
				triangleVertices2D[vertexIndex][1] = yPixel;
			} else {
				return null;
			}
		}

		return triangleVertices2D;
	}

	// Converts a translated and rotated 3D point in space into a 2D point on the
	// screen
	private float[] convertDimension(float[] point3D) {
		// Perspective projection onto a 2D plane
		float focalLength = (float) (height / (2 * Math.tan(Math.toRadians(30))));
		float[] point2D = new float[2];
		point2D[0] = focalLength * point3D[0] / point3D[2];
		point2D[1] = focalLength * point3D[1] / point3D[2];

		return point2D;
	}

	private float[] convertToCameraSpace(float[] point3D, float[] cameraPoint, float[] cameraOrientation) {
		// Translate the 3D point to the camera's coordinate system
		float[] translatedPoint = new float[3];
		translatedPoint[0] = point3D[0] - cameraPoint[0];
		translatedPoint[1] = point3D[1] - cameraPoint[1];
		translatedPoint[2] = point3D[2] - cameraPoint[2];
		// Define the world up vector
		float[] worldUp = { 0, 1, 0 };
		// Compute the camera's right vector as the cross product of worldUp and
		// cameraOrientation
		float[] rightVector = new float[3];
		rightVector[0] = worldUp[1] * cameraOrientation[2] - worldUp[2] * cameraOrientation[1];
		rightVector[1] = worldUp[2] * cameraOrientation[0] - worldUp[0] * cameraOrientation[2];
		rightVector[2] = worldUp[0] * cameraOrientation[1] - worldUp[1] * cameraOrientation[0];
		// Normalize the right vector
		float rightVectorLength = (float) Math
				.sqrt(rightVector[0] * rightVector[0] + rightVector[1] * rightVector[1] + rightVector[2] * rightVector[2]);
		rightVector[0] /= rightVectorLength;
		rightVector[1] /= rightVectorLength;
		rightVector[2] /= rightVectorLength;
		// Compute the camera's up vector as the cross product of
		// cameraOrientation and rightVector
		float[] upVector = new float[3];
		upVector[0] = cameraOrientation[1] * rightVector[2] - cameraOrientation[2] * rightVector[1];
		upVector[1] = cameraOrientation[2] * rightVector[0] - cameraOrientation[0] * rightVector[2];
		upVector[2] = cameraOrientation[0] * rightVector[1] - cameraOrientation[1] * rightVector[0];
		// Normalize the up vector
		float upVectorLength = (float) Math.sqrt(upVector[0] * upVector[0] + upVector[1] * upVector[1] + upVector[2] * upVector[2]);
		upVector[0] /= upVectorLength;
		upVector[1] /= upVectorLength;
		upVector[2] /= upVectorLength;
		// Rotate the translated point to align with the camera's view direction
		float[] rotatedPoint = new float[3];
		rotatedPoint[0] = translatedPoint[0] * rightVector[0] + translatedPoint[1] * rightVector[1] + translatedPoint[2] * rightVector[2];
		rotatedPoint[1] = -(translatedPoint[0] * upVector[0] + translatedPoint[1] * upVector[1] + translatedPoint[2] * upVector[2]);
		rotatedPoint[2] = translatedPoint[0] * cameraOrientation[0] + translatedPoint[1] * cameraOrientation[1]
				+ translatedPoint[2] * cameraOrientation[2];

		return rotatedPoint;
	}

	public float[] getSurfacePoint3D(int[][] triangleVertices2D, int[] point2D, float[][] triangleVertices3D) {
		float[] point3D = new float[3];
		float x0 = triangleVertices2D[0][0];
		float y0 = triangleVertices2D[0][1];
		float x1 = triangleVertices2D[1][0];
		float y1 = triangleVertices2D[1][1];
		float x2 = triangleVertices2D[2][0];
		float y2 = triangleVertices2D[2][1];

		final float EPSILON = 1e-6f;

		float determinate = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2);
		if (Math.abs(determinate) < EPSILON) {
			// Handle degenerate triangle case
			point3D[0] = (triangleVertices3D[0][0] + triangleVertices3D[1][0] + triangleVertices3D[2][0]) / 3;
			point3D[1] = (triangleVertices3D[0][1] + triangleVertices3D[1][1] + triangleVertices3D[2][1]) / 3;
			point3D[2] = (triangleVertices3D[0][2] + triangleVertices3D[1][2] + triangleVertices3D[2][2]) / 3;
			return point3D;
		}
		float l1 = ((y1 - y2) * (point2D[0] - x2) + (x2 - x1) * (point2D[1] - y2)) / determinate;
		float l2 = ((y2 - y0) * (point2D[0] - x2) + (x0 - x2) * (point2D[1] - y2)) / determinate;
		float l3 = ((y0 - y1) * (point2D[0] - x1) + (x1 - x0) * (point2D[1] - y1)) / determinate;

		float w0 = 1.0f / (triangleVertices3D[0][2] + EPSILON);
		float w1 = 1.0f / (triangleVertices3D[1][2] + EPSILON);
		float w2 = 1.0f / (triangleVertices3D[2][2] + EPSILON);

		float interpolatedW = l1 * w0 + l2 * w1 + l3 * w2;
		if (Math.abs(interpolatedW) < EPSILON) {
			determinate = EPSILON;
		}
		point3D[0] = (l1 * triangleVertices3D[0][0] * w0 + l2 * triangleVertices3D[1][0] * w1 + l3 * triangleVertices3D[2][0] * w2) / interpolatedW;
		point3D[1] = (l1 * triangleVertices3D[0][1] * w0 + l2 * triangleVertices3D[1][1] * w1 + l3 * triangleVertices3D[2][1] * w2) / interpolatedW;
		point3D[2] = 1.0f / interpolatedW;

		return point3D;
	}

	// Clears background between frames
	private void clearbg(int[] pixels, float[] zBuffer) {
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				pixels[x + y * width] = 0;
				zBuffer[x + y * width] = -1;
			}
		}
	}

	private int distanceToHexColor(float distance) {
		distance = Math.max(0, Math.min(distance, 25));
		int intensity = (int) ((1 - (distance / 25)) * 255);
		int color = (intensity << 16) | (intensity << 8) | intensity;

		return (int)(color * Math.tan(intensity));
	}
}