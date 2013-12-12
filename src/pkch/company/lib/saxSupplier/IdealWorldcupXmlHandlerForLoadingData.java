package pkch.company.lib.saxSupplier;

import java.util.ArrayList;
import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Log;

import pkch.company.domain.IdealWorldcupLoadingDataElement;

public class IdealWorldcupXmlHandlerForLoadingData extends DefaultHandler{
	private HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>> lists;
	private ArrayList<IdealWorldcupLoadingDataElement> lists2;
	private IdealWorldcupLoadingDataElement lists3;
	private StringBuilder sb;
	private String str;
	
	//문서 읽기가 시작했을 경우
	@Override
	public void startDocument() throws SAXException {
		super.startDocument();
	}

	//문서 읽기가 끝났을 경우
	@Override
	public void endDocument() throws SAXException {
		super.endDocument();
	}
	
	//TAG를 만났을 경우	
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, qName, attributes);
			//Log.e("pkch",qName);
			if("items".equals(qName)){
				lists= new HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>>();
				sb= new StringBuilder();
			}
			else if("client_item".equals(qName)){
				lists2= new ArrayList<IdealWorldcupLoadingDataElement>();
			}
			else if("client_question".equals(qName)){
				lists2= new ArrayList<IdealWorldcupLoadingDataElement>();
			}
			else if("group_item".equals(qName)){
				lists2= new ArrayList<IdealWorldcupLoadingDataElement>();
			}
			else if("group_question".equals(qName)){
				lists2= new ArrayList<IdealWorldcupLoadingDataElement>();
			}
			else if("question_item".equals(qName)){
				lists2= new ArrayList<IdealWorldcupLoadingDataElement>();
			}
			else if("item".equals(qName)){
				lists3= new IdealWorldcupLoadingDataElement();
			}
		}
		
		//TAG 문이 끝났을 경우
		@Override
		public void endElement(String uri, String localName, String qName)throws SAXException {
			super.endElement(uri, localName, qName);
			if("items".equals(qName)){
			}
			else if("client_item".equals(qName)){
				lists.put("client_item", lists2);
			}
			else if("client_question".equals(qName)){
				lists.put("client_question", lists2);
			}
			else if("group_item".equals(qName)){
				lists.put("group_item", lists2);
			}
			else if("group_question".equals(qName)){
				lists.put("group_question", lists2);
			}
			else if("question_item".equals(qName)){
				lists.put("question_item", lists2);
			}
			else if("item".equals(qName)){
				lists2.add(lists3);
			}
			
			else if("client_group_group_no".equals(qName)){
				str=sb.toString();
				lists3.set_client_group_group_no(Integer.parseInt(str));
			}
			else if("client_item_item_no".equals(qName)){
				str=sb.toString();
				lists3.set_client_item_item_no(Integer.parseInt(str));
			}
			else if("client_question_qs_no".equals(qName)){
				str=sb.toString();
				lists3.set_client_question_qs_no(Integer.parseInt(str));
			}
			else if("group_no".equals(qName)){
				str=sb.toString();
				lists3.set_group_no(Integer.parseInt(str));
			}
			else if("group_uid".equals(qName)){
				str=sb.toString();
				lists3.set_group_uid(str);
			}
			else if("item_no".equals(qName)){
				str=sb.toString();
				lists3.set_item_no(Integer.parseInt(str));
			}
			else if("item_name".equals(qName)){
				str=sb.toString();
				lists3.set_item_name(str);
			}
			else if("item_size".equals(qName)){
				str=sb.toString();
				lists3.set_item_size(Integer.parseInt(str));
			}
			else if("item_path".equals(qName)){
				str=sb.toString();
				lists3.set_item_path(str);
			}
			else if("hits".equals(qName)){
				str=sb.toString();
				lists3.set_hits(Integer.parseInt(str));
			}
			else if("qs_no".equals(qName)){
				str=sb.toString();
				lists3.set_qs_no(Integer.parseInt(str));
			}
			else if("qs_title".equals(qName)){
				str=sb.toString();
				lists3.set_qs_title(str);
			}
			else if("qs_round".equals(qName)){
				str=sb.toString();
				lists3.set_qs_round(Integer.parseInt(str));
			}
		}
		
		@Override
		public void characters(char[] ch, int start, int length) throws SAXException {
			super.characters(ch, start, length);
			sb.setLength(0);
			sb.append(ch,start,length);
		}
		
		public HashMap<String, ArrayList<IdealWorldcupLoadingDataElement>> getLists(){
			return lists;
		}

}
