/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import java.util.Iterator;
import java.util.LinkedList;

/**
 * Hidden implementation of the ResultSet interface.
 */
class ResultSetImpl implements ResultSet{
	private	LinkedList<Result>	results;
	private	Iterator<Result>	iterator;
	/** initialize the embedded iterator */
	public	void	startIterator() {iterator = results.iterator();}
	
	public	ResultSetImpl(){
		results		= new LinkedList<Result>();
	}
	
	public	void	add(Result result) {results.add(result);}

	@Override
	public	Iterator<Result> iterator(){ return results.iterator();}

	@Override
	public Result next() {return iterator.next();}

	@Override
	public boolean hasNext() {return iterator.hasNext();}

	@Override
	public int size() {return results.size();}

	@Override
	public void remove() {
		throw new UnsupportedOperationException("ResultSet.remove()");
	}

	@Override
	public void append(ResultSet resultSet) {
		results.addAll(((ResultSetImpl)resultSet).results);
	}
}
