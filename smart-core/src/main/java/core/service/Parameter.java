/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Property;

/**
 * interface for Parameter individuals.
 */
public interface Parameter {
	Individual	asIndividual();
	Service		getService();
	/** return the property that connects this parameter to its parent */
	Property	getFromProperty();
}
