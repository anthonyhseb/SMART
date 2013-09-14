/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import java.util.LinkedList;
import org.w3c.dom.Element;

/**
 *
 * @author anthony
 */
public interface ElementListGenerator<T> {
	public LinkedList<Element> generate(T object, Element parent, Node<T> parentNode);
}
