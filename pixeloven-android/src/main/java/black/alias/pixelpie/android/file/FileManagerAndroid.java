package black.alias.pixelpie.android.file;

import processing.core.PApplet;
import black.alias.pixelpie.file.FileManager;

public class FileManagerAndroid implements FileManager {
	
	final PApplet app;
	
	public FileManagerAndroid (PApplet app) {
		this.app = app;
	}

	public String getDirectory() {
		return app.getActivity().getFilesDir().toString();
	}
}