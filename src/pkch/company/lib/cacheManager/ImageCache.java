package pkch.company.lib.cacheManager;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;
import java.io.StreamCorruptedException;
import java.util.HashMap;
import java.util.Map;

import android.graphics.Bitmap;
import android.util.Log;

//이미지에 대한 캐쉬를 메모리에 저장하는 기능을 제공하는 클래스
public class ImageCache implements Serializable{
	private static final long serialVersionUID=3490364931254392574L;
	private final Map<String, SynchronizedBitmap> synchronizedBitMap;
	
	//클래스 생성자, 해쉬맵 구조를 매핑한다
	public ImageCache(){
		synchronizedBitMap= new HashMap<String, ImageCache.SynchronizedBitmap>();
	}
	
	//URL을 키값으로 하는 MAP에 URL과 매치되는 Synchronized된 BITMAP객체를 저장한다
	void addBitmapToCache(String url, Bitmap bm){
		//Log.e("---------","answer : "+url);
		synchronizedBitMap.put(url, new SynchronizedBitmap(bm));
	}
	
	//URL을 키값으로하는 MAP에 저장된 Synchronized된 BITMAP객체를 반환한다
	Bitmap getBitmapFromCache(String url){
		SynchronizedBitmap bm= synchronizedBitMap.get(url);
		if(bm!=null) return bm.get();
		return null;
	}
	
	//모든 캐쉬를 삭제한다
	public void clearCache(){
		synchronizedBitMap.clear();
	}
	
	//캐쉬에서 읽어오기
	public static ImageCache toImageCache(String fname){
		ImageCache imageCache=null;
		try {
			//캐쉬에서 읽어오는 것인가???
			imageCache = (ImageCache)ObjectRepository.readObject(fname);
		} catch (StreamCorruptedException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return imageCache;
	}
	
	//이미지를 캐쉬에 저장한다
	public static boolean fromImageCache(String fname, ImageCache cache){
		try {
			ObjectRepository.saveObejct(cache, fname);
			return true;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	//직렬화된 Bitmap 객체(MAP에 넣기 위함)를 선언합니다.
	static final class SynchronizedBitmap implements Serializable{
		private static final long serialVersionUID=1859678728937516189L;
		private final Bitmap bm;
		public SynchronizedBitmap(Bitmap bm){
			this.bm= bm;
		}
		
		public Bitmap get(){
			return bm;
		}
	}
}
