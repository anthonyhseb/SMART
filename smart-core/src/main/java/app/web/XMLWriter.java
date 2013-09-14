/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.web;
import core.service.ResultSet;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/**
 * Not used, not implemented.
 */
public class XMLWriter {
	/** Converts the specified ResultSet to XML */
	public static Document convert(ResultSet resultSet){
		Document output;
		try {
			output = DocumentBuilderFactory.newInstance()
		   .newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		
		System.out.println(output.getClass());
		

		return output;
	}
	
	public static void main(String args[]) throws TransformerConfigurationException, TransformerException{
		Document doc = convert(null);
		
		Element rootElement = doc.createElementNS("http://www.example.com/ontresults#", "results");
		doc.appendChild(rootElement);
		
		Element author = doc.createElement("author");
		rootElement.appendChild(author);
		author.appendChild(doc.createTextNode("Anthony"));
		
		
		// Prepare the DOM document for writing
		DOMSource source = new DOMSource(doc);

		// Prepare the output file
		//File file = new File(filename);
		//Result result = new StreamResult(file);
		Result result = new StreamResult(System.out);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
	}
}
