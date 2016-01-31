package black.alias.pixelpie;

import processing.data.StringDict;
import processing.event.MouseEvent;
import black.alias.pixelpie.sprite.sprite;

public class gameObject {
	public int x, y, xOffset, yOffset, origin, depth, objWidth, objHeight, alpha, index;
	public int bBoxWidth, bBoxHeight, bBoxXOffset, bBoxYOffset;
	public int objFrames, waitFrames, currentFrame, currentWait; // Animation
	
	// Variables.
	public float xSpeed, ySpeed;
	public boolean destroyed, visible, lockBBox, lighted, noLoop, preserveFrame, noCollide, noAnimate, reverseAnim;
	public String sprite, type;
	public StringDict parameters;
	public gameObject other, otherPredict;
	
	// Reference to PixelPie.
	final PixelPie pie;

	// Constructor.
	public gameObject(PixelPie pie) {
		visible = true;
		lockBBox = false;
		type = "Object";
		sprite = null;
		alpha = 255;
		
		// Grab PixelPie.
		this.pie = pie;

		// Add object to collide detect array.
		pie.collider.objectArray.add(this);

		// Grab index.
		index = pie.index;
		pie.index++;
	}

	public void setX(int PosX) {
		x = PosX;
	}

	public void setY(int PosY) {
		y = PosY;
	}

	public void setAlpha(int Alpha) {
		alpha = Alpha;
	}

	public void setDepth(int RenderDepth) {
		depth = RenderDepth;
	}

	public void setWidth(int Width) {
		objWidth = Width;
		refreshBBox();
	}

	public void setHeight(int Height) {
		objHeight = Height;
		refreshBBox();
	}

	public void setType(String TypeString) {
		type = TypeString;
	}

	public void setLighted(boolean Lighted) {
		lighted = Lighted;
	}

	public void setNoCollide(boolean NoCollide) {
		noCollide = NoCollide;
	}

	public void setNoAnimate(boolean NoAnimate) {
		noAnimate = NoAnimate;
	}

	public void setNoLoop(boolean NoLoop) {
		noLoop = NoLoop;
	}

	public void setReverseAnim(boolean ReverseAnim) {
		reverseAnim = ReverseAnim;
	}

	public void setParameters(StringDict param) {
		parameters = param;
	}

	public void setOrigin(int Ori) {
		origin = Ori;
		xOffset = PixelPie.getXOffset(origin, objWidth);
		yOffset = PixelPie.getYOffset(origin, objHeight);
		refreshBBox();
	}

	public void setSprite(String SpriteFile) {
		if (pie.spr.get(SpriteFile) != null) {
			sprite = SpriteFile;
			sprite pix = pie.spr.get(sprite);
			objWidth = pix.pixWidth;
			objHeight = pix.sprite.height;
			objFrames = pix.pixFrames;
			waitFrames = pix.waitFrames;
		} else {
			sprite = null;
			objWidth = 0;
			objHeight = 0;
			objFrames = 0;
			waitFrames = 0;
			pie.log.printlg("Sprite " + SpriteFile + " not found.");
		}
		if (!preserveFrame) {
			currentFrame = 0;
		}
		xOffset = PixelPie.getXOffset(origin, objWidth);
		yOffset = PixelPie.getYOffset(origin, objHeight);
		refreshBBox();
	}

	public void animate() {
		// Update frames according to FPS.
		if (!reverseAnim) {
			if (currentWait < waitFrames) {
				currentWait++;
			} else {
				currentWait = 0;
				if (currentFrame < objFrames) {
					currentFrame++;
				} else {
					if (!noLoop) {
						currentFrame = 0;
					} // Don't reset frame if noLoop is set.
				}
			}
		} else {
			if (currentWait < waitFrames) {
				currentWait++;
			} else {
				currentWait = 0;
				if (currentFrame > 0) {
					currentFrame--;
				} else {
					if (!noLoop) {
						currentFrame = objFrames;
					} // Don't reset frame if noLoop is set.
				}
			}
		}
	}

	public void draw() {
		if ( // If it's not on screen, skip draw method.
				PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset, y - yOffset + objHeight))
				+ PixelPie.toInt(pie.testOnScreen(x - xOffset + objWidth, y - yOffset + objHeight)) == 0) {
			return;
		}

		if (lighted && pie.lighting) {
			pie.drawSprite(x - xOffset, y - yOffset, depth, currentFrame, alpha, 1, sprite);
		} else {
			pie.drawSprite(x - xOffset, y - yOffset, depth, currentFrame, alpha, sprite);
		}
	}

	public void move() {
		x = Math.round(x + xSpeed);
		y = Math.round(y + ySpeed);
	}

	public void init() {
	} // Initializing method.

	public void update() {
	} // Drawing method.

	public void collide() {
	} // Run when game object collides with another game object.

	public void colPredict() {
	} // Run when game object will collide into something next frame.

	public void keyPressed() {
	} // Run when a key is pressed.

	public void keyReleased() {
	} // Run when a key is released.

	public void mousePressed() {
	} // Run when mouse is clicked.

	public void mouseReleased() {
	} // Run when mouse is released.

	public void mouseWheel(MouseEvent event) {
	}

	public void destroy() {
		destroyed = true;
	}

	public void customEvent01() {
	} // Custom event 01.

	public void customEvent02() {
	} // Custom event 02.

	/**
	 *  Refresh bounding box.
	 */
	private void refreshBBox() {
		
		// Bounding box.
		// If bounding box is not locked, change the bounding box dimensions.
		if (!lockBBox) {
			bBoxWidth = objWidth;
			bBoxHeight = objHeight;
			bBoxXOffset = xOffset;
			bBoxYOffset = yOffset;
		} else {

			// If no current sprite, change bounding box anyway.
			if (sprite == null) {
				bBoxWidth = objWidth;
				bBoxHeight = objHeight;
				bBoxXOffset = xOffset;
				bBoxYOffset = yOffset;
			}
		}
	}
}
