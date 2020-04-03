package black.alias.pixelpie.level;

import processing.core.*;

public class ScriptAction {
	Script parent;
	boolean destroyed, pause;
	int currentFrame, endFrame;
	String[] arguments;

	// Construct.
	ScriptAction() {
	}

	// Setup.
	void setup(String Arguments, Script Parent) {
		arguments = PApplet.split(Arguments, ",");
		for (int i = 0; i < arguments.length; i++) {
			arguments[i] = PApplet.trim(arguments[i]);
		}
		parent = Parent;
	}

	// Update method.
	void updateAll() {

		// Call init() on first frame.
		if (currentFrame == 0) {
			init();
		}

		// Progress frame or destroy action.
		if (currentFrame < endFrame) {
			update();
			if (!parent.loopFrame) {
				currentFrame++;
			}
		} else {
			destroyed = true;
			currentFrame = 0;
		}
	}

	// Client methods.
	void init() {
	}

	void update() {
	}
}
