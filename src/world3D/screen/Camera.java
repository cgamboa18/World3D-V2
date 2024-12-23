package world3D.screen;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import world3D.scene.Scene;
import world3D.scene.entity.Entity;
import world3D.scene.physics.Physics;
import world3D.scene.physics.PositionAttributes;

public class Camera extends Entity implements KeyListener {
	public boolean lookLeft, lookRight, lookUp, lookDown, forward, back, left, right, jump, sink;
	public final float ROTATION_SPEED = 0.045f;

	public Camera(float x, float y, float z, float xd, float yd, float zd, float xp, float yp) {
		setPositionAttributes(new PositionAttributes(x, y, z, xd, yd, zd, xp, yp));
	}

	public void keyPressed(KeyEvent key) {
		if ((key.getKeyCode() == KeyEvent.VK_LEFT))
			lookLeft = true;
		if ((key.getKeyCode() == KeyEvent.VK_RIGHT))
			lookRight = true;
		if ((key.getKeyCode() == KeyEvent.VK_UP))
			lookUp = true;
		if ((key.getKeyCode() == KeyEvent.VK_DOWN))
			lookDown = true;
		if ((key.getKeyCode() == KeyEvent.VK_W))
			forward = true;
		if ((key.getKeyCode() == KeyEvent.VK_S))
			back = true;
		if ((key.getKeyCode() == KeyEvent.VK_A))
			left = true;
		if ((key.getKeyCode() == KeyEvent.VK_D))
			right = true;
		if ((key.getKeyCode() == KeyEvent.VK_SPACE))
			jump = true;
		if ((key.getKeyCode() == KeyEvent.VK_X))
			sink = true;
	}

	public void keyReleased(KeyEvent key) {
		if ((key.getKeyCode() == KeyEvent.VK_LEFT))
			lookLeft = false;
		if ((key.getKeyCode() == KeyEvent.VK_RIGHT))
			lookRight = false;
		if ((key.getKeyCode() == KeyEvent.VK_UP))
			lookUp = false;
		if ((key.getKeyCode() == KeyEvent.VK_DOWN))
			lookDown = false;
		if ((key.getKeyCode() == KeyEvent.VK_W))
			forward = false;
		if ((key.getKeyCode() == KeyEvent.VK_S))
			back = false;
		if ((key.getKeyCode() == KeyEvent.VK_A))
			left = false;
		if ((key.getKeyCode() == KeyEvent.VK_D))
			right = false;
		if ((key.getKeyCode() == KeyEvent.VK_SPACE))
			jump = false;
		if ((key.getKeyCode() == KeyEvent.VK_X))
			sink = false;
	}

	public void update(Scene world) {
		// Movement + Collision
		if (forward) {
			positionAttributes.accelerateXZ(0.05f, false);
		}
		if (back) {
			positionAttributes.accelerateXZ(-0.05f, false);
		}
		if (left) {
			positionAttributes.accelerateXZ(0.05f, true);
		}
		if (right) {
			positionAttributes.accelerateXZ(-0.05f, true);
		}

		// Looking
		if (lookRight) {
			float oldxDir = positionAttributes.xDir;
			positionAttributes.xDir = (float) (positionAttributes.xDir * Math.cos(-ROTATION_SPEED)
					- positionAttributes.zDir * Math.sin(-ROTATION_SPEED));
			positionAttributes.zDir = (float) (oldxDir * Math.sin(-ROTATION_SPEED) + positionAttributes.zDir * Math.cos(-ROTATION_SPEED));
			float oldxPlane = positionAttributes.xPlane;
			positionAttributes.xPlane = (float) (positionAttributes.xPlane * Math.cos(-ROTATION_SPEED)
					- positionAttributes.zPlane * Math.sin(-ROTATION_SPEED));
			positionAttributes.zPlane = (float) (oldxPlane * Math.sin(-ROTATION_SPEED) + positionAttributes.zPlane * Math.cos(-ROTATION_SPEED));
		}
		if (lookLeft) {
			float oldxDir = positionAttributes.xDir;
			positionAttributes.xDir = (float) (positionAttributes.xDir * Math.cos(ROTATION_SPEED)
					- positionAttributes.zDir * Math.sin(ROTATION_SPEED));
			positionAttributes.zDir = (float) (oldxDir * Math.sin(ROTATION_SPEED) + positionAttributes.zDir * Math.cos(ROTATION_SPEED));
			float oldxPlane = positionAttributes.xPlane;
			positionAttributes.xPlane = (float) (positionAttributes.xPlane * Math.cos(ROTATION_SPEED)
					- positionAttributes.zPlane * Math.sin(ROTATION_SPEED));
			positionAttributes.zPlane = (float) (oldxPlane * Math.sin(ROTATION_SPEED) + positionAttributes.zPlane * Math.cos(ROTATION_SPEED));
		}
		if (lookUp) {
			if (positionAttributes.yDir < 1) {
				positionAttributes.yDir += ROTATION_SPEED;
			}

		}
		if (lookDown) {
			if (positionAttributes.yDir > -1) {
				positionAttributes.yDir -= ROTATION_SPEED;
			}
		}
		// Normalize direction vectors
		float length = (float) Math.sqrt(
				positionAttributes.xDir * positionAttributes.xDir + 
				positionAttributes.yDir * positionAttributes.yDir + 
				positionAttributes.zDir * positionAttributes.zDir);
		positionAttributes.xDir /= length;
		positionAttributes.yDir /= length;
		positionAttributes.zDir /= length;
		
		// Jump
		if (jump) {
			positionAttributes.accelerateY(0.05f);
		}
		
		if (sink) {
			positionAttributes.accelerateY(-0.05f);
		}


		Physics.updatePositionAttributes(positionAttributes, getHitboxVertices(), getHitboxCenter(), world);
	}

	public void keyTyped(KeyEvent e) {

	}
}