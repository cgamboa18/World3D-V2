package world3D.scene.physics;

public class PositionAttributes {
	public float xPos, yPos, zPos, xDir, yDir, zDir, xPlane, zPlane;
	public float xVel, yVel, zVel, xAcc, yAcc, zAcc;
	
	public PositionAttributes(float x, float y, float z, float xd, float yd, float zd, float xp, float zp) {
		xPos = x;
		yPos = y;
		zPos = z;
		xDir = xd;
		yDir = yd;
		zDir = zd;
		xPlane = xp;
		zPlane = zp;
		xVel = 0;
		yVel = 0;
		xAcc = 0;
		yAcc = 0;
	}
	
	public void accelerateXZ(float magnitude, boolean lateral) {
		if(lateral) {
			xAcc += zDir * -magnitude;
			zAcc += xDir * magnitude;
		} else {
			xAcc += xDir * magnitude;
			zAcc += zDir * magnitude;
		}
			
	}
	
	public void accelerateY(float magnitude) {
		yAcc += magnitude;
	}
	
}
