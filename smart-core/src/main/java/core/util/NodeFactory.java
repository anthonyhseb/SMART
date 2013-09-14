/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

/**
 * Creates Node objects.
 */
public class NodeFactory<C> {
	/** creates an empty Node object. */
	public static <T> Node<T> create(Class<T> type, T object){
		return new NodeImpl<T>(object);
	}
	
	public Node create(C object){
		return new NodeImpl<C>(object);
	}
}
