package pkch.company.lib.cacheManager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;


//캐쉬를 저장하고 불러오는 최소객체 클래스
public class ObjectRepository {
	//객체 저장하기
	public static void saveObejct(Object obj, String fname) throws FileNotFoundException, IOException{
		ObjectOutputStream os;
		os = new ObjectOutputStream(new FileOutputStream(fname));
		os.writeObject(os);
		os.flush();
		os.close();
	}
	
	public static Object readObject(String fname) throws ClassNotFoundException, StreamCorruptedException, FileNotFoundException, IOException{
		ObjectInputStream is= new ObjectInputStream(new FileInputStream(fname));
		Object object= is.readObject();
		is.close();
		return object;
		
	}
}
