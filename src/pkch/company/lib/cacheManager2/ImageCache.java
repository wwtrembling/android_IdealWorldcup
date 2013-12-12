package pkch.company.lib.cacheManager2;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

public class ImageCache extends HashMap<String, ImageCacheObject> {

	public void addImage(String filename, Bitmap image) {
		synchronized (this) {
			if (!this.containsKey(filename))
				this.put(filename, new ImageCacheObject(image));
		}
	}

	public Bitmap getImage(String filename) {
		synchronized (this) {
			if (this.size() > 0) {
				ImageCacheObject o = this.get(filename);
				return (o != null) ? o.getBitmap() : null;
			}
		}
		return null;
	}

	public void empty() {
		empty(false);
	}

	public void empty(boolean doTimeCheck) {

		Date d = new Date();

		synchronized (this) {
			if (this.size() > 0) {

				try {

					ArrayList<String> list = new ArrayList<String>();

					for (Iterator i = this.entrySet().iterator(); i.hasNext();) {

						Map.Entry<String, ImageCacheObject> pair = (Map.Entry<String, ImageCacheObject>) i
								.next();
						ImageCacheObject o = pair.getValue();

						if (!doTimeCheck
								|| d.getTime() - o.lastRetrieved.getTime() > 60000) {
							o.bm = null;
							pair.setValue(null);
							list.add(pair.getKey());

						}

					}

					for (int i = 0; i < list.size(); i++) {
						this.remove(list.get(i));
					}

				} catch (Exception e) {
					Log.v("ImageCache",
							"ImageCache: Exception in function empty()");
				}
			}
		}

	}

	public void expireOldCache() {
		empty(true);
	}
}