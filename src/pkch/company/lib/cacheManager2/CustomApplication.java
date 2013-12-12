package pkch.company.lib.cacheManager2;

import java.util.Timer;


import android.app.Activity;
import android.app.Application;
import android.graphics.Bitmap;

public class CustomApplication extends Application {

	public static CustomApplication mApp = null;
	private ImageCache mImageCache = new ImageCache();
	static private Timer cacheTimer = new Timer();

	@Override
	public void onCreate() {
		mApp = this;
		cacheTimer.schedule(new ImageCacheManager(), 0, 30000);
		super.onCreate();
	}

	@Override
	public void onTerminate() {
		cacheTimer.cancel();
		cacheTimer = null;
		super.onTerminate();
	}

	public static CustomApplication getCustomApplication(Activity a) {
		Application app = a.getApplication();
		return (CustomApplication) app;
	}

	public Bitmap getImage(String filename) {
		return mImageCache.getImage(filename);
	}

	public void addImage(String filename, Bitmap image) {
		mImageCache.addImage(filename, image);
	}

	public void expireOldCache() {
		mImageCache.expireOldCache();
	}

}