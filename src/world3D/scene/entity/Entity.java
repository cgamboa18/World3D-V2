package world3D.scene.entity;

import world3D.scene.physics.PositionAttributes;
import world3D.visual3D.Triangle;

public class Entity {

	private boolean visible;
	private Triangle[] model;
	private float scaleFactor;
	private float[] hitboxCenter;
	private float[] hitboxDimensions; // xLength, yLength, zLength

	public PositionAttributes positionAttributes;

	public Entity() {
		scaleFactor = 1;
		positionAttributes = null;
		model = null;
		visible = true;
		hitboxCenter = new float[3];
		float[] dim = { 0.05f, 0.05f, 0.05f };
		hitboxDimensions = dim;
	}
	
	public Entity(Triangle[] m, PositionAttributes pa) {
		scaleFactor = 1;
		positionAttributes = pa;
		setModel(m);
		visible = true;
		hitboxCenter = new float[3];
		float[] dim = { 0.05f, 0.05f, 0.05f };
		hitboxDimensions = dim;
	}

	public boolean isVisible() {
		return visible;
	}

	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	public Triangle[] getModel() {
		return model;
	}

	public void setModel(Triangle[] model) {
		Triangle[] transformedModel = new Triangle[model.length];

		for (int triangleIndex = 0; triangleIndex < model.length; triangleIndex++) {
			transformedModel[triangleIndex] = new Triangle();
			for (int vertexIndex = 0; vertexIndex < 3; vertexIndex++) {
				transformedModel[triangleIndex].vertices[vertexIndex][0] = (model[triangleIndex].vertices[vertexIndex][0] +
						positionAttributes.xPos) * scaleFactor;
				transformedModel[triangleIndex].vertices[vertexIndex][1] = (model[triangleIndex].vertices[vertexIndex][1] +
						positionAttributes.yPos) * scaleFactor;
				transformedModel[triangleIndex].vertices[vertexIndex][2] = (model[triangleIndex].vertices[vertexIndex][2] +
						positionAttributes.zPos) * scaleFactor;
			}
		}

		this.model = transformedModel;
	}

	public double getScaleFactor() {
		return scaleFactor;
	}

	public void setScaleFactor(float scaleFactor) {
		this.scaleFactor = scaleFactor;
	}

	public PositionAttributes getPositionAttributes() {
		return positionAttributes;
	}

	public void setPositionAttributes(PositionAttributes positionAttributes) {
		this.positionAttributes = positionAttributes;
	}

	public void setHitbox(float[] center, float[] dimensions) {
		hitboxCenter = center;
		hitboxDimensions = dimensions;
	}

	public float[][] getHitboxVertices() {
		float[][] vertices = new float[8][3];

		vertices[0] = new float[] { 
				getHitboxCenter()[0] - hitboxDimensions[0], 
				getHitboxCenter()[1] - hitboxDimensions[1], 
				getHitboxCenter()[2] - hitboxDimensions[2] };
		vertices[1] = new float[] { 
				getHitboxCenter()[0] + hitboxDimensions[0], 
				getHitboxCenter()[1] - hitboxDimensions[1], 
				getHitboxCenter()[2] - hitboxDimensions[2] };
		vertices[2] = new float[] { 
				getHitboxCenter()[0] + hitboxDimensions[0], 
				getHitboxCenter()[1] + hitboxDimensions[1], 
				getHitboxCenter()[2] - hitboxDimensions[2] };
		vertices[3] = new float[] { 
				getHitboxCenter()[0] - hitboxDimensions[0], 
				getHitboxCenter()[1] + hitboxDimensions[1], 
				getHitboxCenter()[2] - hitboxDimensions[2] };
		vertices[4] = new float[] { 
				getHitboxCenter()[0] - hitboxDimensions[0], 
				getHitboxCenter()[1] - hitboxDimensions[1], 
				getHitboxCenter()[2] + hitboxDimensions[2] };
		vertices[5] = new float[] { 
				getHitboxCenter()[0] + hitboxDimensions[0], 
				getHitboxCenter()[1] - hitboxDimensions[1], 
				getHitboxCenter()[2] + hitboxDimensions[2] };
		vertices[6] = new float[] { 
				getHitboxCenter()[0] + hitboxDimensions[0], 
				getHitboxCenter()[1] + hitboxDimensions[1], 
				getHitboxCenter()[2] + hitboxDimensions[2] };
		vertices[7] = new float[] { 
				getHitboxCenter()[0] - hitboxDimensions[0], 
				getHitboxCenter()[1] + hitboxDimensions[1], 
				getHitboxCenter()[2] + hitboxDimensions[2] };

		return vertices;
	}

	public float[] getHitboxCenter() {
		float[] boxMidpoint = new float[3];
		boxMidpoint[0] = hitboxCenter[0] + positionAttributes.xPos;
		boxMidpoint[1] = hitboxCenter[1] + positionAttributes.yPos;
		boxMidpoint[2] = hitboxCenter[2] + positionAttributes.zPos;

		return boxMidpoint;
	}
}
