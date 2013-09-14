/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import core.util.DictionaryImpl;
import java.util.Iterator;
import java.util.Stack;

/**
 * Iterator of the Dictionary structure.
 */
class DictionaryIterator<T> implements Iterator{
	private Stack<DictionaryImpl<T>>	stack;
	private Stack<Integer>			curChild;
	private boolean						noReturn;
	private DictionaryImpl<T>				nextNode;
	private DictionaryImpl<T>				rootNode;
	

	/** builds an iterator for the specified Dictionary */
	public DictionaryIterator(DictionaryImpl<T> dictionary){
		if (dictionary == null)
			throw new IllegalArgumentException("dictionary can't be null");
		rootNode	= dictionary;
		stack		= new Stack<DictionaryImpl<T>>();
		curChild	= new Stack<Integer>();
		stack.push(rootNode);
		curChild.push(0);
		noReturn	= false;
		findNext();
	}
	
//	private static char int2char(int index){
//		return (char)((index&1) == 0 ?
//				'a' + index : 'A' + index);
//	}
	
	/** gets the index of the first sub-node after the curChild value of
	 * the current node that isn't null. */
	private int findNextChild(){
		int curIndex = curChild.peek();
		if (curIndex > 52 ) return -1;
		while(stack.peek().get(curIndex) == null){
			if (curIndex == 52) return -1;
			curIndex++;
		}
		return curIndex;
	}
	/** finds the next non-empty node using depth first search */
	private void findNext(){
		loop1:while(!stack.isEmpty()){

			// if node has payload set nextNode unless already done (noReturn true)
			if((stack.peek().get() != null)&&(!noReturn)) {
				nextNode = stack.peek();
				noReturn = true;
				return;
			}
			// find the next non null child
			int nextChild = findNextChild();
			if (nextChild == -1) {
				stack.pop();
				if(stack.isEmpty()){nextNode = null; return;}
				curChild.pop();
				curChild.push(curChild.pop()+1);
				noReturn = true;
				continue loop1;
			}

			curChild.pop();
			curChild.push(nextChild);
			stack.push(stack.peek().get(nextChild));
			curChild.push(0);
			noReturn = false;
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
		DictionaryImpl<T> output = nextNode;
		findNext();
		return output.get();
	}
	/** not implemented. */
	@Override
	public void remove() {
		throw new UnsupportedOperationException("Not supported yet.");
	}
}
