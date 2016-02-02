package black.alias.pixelpie.java.file;

import processing.core.PApplet;
import black.alias.pixelpie.file.FileManager;

public class FileManagerJava implements FileManager {
	
	final PApplet app;
	
	public FileManagerJava (PApplet app) {
		this.app = app;
	}

	public String getDirectory() {
		return app.dataPath("") + "/";
	}
}