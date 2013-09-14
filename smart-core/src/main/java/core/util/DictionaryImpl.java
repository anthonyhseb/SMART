/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * Hidden implementation of the Dictionary interface, based on a tree structure
 * , each node is a Dictionary
 */
class DictionaryImpl<T> implements Dictionary<T>{

	private DictionaryImpl	node[];
	private T				payload;
	private String			key;
	/** converts an alphabetic character to the corresponding array index. */
	private static int toIndex(char c) {
		int ret	= c - 'a';
		ret *=2;
		
		if ( (ret < 0) || (ret > 50) ){
			ret = c - 'A';
			ret = ret*2 +1;
		}
		
		if ( (ret < 0) || (ret > 51) ){
			ret = 53; //other char
		}
		return (ret + 1)%53;
	}
	/** build a new DictionaryImpl object */
	public DictionaryImpl() {
		node = new DictionaryImpl[53];
		payload = null;
		key = null;
//		for (int i = 0; i < 26; i++) {
//			node[i] = null;
//		}
	}
	/** Inserts the node at the right place in the tree, creates new nodes if
	 * necessary, private recursive method requiring an additional parameter.
	 */
	private void insert(String key, T object, String remainder) {
		if (remainder.length() == 0) {
			payload = object;
			this.key = key;
			//System.out.println("string ended");
			return;
		}
		int curIndex = toIndex(remainder.charAt(0));
		if (node[curIndex] == null) {
			node[curIndex] = new DictionaryImpl();
		}
		//System.out.println(key.substring(1));
		node[curIndex].insert(key, object, remainder.substring(1));
	}
	/** returns the subtree at the end of the specified string. */
	@Override
	public DictionaryImpl lookup(String substring) {
		DictionaryImpl output = this;
		for (int i = 0; i < substring.length(); i++) {
			int curIndex = toIndex(substring.charAt(i));
			if (output == null) return null;
			output = output.node[curIndex];
		}
		return output;
	}
	/** returns the node for the specified character (required by the 
	 * DictionaryIterator class.
	 */
	public	DictionaryImpl<T>	get(char c){
		return node[toIndex(c)];
	}
	public	DictionaryImpl<T>	get(int index){
		return node[index];
	}
	
	/** gets the object at the root (required by the DictionaryIterator class. */
	public	T	get(){return payload;}
	@Override
	/** gets the object with the given key */
	public	T	get(String key){
		DictionaryImpl<T> curDictionary = this;
		for (int i = 0; i < key.length(); i++) {
			int curIndex = toIndex(key.charAt(i));
			if (curDictionary == null) return null;
			curDictionary = curDictionary.node[curIndex];
		}
		if (curDictionary == null) return null;
		return curDictionary.payload;
	}
	/** converts the dictionary to a LinkedList */	
	public LinkedList<T> toList() {
		LinkedList<T> output = new LinkedList<T>();
		if (payload != null) {
			output.add(payload);
		}
		for (int i = 0; i < 52; i++) {
			if (node[i] == null) {
				continue;
			}
			output.addAll(node[i].toList());
		}
		return output;
	}
	/** Inserts the entry at the right place in the tree */
	@Override
	public void insert(String key, T object) {
		insert(key, object, key);
	}

	private static String randomKey(int length){
		Random rand = new Random();
		String output = "";
		for (int i=0 ; i<length ; i++) {
			output += Character.toString((char)(rand.nextInt(26) + 'a'));
		}
		return output;
	}
	/** returns an iterator for this Dictionary */
	@Override
	public Iterator<T> iterator(){
		return new DictionaryIterator<T>(this);
	}
	public static void main(String args[]) {

		Dictionary<Integer> dic = new DictionaryImpl<Integer>();
		String lastKey = null;
		for (int i=0 ; i<50000 ; i++){
			lastKey = randomKey(12);
			dic.insert(lastKey, i);
		}
		System.out.println("insert complete");
		for (int i=0 ; i<100000 ; i++ ){
				dic.lookup(lastKey);
		}
		System.out.println("Done!");

		//System.out.println(toIndex("a".charAt(0)));
		dic = new DictionaryImpl();
		dic.insert("aa", 1);
		dic.insert(" aaaa", 2);
		dic.insert("bntz", 3);
		dic.insert("Bnth", 4);
		dic.insert("z", 5);
		dic.insert("Zz", 6);
		dic.insert("_z", 7);

//		System.out.println(
//				dic.node[toIndex('a')].node[toIndex('n')].node[toIndex('t')].node[toIndex('h')].key
//				+ dic.node[toIndex('a')].node[toIndex('n')].node[toIndex('t')].node[toIndex('h')].payload.toString());
		Dictionary subdic = dic.lookup("!");
		if (subdic == null) {
			throw new RuntimeException();
		}

		System.out.println(dic.get("anzasdf"));
		
		Iterator<Integer> resultIt = dic.iterator();

		while (resultIt.hasNext()) {
			System.out.println(resultIt.next());
		}
		
	}
}
