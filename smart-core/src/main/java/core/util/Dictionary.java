/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import java.util.Iterator;
import java.util.List;

/**
 * Indexed container allowing lookup with partial keys.
 */
public interface Dictionary<T> {
	/** inserts a key value pair into the Dictionary. */
	public	void insert(String key, T object);
	/** returns a dictionary containing the entries where the the key starts
	 * with the specified string.
	 */
	public	Dictionary<T> lookup(String str);
	/**
	 * returns a iterator over the dictionary.
	 */
	public	Iterator<T> iterator();
	/** returns the object with the given key, or null. */
	public	T	get(String key);
	
	/** returns the objects in the dictionary sorted by key. */
	public	List<T> toList();
}
