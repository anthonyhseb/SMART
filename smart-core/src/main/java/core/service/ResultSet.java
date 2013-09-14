/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import java.util.Iterator;

/**
 * A set of Results, namely a list of lists of individuals.&nbsp;
 * the lists of individuals are the rootOutputs in the same order they appeared
 * the service object.
 * 
 */
public interface ResultSet extends Iterator<Result>{
	/** return an iterator over the result set. */
	public	Iterator<Result>	iterator();
	/** return the number of Result objects */
	public	int					size();
	/** merges two result sets. */
	public	void				append(ResultSet resultSet);
}
