/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import core.Ontology;

/**
 * A wrapper for a property or class used in a Query
 */
public class Concept {
	private	Resource	concept;
	private	boolean		is_class = false;
	/**
	 * Builds the Concept object from it's URI reference in the ontology
	 *
	 */
	public Concept(String uri){
		Resource resource = Ontology.getModel().getResource(uri);
		if (resource == null) throw new InvalidConceptException(uri);
		if(!resource.canAs(OntProperty.class)) is_class = true;
		if(!((is_class)||(resource.canAs(OntProperty.class)))){
			throw new InvalidConceptException(resource);
		}
		this.concept = resource;
		
	}
	/**
	 * Builds a Concept form the specified resource
	 * @param concept 
	 */
	public Concept(Resource concept){
		if(!concept.canAs(OntProperty.class)) is_class = true;
		if(!((is_class)||(concept.canAs(OntProperty.class)))){
			throw new InvalidConceptException(concept);
		}
		this.concept = concept;
	}
	/**
	 * Casts the Concept instance to the specified RDFNode subtype 
	 */
	public	<T extends RDFNode>	 T as(Class<T> type){ return concept.as(type); }
	/**
	 * Returns the Resource object represented by this Concept
	 * @return 
	 */
	public	Resource asResource(){ return concept;}
	/**
	 * Exception thrown when the constructor of Concept fails.
	 */
	private static class InvalidConceptException extends RuntimeException {
		public InvalidConceptException(Resource concept) {
			super(concept.toString());
		}
		public InvalidConceptException(String uri){
			super(uri);
		}
	}
	/** Answers true if the Concept is a Class. */
	public	boolean	isClass() { return is_class; }
	/** Answers true if the Concept is a resource. */
	public	boolean isProperty() { return !is_class; }
	
	/** Casts the Concept as an OntClass */
	public	OntClass	asClass()	{ return concept.as(OntClass.class);	}
	/** Casts the Concept as an OntProperty */
	public	OntProperty	asProperty(){ return concept.as(OntProperty.class);	}
}
