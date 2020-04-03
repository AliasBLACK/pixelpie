package black.alias.pixelpie;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;
import processing.data.StringDict;
import processing.event.MouseEvent;
import black.alias.pixelpie.sprite.Sprite;

public class GameObject {
	public int x, y, xOffset, yOffset, origin, depth, objWidth, objHeight, alpha, index;
	public int bBoxWidth, bBoxHeight, bBoxXOffset, bBoxYOffset;
	public int objFrames, waitFrames, currentFrame, currentWait;
	public float xSpeed, ySpeed;
	public boolean destroyed, visible, lockBBox, lighted, noLoop, preserveFrame, noCollide, noAnimate, reverseAnim;
	public String sprite, type;
	public StringDict parameters;
	public GameObject other, otherPredict;
	public PImage lightTemp;
	final PixelPie pie;

	/**
	 * Constructor.
	 * @param pie
	 */
	public GameObject(PixelPie pie) {
		
		// Grab PixelPie.
		this.pie = pie;
		
		// Set some parameters.
		visible = false;
		lockBBox = false;
		type = "Object";
		sprite = null;
		alpha = 255;
		lightTemp = pie.app.createImage(1, 1, PConstants.ARGB);

		// Add object to collide detect array.
		pie.objectArray.add(this);

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

	/**
	 * Change origin pt.
	 * @param Ori
	 */
	public void setOrigin(int Ori) {
		origin = Ori;
		xOffset = PixelPie.getXOffset(origin, objWidth);
		yOffset = PixelPie.getYOffset(origin, objHeight);
		refreshBBox();
	}

	/**
	 * Assign the sprite for this game object.
	 * @param SpriteFile
	 */
	public void setSprite(String SpriteFile) {
		if (!SpriteFile.equals(sprite)){
			if (pie.spr.get(SpriteFile) != null) {			
				visible = true;
				sprite = SpriteFile;
				Sprite pix = pie.spr.get(sprite);
				objWidth = pix.pixWidth;
				objHeight = pix.sprite.height;
				objFrames = pix.pixFrames;
				waitFrames = pix.waitFrames;
			} else {
				visible = false;
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
	}

	/**
	 * Move frames forward if sprite is animated.
	 */
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

	/**
	 * Init draw if object is on screen.
	 * @param i
	 */
	public void draw(int index) {
		if ( // If it's not on screen, skip draw method.
				PixelPie.toInt(pie.testOnScreen(x, y))
				+ PixelPie.toInt(pie.testOnScreen(x + objWidth, y))
				+ PixelPie.toInt(pie.testOnScreen(x, y + objHeight))
				+ PixelPie.toInt(pie.testOnScreen(x + objWidth, y + objHeight)) == 0) {
			return;
		}
		
		// Add object to render queue.
		pie.depthBuffer.append(PApplet.nf(depth, 4)	+ 0	+ index);
	}
	
	/**
	 * Render this object to the screen.
	 */
	public void render() {
		drawSprite(x - xOffset, y - yOffset, depth, currentFrame, alpha, pie.lighting, sprite);
	}

	/**
	 * Calculate movement.
	 */
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
	
	/**
	 * Draw a sprite to screen.
	 * @param x
	 * @param y
	 * @param depth
	 * @param currentFrame
	 * @param alpha
	 * @param lighted
	 * @param sprite
	 */
	protected void drawSprite(int x, int y, int depth, int currentFrame, int transparency, boolean lighted, String sprite) {
		
		// Grab sprite.
		Sprite spr = pie.spr.get(sprite);

		// Draw image to screen.
		int startX = PApplet.max(0, -(x - pie.displayX));
		int startY = PApplet.max(0, -(y - pie.displayY));
		int drawWidth = spr.pixWidth - PApplet.max(0, x + spr.pixWidth - pie.displayX - pie.matrixWidth) - startX;
		int drawHeight = spr.sprite.height - PApplet.max(0, y + spr.sprite.height - pie.displayY - pie.matrixHeight) - startY;

		// Draw lighting.
		if (lighted) {

			// Apply lighting to the sprite.
			if (lightTemp.width < spr.pixWidth || lightTemp.height < spr.sprite.height) {
				lightTemp = pie.app.createImage(PApplet.max(lightTemp.width, spr.pixWidth), PApplet.max(lightTemp.height, spr.sprite.height), PConstants.ARGB);
			}
			lightTemp.copy(spr.sprite, currentFrame * spr.pixWidth, 0, spr.pixWidth, spr.sprite.height, 0, 0, spr.pixWidth, spr.sprite.height);
			lightTemp.loadPixels();
			for (int i = startX; i < drawWidth + startX; i++) {
				for (int k = startY; k < drawHeight + startY; k++) {

					// If it's a completely transparent pixel, ignore it.
					if (((lightTemp.pixels[k * spr.pixWidth + i] >> 24) & 0xFF) == 0) {
						continue;

					// If not, light it.
					} else {
						int lightRed = (pie.lightMap.pixels[(y + k) * pie.roomWidth + (x + i)] >> 16) & 0xFF;
						int lightGreen = (pie.lightMap.pixels[(y + k) * pie.roomWidth + (x + i)] >> 8) & 0xFF;
						int lightBlue = pie.lightMap.pixels[(y + k) * pie.roomWidth + (x + i)] & 0xFF;
						if (spr.IlumMap != null) {
							float factor = (1 - spr.IlumMap[spr.sprite.width * k + (currentFrame * spr.pixWidth + i)]);
							lightRed = Math.round((1 - (1 - (lightRed / 255.0f)) * factor) * 255);
							lightGreen = Math.round((1 - (1 - (lightGreen / 255.0f)) * factor) * 255);	
							lightBlue = Math.round((1 - (1 - (lightBlue / 255.0f)) * factor) * 255);
						}
						int red = (lightTemp.pixels[k * spr.pixWidth + i] >> 16) & 0xFF;
						int green = (lightTemp.pixels[k * spr.pixWidth + i] >> 8) & 0xFF;
						int blue = lightTemp.pixels[k * spr.pixWidth + i] & 0xFF;
						int alpha = (lightTemp.pixels[k * spr.pixWidth + i] >> 24) & 0xFF;

						lightTemp.pixels[k * spr.pixWidth + i] = 
								alpha << 24 |
								(red * lightRed) 	/ 255 << 16 |
								(green * lightGreen)/ 255 << 8 |
								(blue * lightBlue) 	/ 255;
					}						
				}
			}
			lightTemp.updatePixels();

			// Draw the lighted sprite onto screen.
			PixelPie.screenBuffer.image(
				lightTemp.get(startX, startY, drawWidth, drawHeight),
				(startX > 0) ? 0 : (x - pie.displayX),
				(startY > 0) ? 0 : (y - pie.displayY)
			);

		// Else, if no lighting.
		} else {
			PixelPie.screenBuffer.image(
				spr.sprite.get(startX + (currentFrame * spr.pixWidth), startY, drawWidth, drawHeight),
				(startX > 0) ? 0 : (x - pie.displayX),
				(startY > 0) ? 0 : (y - pie.displayY)
			);
		}
	}
}
