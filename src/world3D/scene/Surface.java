package world3D.scene;

import world3D.scene.physics.PositionAttributes;
import world3D.visual3D.Triangle;

public class Surface {

	private PositionAttributes positionAttributes;
	private Triangle[] mesh;
	private float scaleFactor;

	public Surface(Triangle[] mesh, PositionAttributes positionAttributes, float scaleFactor) {
		this.setScaleFactor(scaleFactor);
		this.positionAttributes = positionAttributes;

		Triangle[] transformedModel = new Triangle[mesh.length];

		for (int triangleIndex = 0; triangleIndex < mesh.length; triangleIndex++) {
			transformedModel[triangleIndex] = new Triangle();
			for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
				transformedModel[triangleIndex].vertices[vertexIndex][0] = (mesh[triangleIndex].vertices[vertexIndex][0] +
						positionAttributes.xPos) * scaleFactor;
				transformedModel[triangleIndex].vertices[vertexIndex][1] = (mesh[triangleIndex].vertices[vertexIndex][1] +
						positionAttributes.yPos) * scaleFactor;
				transformedModel[triangleIndex].vertices[vertexIndex][2] = (mesh[triangleIndex].vertices[vertexIndex][2] +
						positionAttributes.zPos) * scaleFactor;

				rotate(transformedModel[triangleIndex].vertices[vertexIndex],
						positionAttributes.xDir,
						positionAttributes.yDir,
						positionAttributes.zDir);
			}
		}
		this.mesh = transformedModel;
	}

	public PositionAttributes getPositionAttributes() {
		return positionAttributes;
	}

	public void setPositionAttributes(PositionAttributes positionAttributes) {
		this.positionAttributes = positionAttributes;
	}

	public Triangle[] getMesh() {
		return mesh;
	}

	public float getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	private static void rotate(float[] vertex, float xDir, float yDir, float zDir) {
		// Normalize the new direction vector
		float length = (float) Math.sqrt(xDir * xDir + yDir * yDir + zDir * zDir);
		xDir /= length;
		yDir /= length;
		zDir /= length;

		float[] initialDir = { 0, 0, 1 };

		float[] axis = {
				initialDir[1] * zDir - initialDir[2] * yDir,
				initialDir[2] * xDir - initialDir[0] * zDir,
				initialDir[0] * yDir - initialDir[1] * xDir
		};

		float cosTheta = xDir * initialDir[0] + yDir * initialDir[1] + zDir * initialDir[2];
		float sinTheta = (float) Math.sqrt(1 - cosTheta * cosTheta);

		float axisLength = (float) Math.sqrt(axis[0] * axis[0] + axis[1] * axis[1] + axis[2] * axis[2]);
		axis[0] /= axisLength;
		axis[1] /= axisLength;
		axis[2] /= axisLength;

		float[][] rotationMatrix = new float[3][3];
		rotationMatrix[0][0] = cosTheta + axis[0] * axis[0] * (1 - cosTheta);
		rotationMatrix[0][1] = axis[0] * axis[1] * (1 - cosTheta) - axis[2] * sinTheta;
		rotationMatrix[0][2] = axis[0] * axis[2] * (1 - cosTheta) + axis[1] * sinTheta;

		rotationMatrix[1][0] = axis[1] * axis[0] * (1 - cosTheta) + axis[2] * sinTheta;
		rotationMatrix[1][1] = cosTheta + axis[1] * axis[1] * (1 - cosTheta);
		rotationMatrix[1][2] = axis[1] * axis[2] * (1 - cosTheta) - axis[0] * sinTheta;

		rotationMatrix[2][0] = axis[2] * axis[0] * (1 - cosTheta) - axis[1] * sinTheta;
		rotationMatrix[2][1] = axis[2] * axis[1] * (1 - cosTheta) + axis[0] * sinTheta;
		rotationMatrix[2][2] = cosTheta + axis[2] * axis[2] * (1 - cosTheta);

		// Rotate the vertex
		float[] rotatedVertex = new float[3];
		rotatedVertex[0] = rotationMatrix[0][0] * vertex[0] + rotationMatrix[0][1] * vertex[1] + rotationMatrix[0][2] * vertex[2];
		rotatedVertex[1] = rotationMatrix[1][0] * vertex[0] + rotationMatrix[1][1] * vertex[1] + rotationMatrix[1][2] * vertex[2];
		rotatedVertex[2] = rotationMatrix[2][0] * vertex[0] + rotationMatrix[2][1] * vertex[1] + rotationMatrix[2][2] * vertex[2];

		// Update the vertex
		System.arraycopy(rotatedVertex, 0, vertex, 0, vertex.length);
	}
}