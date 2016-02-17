package black.alias.pixelpie.android.file;

import android.app.Activity;
import black.alias.pixelpie.file.FileManager;

public class FileManagerAndroid implements FileManager {
	
	final Activity activity;
	
	public FileManagerAndroid (Activity activity) {
		this.activity = activity;
	}

	public String getDirectory() {
		return activity.getFilesDir().toString();
	}
}