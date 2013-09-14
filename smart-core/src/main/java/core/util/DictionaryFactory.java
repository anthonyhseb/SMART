/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

/**
 * Creates Dictionary objects.
 */
public class DictionaryFactory {
	/** creates an empty Dictionary object. */
	public static <T> Dictionary<T> create(Class<T> type){
		return new DictionaryImpl<T>();
	} 
}
