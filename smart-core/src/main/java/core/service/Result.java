/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Service Result.
 */
public interface Result extends Iterator<Individual>{
	/** return an Iterator over the contained individuals. */
	public	Iterator<Individual>	iterator();
	/** return the number of individuals in the result. */
	public	int						size();
	/** return the list of individuals. */
	public	LinkedList<Individual>		toList();
}
