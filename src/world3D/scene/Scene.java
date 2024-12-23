package world3D.scene;

import java.io.IOException;
import java.util.ArrayList;

import world3D.scene.entity.Entity;
import world3D.scene.physics.PositionAttributes;
import world3D.screen.Camera;
import world3D.visual3D.OBJParser;
import world3D.visual3D.Triangle;

public class Scene {

	private ArrayList<Surface> surfaces;
	private ArrayList<Entity> entities;

	public Scene() {
		surfaces = new ArrayList<Surface>();
		entities = new ArrayList<Entity>();

		try {
			Surface so = new Surface(OBJParser.parseOBJ("models/floor.obj"), new PositionAttributes(0, 0, 0, 1, 0, 0, 0, 0), 10f);
			/*
			float[] s1 = { 1, 0, 0 };
			float[] s2 = { 0, 1, 0 };
			float[] s3 = { 0, 0, 1 };
			Triangle t1 = new Triangle(s1, s2, s3);
			Triangle[] t = { t1 };
			Surface s = new Surface(t, new PositionAttributes(1, 0, 0, 1, 0, 0, 0, 0), 5f);*/
			surfaces.add(so);
			
			Entity e = new Entity();
			e.setPositionAttributes(new PositionAttributes(0, 2, 0, 1, 0, 0, 0, 0));
			e.setModel(OBJParser.parseOBJ("models/thinker.obj"));
			entities.add(e);
			
			Entity e1 = new Entity();
			e1.setPositionAttributes(new PositionAttributes(3, 2, 0, 1, 0, 0, 0, 0));
			e1.setModel(OBJParser.parseOBJ("models/cube.obj"));
			entities.add(e1);
			
			Entity e2 = new Entity();
			e2.setPositionAttributes(new PositionAttributes(0, 2, 1, 1, 0, 0, 0, 0));
			e2.setModel(OBJParser.parseOBJ("models/fish.obj"));
			entities.add(e2);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}

	public boolean surfaceBoxIntersect(float[][] box, float[] boxMidpoint) {
		for (int surfaceIndex = 0; surfaceIndex < surfaces.size(); surfaceIndex++) {
			for (int triangleIndex = 0; triangleIndex < surfaces.get(surfaceIndex).getMesh().length; triangleIndex++) {
				float[][] triangleVertices = surfaces.get(surfaceIndex).getMesh()[triangleIndex].getVertices();
				//System.out.println(Arrays.deepToString(box));
				float[] triangleMidpoint = {
						(triangleVertices[0][0] + triangleVertices[1][0] + triangleVertices[2][0]) / 3,
						(triangleVertices[0][1] + triangleVertices[1][1] + triangleVertices[2][1]) / 3,
						(triangleVertices[0][2] + triangleVertices[1][2] + triangleVertices[2][2]) / 3
				};

				float[] planePoint = midpointBetween(boxMidpoint, triangleMidpoint);

				float[] normal = subtract(triangleMidpoint, boxMidpoint);
				normal = normalize(normal);

				if (isZeroVector(normal)) {
					continue;
				}

				// Plane equation: a(x - x1) + b(y - y1) + c(z - z1) = 0
				float a = normal[0];
				float b = normal[1];
				float c = normal[2];
				float x1 = planePoint[0];
				float y1 = planePoint[1];
				float z1 = planePoint[2];

				// Check each vertex of the box against the plane
				boolean hasPositive = false;
				boolean hasNegative = false;

				for (float[] vertex : box) {
					float x = vertex[0];
					float y = vertex[1];
					float z = vertex[2];
					float distance = a * (x - x1) + b * (y - y1) + c * (z - z1);

					if (distance > 0) {
						hasPositive = true;
					} else if (distance < 0) {
						hasNegative = true;
					}

					if (hasPositive && hasNegative) {
						// Box intersects the plane
						return true;
					}
				}
				
				hasPositive = false;
				hasNegative = false;

				for (float[] vertex : triangleVertices) {
					float x = vertex[0];
					float y = vertex[1];
					float z = vertex[2];
					float distance = a * (x - x1) + b * (y - y1) + c * (z - z1);

					if (distance > 0) {
						hasPositive = true;
					} else if (distance < 0) {
						hasNegative = true;
					}

					if (hasPositive && hasNegative) {
						// Triangle intersects the plane
						return true;
					}
				}
			}
		}
		return false; // No collision detected
	}

	// Helper methods
	private float[] midpointBetween(float[] a, float[] b) {
		return new float[] {
				(a[0] + b[0]) / 2,
				(a[1] + b[1]) / 2,
				(a[2] + b[2]) / 2
		};
	}

	private float[] subtract(float[] a, float[] b) {
		return new float[] { a[0] - b[0], a[1] - b[1], a[2] - b[2] };
	}

	private float[] normalize(float[] vector) {
		float length = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
		return new float[] { vector[0] / length, vector[1] / length, vector[2] / length };
	}

	private boolean isZeroVector(float[] vector) {
		return vector[0] == 0 && vector[1] == 0 && vector[2] == 0;
	}

	// Finds if point is within field of view
	public boolean pointInFov(float xp, float yp, Camera camera) {
		int count = 0;

		for (int viewBoundIndex = 0; viewBoundIndex < 3; viewBoundIndex++) {

			float x1 = camera.positionAttributes.xPos;
			float y1 = camera.positionAttributes.yPos;
			float x2 = camera.positionAttributes.xPos;
			float y2 = camera.positionAttributes.yPos;

			if (viewBoundIndex <= 1) {
				x1 += (camera.positionAttributes.xDir + -camera.positionAttributes.xPlane) * 1000;// render dist 100
				y1 += (camera.positionAttributes.yDir + -camera.positionAttributes.zPlane) * 1000;
			}
			if (viewBoundIndex >= 1) {
				x2 += (camera.positionAttributes.xDir + camera.positionAttributes.xPlane) * 1000;
				y2 += (camera.positionAttributes.yDir + camera.positionAttributes.zPlane) * 1000;
			}

			if ((yp < y1) != (yp < y2) && xp < x1 + ((yp - y1) / (y2 - y1)) * (x2 - x1)) {
				count++;
			}
		}

		if (count % 2 == 1) {
			return true;
		} else {
			count = 0;
		}
		return false; // false
	}

	// Finds entities that has a position within cameras FOV
	public Triangle[] getVisibleTriangles(Camera camera) {
		ArrayList<Triangle> visibleTriangles = new ArrayList<Triangle>();

		for (int entityIndex = 0; entityIndex < entities.size(); entityIndex++) {
			if (entities.get(entityIndex).isVisible() && entities.get(entityIndex).getModel() != null) {
				boolean inBounds = true;// unlock
				for (int triangleIndex = 0; triangleIndex < entities.get(entityIndex).getModel().length; triangleIndex++) {
					for (int vertexIndex = 0; vertexIndex <= 2; vertexIndex++) {
						if (pointInFov(entities.get(entityIndex).getModel()[triangleIndex].vertices[vertexIndex][0],
								entities.get(entityIndex).getModel()[triangleIndex].vertices[vertexIndex][1], camera)) {
							inBounds = true;
						}
						// If the triangle is found to be visible, stop checking
						if (inBounds) {
							visibleTriangles.add(entities.get(entityIndex).getModel()[triangleIndex]);
							break;
						}
					}
				}
			}
		}
		for (int surfaceIndex = 0; surfaceIndex < surfaces.size(); surfaceIndex++) {
			boolean inBounds = true;// unlock
			for (int triangleIndex = 0; triangleIndex < surfaces.get(surfaceIndex).getMesh().length; triangleIndex++) {
				for (int vertexIndex = 0; vertexIndex <= 2; vertexIndex++) {
					if (pointInFov(surfaces.get(surfaceIndex).getMesh()[triangleIndex].vertices[vertexIndex][0],
							surfaces.get(surfaceIndex).getMesh()[triangleIndex].vertices[vertexIndex][1], camera)) {
						inBounds = true;
					}
					// If the mesh triangle is found to be visible, stop checking
					if (inBounds) {
						visibleTriangles.add(surfaces.get(surfaceIndex).getMesh()[triangleIndex]);
						break;
					}
				}
			}
		}

		return visibleTriangles.toArray(new Triangle[0]);
	}
}
