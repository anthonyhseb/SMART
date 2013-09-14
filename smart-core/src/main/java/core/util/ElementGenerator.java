/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import org.w3c.dom.Element;

/**
 * Instances of this interface are passed to the buildDocument() method of the
 * Node class.
 * @author anthony
 */
public interface ElementGenerator<T> {
	public Element generate(T object, Element parent, Node<T> parentNode);
	/**
	 * Default element generator, uses the runtime class name as tag name,
	 * and toString() as text content.
	 */
	public class Default<T> implements ElementGenerator<T>{
		public static <T> Default<T> newInstance(Class<T> type){
			return new Default<T>();
		}
		public static Default newInstance(){
			return new Default();
		}
		@Override
		public Element generate(T object, Element parent, Node<T> parentNode) {
			Element newElement = parent.getOwnerDocument()
					.createElement(object.getClass().toString()
					.split("^.*\\.|<.*$")[1]);
			Element valueNode = parent.getOwnerDocument().createElement("value");
			valueNode.setTextContent(object.toString());
			newElement.appendChild(valueNode);
			parent.appendChild(newElement);
			return newElement;
		}	
	}
}
