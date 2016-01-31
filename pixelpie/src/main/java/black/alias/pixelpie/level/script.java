package black.alias.pixelpie.level;

import java.util.ArrayList;
import java.util.HashMap;

import black.alias.pixelpie.PixelPie;
import processing.core.PApplet;
import processing.data.IntDict;

public class script {
	boolean loopFrame;
	int currentFrame, endFrame, currentAction;
	IntDict scriptCoords;
	ArrayList<scriptAction> runningChildren;
	HashMap<String, scriptAction[]> children;
	final PixelPie pie;

	public script(int EndFrame, IntDict ScriptCoords, HashMap<String, scriptAction[]> Children, PixelPie pie) {
		endFrame = EndFrame;
		scriptCoords = ScriptCoords;
		children = Children;
		runningChildren = new ArrayList<scriptAction>();
		this.pie = pie;
	}

	// Update Method.
	public void update() {

		// If current script is still active...
		if ((currentFrame <= endFrame) || (runningChildren.size() > 0)) {

			// If frame is not set to loop, add new scriptActions to
			// runningChildren.
			if (!loopFrame) {
				scriptAction[] newActions = children.get(PApplet.str(currentFrame));
				if (newActions != null) {
					for (scriptAction action : newActions) {
						if (action != null) {
							runningChildren.add(action);
						}
					}
				}
			}

			// Update actions currently in runningChildren.
			for (int i = 0; i < runningChildren.size(); i++) {

				// Get action.
				scriptAction action = runningChildren.get(i);

				// Check if action is done.
				if (action.destroyed) {
					action.destroyed = false;
					runningChildren.remove(i);
					i--;

					// Else, update.
				} else {
					action.updateAll();
				}
			}

			// If not loopFrame, update current frame.
			if (!loopFrame) {
				currentFrame++;
			}

			// If this script is done, remove it.
		} else {
			currentFrame = 0;
			pie.currentScript = null;
		}
	}
}
