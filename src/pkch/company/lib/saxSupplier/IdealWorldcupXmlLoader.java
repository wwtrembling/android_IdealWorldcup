package pkch.company.lib.saxSupplier;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import pkch.company.domain.IdealWorldcupElement;

//지정된 XML을 받아옴
public class IdealWorldcupXmlLoader extends DefaultHandler{	
	//생성자
	public HashMap<HashMap<Integer,String>, ArrayList<IdealWorldcupElement>> getAllLists(String xmlstr) {
		HashMap<HashMap<Integer,String>, ArrayList<IdealWorldcupElement>> lists= null;
		/*
		try {
			//Log.e("pkch","XML 파싱 시작!");
			XMLReader reader= SAXParserFactory.newInstance().newSAXParser().getXMLReader();
			//만들어진 핸들러 객체 생성, XML reader에 핸들러 객체를 적용시킴
			IdealWorldcupXmlHandler handler= new IdealWorldcupXmlHandler();
			reader.setContentHandler(handler);
			reader.parse(new InputSource(new ByteArrayInputStream(xmlstr.getBytes("utf-8") ) ) );
			lists= handler.getLists();
			//Log.e("pkch","XML 파싱 완료!");
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		*/
		return lists;
	}
		
}
