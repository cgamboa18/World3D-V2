package world3D.scene.physics;

import world3D.scene.Scene;

public class Physics {

	public static final float FRICTION = 1.15f;

	public static void updatePositionAttributes(PositionAttributes pa, float[][] hitboxVertices, float[] hitboxCenter, Scene world) {
		// Update velocity based off of acceleration
		pa.xVel += pa.xAcc;
		pa.yVel += pa.yAcc;
		pa.zVel += pa.zAcc;

		float totalVel = (float) Math.sqrt(Math.pow(pa.xVel, 2) + Math.pow(pa.yVel, 2) + Math.pow(pa.zVel, 2));

		// Apply friction
		if (totalVel > 0) {
			pa.xVel /= FRICTION;
			pa.yVel /= FRICTION;
			pa.zVel /= FRICTION;

			if (totalVel < 0.0001) {
				pa.xVel = 0;
				pa.yVel = 0;
				pa.zVel = 0;
			}
		}

		float[][] nextHitboxVerticesX = hitboxVertices.clone();
		float[][] nextHitboxVerticesY = hitboxVertices.clone();
		float[][] nextHitboxVerticesZ = hitboxVertices.clone();
		float[] nextCenterX = hitboxCenter.clone();
		float[] nextCenterY = hitboxCenter.clone();
		float[] nextCenterZ = hitboxCenter.clone();
		
		// calculate hitbox for frame after update
		for (int vertexIndex = 0; vertexIndex < 8; vertexIndex++) {
			nextHitboxVerticesX[vertexIndex][0] = hitboxVertices[vertexIndex][0] + pa.xVel;
			nextHitboxVerticesY[vertexIndex][1] = hitboxVertices[vertexIndex][1] + pa.yVel;
			nextHitboxVerticesZ[vertexIndex][2] = hitboxVertices[vertexIndex][2] + pa.zVel;
		}
		
		nextCenterX[0] = hitboxCenter[0] + pa.xVel;
		nextCenterY[1] = hitboxCenter[1] + pa.yVel;
		nextCenterZ[2] = hitboxCenter[2] + pa.zVel;
		
		// if update will cause collision, cancel TODO: COLLISION DISABLED
		boolean collisionDisabled = true;
		
		if (collisionDisabled || !world.surfaceBoxIntersect(nextHitboxVerticesX, nextCenterX)) {
			pa.xPos += pa.xVel;
		} else {
			pa.xVel = -pa.xVel;
		}
		
		if (collisionDisabled || !world.surfaceBoxIntersect(nextHitboxVerticesY, nextCenterY)) {
			pa.yPos += pa.yVel;
		} else {
			pa.yVel = 0;
		}
		
		if (collisionDisabled || !world.surfaceBoxIntersect(nextHitboxVerticesZ, nextCenterZ)) {
			pa.zPos += pa.zVel;
		} else {
			pa.zVel = -pa.zVel;
		}

		// Reset acceleration
		pa.xAcc = 0;
		pa.yAcc = 0;
		pa.zAcc = 0;
	}

}
