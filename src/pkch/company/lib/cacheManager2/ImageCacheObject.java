package pkch.company.lib.cacheManager2;

import java.util.Date;

import android.graphics.Bitmap;

public class ImageCacheObject {

	public Bitmap bm;
	public Date lastRetrieved;

	public ImageCacheObject(Bitmap bm) {
		lastRetrieved = new Date();
		this.bm = bm;
	}

	public void touch() {
		lastRetrieved = new Date();
	}

	public Bitmap getBitmap() {
		touch();
		return bm;
	}
}