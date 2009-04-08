package org.purc.purcforms.server.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.Enumeration;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.IOUtils;
import org.purc.purcforms.server.xpath.XPathExpression;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 * 
 * @author daniel
 *
 */
public class XmlUtil {

	public static String getRequestAsString(HttpServletRequest request) throws java.io.IOException {
		BufferedReader requestData = new BufferedReader(new InputStreamReader(request.getInputStream()));
		StringBuffer stringBuffer = new StringBuffer();
		String line;
		try{
			while ((line = requestData.readLine()) != null)
				stringBuffer.append(line);
		} 
		catch (Exception e){e.printStackTrace();}
		
		return stringBuffer.toString();
		//return IOUtils.toString(request.getInputStream());
	}
	
	/**
	 * Converts a document to its text representation.
	 * 
	 * @param doc - the document.
	 * @return - the text representation of the document.
	 */
	public static String doc2String(Document doc){
		try{
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			//transformer.setOutputProperty(OutputKeys.INDENT, "yes");
			
			StringWriter outStream  = new StringWriter();
			DOMSource source = new DOMSource(doc);
			StreamResult result = new StreamResult(outStream);
			transformer.transform(source, result);
			return outStream.getBuffer().toString();
		}
		catch(Exception e){
			e.printStackTrace();
		}
		
		return null;
	}
	
	public static Document fromString2Doc(String xml){
		try{
			return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(IOUtils.toInputStream(xml));
		}
		catch(Exception ex){
			ex.printStackTrace();
		}
		return null;
	}
	
	public static String getNodeValue(Document doc, String xpath){
		//xpath = new String(xpath.toCharArray(), 1, xpath.length()-1);
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos > 0){
			attributeName = xpath.substring(pos+1,xpath.length());
			xpath = xpath.substring(0,pos-1);
		}
		
		XPathExpression xpls = new XPathExpression(doc.getDocumentElement(), xpath);
		Vector result = xpls.getResult();
		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				if(pos > 0) //Check if we are to set attribute value.
					return ((Element) obj).getAttribute(attributeName);
				else
					return ((Element) obj).getTextContent();
			}
		}
		return null;
	}
	
	public static String getDescriptionTemplate(Element node, String template){
		if(template == null || template.trim().length() == 0)
			return null;
		
//		String s = "Where does ${name}$ come from?";
		String f,v,text = template;

		int startIndex,j,i = 0;
		do{
			startIndex = i; //mark the point where we found the first $ character.

			i = text.indexOf("${",startIndex); //check the opening $ character
			if(i == -1)
				break; //token not found.

			j = text.indexOf("}$",i+1); //check the closing $ character
			if(j == -1)
				break; //closing token not found. possibly wrong syntax.

			f = text.substring(0,i); //get the text before token
			v = getValue(node,text.substring(i+2, j)); //append value of token.

			f += (v == null) ? "" : v;
			f += text.substring(j+2, text.length()); //append value after token.

			text = f;

		}while (true); //will break out when dollar symbols are out.

		return text;
	}
	
	private static String getValue(Element node, String xpath){
		int pos = xpath.lastIndexOf('@'); String attributeName = null;
		if(pos > 0){
			attributeName = xpath.substring(pos+1,xpath.length());
			xpath = xpath.substring(0,pos-1);
		}
		
		XPathExpression xpls = new XPathExpression(node, xpath);
		Vector result = xpls.getResult();

		for (Enumeration e = result.elements(); e.hasMoreElements();) {
			Object obj = e.nextElement();
			if (obj instanceof Element){
				if(pos > 0) //Check if we are to set attribute value.
					return ((Element) obj).getAttribute(attributeName);
				else
					return ((Element) obj).getTextContent();
			}
		}
		
		return null;
	}
	
	public static String getDescriptionTemplate(String xml) throws Exception{
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db = dbf.newDocumentBuilder();
		Document doc = db.parse(IOUtils.toInputStream(xml));
		
		String descTemplate = doc.getDocumentElement().getAttribute("description-template");
		return getDescriptionTemplate(doc.getDocumentElement(),descTemplate);
	}
	
	public static Document getNewDocument() throws Exception{
		return DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
	}
}
