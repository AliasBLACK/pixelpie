package black.alias.pixelpie.ui.render;

import java.io.File;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import processing.core.PApplet;
import de.lessvoid.nifty.tools.resourceloader.ResourceLocation;

public class ProcessingLocation implements ResourceLocation {
	
	PApplet app;
	
	public ProcessingLocation(PApplet app) {
		this.app = app;
	}

	public InputStream getResourceAsStream(String ref) {		
		return app.createInput(ref);
	}

	public URL getResource(String ref) {
		try {
			File loc = new File(app.dataPath(ref));
			return loc.toURI().toURL();
			
		} catch (MalformedURLException e) {
			e.printStackTrace();
			return null;
		}
	}

}
