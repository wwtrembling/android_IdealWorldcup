package pkch.company.lib.cacheManager;

import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;

//이미지가 담긴 url 을 http 통신을 통해 가지고 오고 캐쉬 클래스를 이용하여 메모리에 접근한다
public class ImageCacheHandler {
	static final String TAG="ImageCacheHandler";
	private final ImageCache mImageCache;
	private Bitmap mDefaultBitmap;
	
	//생성자
	public ImageCacheHandler(ImageCache imageCache){
		mImageCache= imageCache;
	}
		
	//이미지 뷰를 세팅한다.
	public void setImageBitmap(String url, ImageView iv){
		//캐쉬에서 가지고 온다
		Bitmap bm= mImageCache.getBitmapFromCache(url);
		//값이 없을 경우에는 url이미지를 다운로드 한 후에 캐쉬에 저장한다
		if(bm==null){
			BitmapDownloaderTask task= new BitmapDownloaderTask(iv);
			iv.setImageBitmap(mDefaultBitmap); // default ??
			task.execute(url);
		}
		else {
			iv.setImageBitmap(bm);
		}
	}
	
	//세팅하기전에 뿌려줄 비트맵
	public void setDefaultBitmap(Bitmap bm){
		mDefaultBitmap=bm;
	}
	
	//이미지 캐쉬객체 리턴
	public ImageCache getImageCache(){
		return mImageCache;
	}
	
	//이미지 asynctask 수행
	class BitmapDownloaderTask extends AsyncTask<String, Void, Bitmap>{

		private String downloaderAddress;
		private final WeakReference<ImageView> imageViewReference;
		
		//생성자
		public BitmapDownloaderTask(ImageView iv){
			imageViewReference= new WeakReference<ImageView>(iv);
		}
		
		//url에 해당하는 이미지URL에서 이미지를 불러와서 BITMAP형태로 리턴한다 
		@Override
		protected Bitmap doInBackground(String... params) {
			downloaderAddress = params[0];
			return downloadBitmap(downloaderAddress);
		}
		
		//비트맵을 가지고와서 해당 ImageView에 넣는다
		@Override
		protected void onPostExecute(Bitmap bm) {
			super.onPostExecute(bm);
			if(bm!=null){
				mImageCache.addBitmapToCache(downloaderAddress, bm);
				if(imageViewReference!=null){
					imageViewReference.get().setImageBitmap(bm);
				}
			}
		}
	}
	
	//url을 통해서 비트맵을 다운로드 하여 Bitmap 객체로 가지고 온다
	Bitmap downloadBitmap(String url){
		final HttpClient client= new DefaultHttpClient();
		final HttpGet getRequest= new HttpGet(url);
		
		HttpResponse response;
		try {
			response = client.execute(getRequest);
			final int statusCode= response.getStatusLine().getStatusCode();
			if(statusCode==HttpStatus.SC_OK){
				final HttpEntity entity= response.getEntity();
				if(entity!=null){
					InputStream is=null;
					is = entity.getContent();
					return BitmapFactory.decodeStream(is);
				}
			}
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
