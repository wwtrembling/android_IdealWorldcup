package pkch.company.lib.cacheManager2;

import java.util.TimerTask;


public class ImageCacheManager extends TimerTask {

	private boolean isRunning = false;

	public void run() {
		if (isRunning)
			return;

		synchronized (this) {
			isRunning = true;

			try {

				CustomApplication.mApp.expireOldCache();
			} catch (Exception e) {

			} catch (Throwable t) {

			}
			isRunning = false;
		}

	}

}