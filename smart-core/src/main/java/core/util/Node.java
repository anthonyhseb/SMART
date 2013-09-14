/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.Document;
/**
 * Directed Graph Node
 * @author anthony
 * @param <T> Data Type
 */
public interface Node<T> {
	public	void	appendChild(T object);
	public	void	appendChild(Node<T> node);
	public	void	appendAllChildNodes(Collection<Node<T>> nodes);
	public	void	appendAllChildren(Collection<T>	children);
	public	List<Node<T>>	listChildNodes();
	public	T		get();
	public	void	set(T obj);
	public	Iterator<T> iterator();
	public	Document	buildDocument(ElementGenerator<T> generator, String rootNodeName, String rootNodeNs);
	public	Document	buildDocument(ElementListGenerator<T> generator, String rootNodeName, String rootNodeNs);
	public	void	traverse(NodeVisitor<T> visitor, Object initialState);
}
