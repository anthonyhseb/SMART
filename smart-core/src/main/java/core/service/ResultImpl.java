/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Hidden implementation of the Result interface.
 */
class ResultImpl implements Result{
	private	LinkedList<Individual>	rootIndividuals;
	private	Iterator<Individual>	iterator;
	/** initializes the embedded iterator */
	public	void	startIterator()	{ iterator = rootIndividuals.iterator();}
	
	public	LinkedList<Individual> toList() { return rootIndividuals; }
	
	public	ResultImpl(){
		rootIndividuals = new LinkedList<Individual>();
	}
	
	public	void add(Individual individual){ rootIndividuals.add(individual);}

	@Override
	public Iterator<Individual> iterator() {
		return rootIndividuals.iterator();
	}

	@Override
	public Individual next() { return iterator.next(); }

	@Override
	public boolean hasNext() { return iterator.hasNext(); }

	@Override
	public void remove() {
		throw new UnsupportedOperationException("Result.remove()");
	}

	@Override
	public int size() { return rootIndividuals.size(); }
}
