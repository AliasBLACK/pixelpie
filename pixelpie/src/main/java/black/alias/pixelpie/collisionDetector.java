package black.alias.pixelpie;

import java.util.ArrayList;

/**
 * Collision detection.
 * @author Xuanming
 *
 */
public class CollisionDetector extends Thread{
	public ArrayList<GameObject> objectArray = new ArrayList<GameObject>();
	final PixelPie pie;

	public CollisionDetector(PixelPie pie) {
		this.pie = pie;
	}

	public void start() {
		super.start();
	}

	public void run() {
		// Go through the list and detect collisions.
		if (objectArray.size() != 0) {
			for (int i = 0; i < objectArray.size(); i++) {

				// Select object 1.
				GameObject obj1 = objectArray.get(i);

				// Test if object 1 is flagged as destroyed. Remove it from
				// list.
				if (obj1.destroyed) {
					objectArray.remove(i);
					i--;

					// Else, proceed with collision testing if noCollide is
					// false.
				} else if (!obj1.noCollide) {
					for (int k = i + 1; k < objectArray.size(); k++) {

						// Get object 2.
						GameObject obj2 = objectArray.get(k);

						// If object 2's noCollide is also false...
						if (!obj2.noCollide) {

							// See if they collide.
							if (PixelPie.objCollision(obj1, obj2)) {

								// Run the collision event in each object in
								// event of collision.
								obj1.other = obj2;
								obj1.collide();

								obj2.other = obj1;
								obj2.collide();
							}

							// If there's a velocity vector, see if it will
							// collide with anything next frame.
							if ((obj1.xSpeed != 0) || (obj1.ySpeed != 0) || (obj2.xSpeed != 0)
									|| (obj2.ySpeed != 0)) {
								if (PixelPie.objColPredictive(obj1, obj2)) {

									// Run the collision event in each
									// object in event of collision.
									obj1.otherPredict = obj2;
									obj1.colPredict();

									obj2.otherPredict = obj1;
									obj2.colPredict();
								}
							}
						}
					}
				}
			}
		}

		// Sync with current frame.
		pie.waitThread = false;
	}
}
