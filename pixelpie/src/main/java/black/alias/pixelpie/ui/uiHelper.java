package black.alias.pixelpie.ui;

import de.lessvoid.nifty.Nifty;
import de.lessvoid.nifty.elements.render.TextRenderer;

/**
 * UI Helper class.
 * @author Xuanming
 *
 */
public class uiHelper {
	private final Nifty nifty;
	
	/**
	 * Constructor.
	 * @param nifty
	 */
	public uiHelper(Nifty nifty) {
		this.nifty = nifty;
	}
	
	/**
	 * Get the current screen id.
	 * @return
	 */
	public String getScreenId() {
		return nifty.getCurrentScreen().getScreenId();
	}
	
	/**
	 * Change text in a text element on the current screen.
	 * @param textId ID of the text element
	 * @param text Text to change it to.
	 */
	public void setText(String textId, String text) {
		nifty.getCurrentScreen().findElementById(textId).getRenderer(TextRenderer.class).setText(text);
	}
	
	/**
	 * Goto screen.
	 * @param screenId
	 */
	public void gotoScreen(String screenId) {
		nifty.gotoScreen(screenId);
	}
}
