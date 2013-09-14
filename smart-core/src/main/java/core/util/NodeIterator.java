/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import java.util.Iterator;
import java.util.Stack;

/**
 * Iterator of the Dictionary structure.
 */
class NodeIterator<T> implements Iterator{
	private Stack<NodeImpl<T>>			stack;
	private Stack<Iterator<NodeImpl<T>>>itstack;
	private boolean						backtracking;
	private NodeImpl<T>					nextNode;
	private NodeImpl<T>					rootNode;
	

	/** builds an iterator for the specified Dictionary */
	public NodeIterator(NodeImpl<T> node){
		if (node == null)
			throw new IllegalArgumentException("node can't be null");
		rootNode	= node;
		stack		= new Stack<NodeImpl<T>>();
		itstack	= new Stack<Iterator<NodeImpl<T>>>();
		stack.push(rootNode);
		itstack.push(rootNode.listChildren().iterator());
		backtracking	= false;
		findNext();
	}
	
	/** finds the next non-empty node using depth first search */
	private void findNext(){
		loop1:while(!stack.isEmpty()){

			// if node has payload set nextNode unless already done (noReturn true)
			if((!backtracking)&&(stack.peek().get() != null)) {
				nextNode = stack.peek();
				backtracking = true;
				return;
			}
			
			if (!itstack.peek().hasNext()) {
				stack.pop();
				if(stack.isEmpty()){nextNode = null; return;}
				itstack.pop();
				backtracking = true;
				continue loop1;
			}

			NodeImpl<T> newChild = itstack.peek().next();
			stack.push(newChild);
			itstack.push(newChild.listChildren().iterator());
			backtracking = false;
		}
		nextNode = null;
	}
	/** answers whether there are more elements in the Dictionary. */
	@Override
	public boolean hasNext() {
		return nextNode != null;
	}
	/** returns the next object. */
	@Override
	public T next() {
		NodeImpl<T> output = nextNode;
		findNext();
		return output.get();
	}
	/** not implemented. */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
