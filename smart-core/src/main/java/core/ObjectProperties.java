/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.hp.hpl.jena.ontology.ObjectProperty;
/**
 *
 * Enumeration of all sub properties of topServiceObjectProperty.
 */
public enum ObjectProperties {
	
	restOutputOf				("restOutputOf"),
	type						("type"),
	fromLogicalOutput			("fromLogicalOutput"),
	hasRestOutput				("hasRestOutput"),
	object						("object"),
	rootOutputOf				("rootOutputOf"),
	fromDataProperty			("fromDataProperty"),
	fromObjectProperty			("fromObjectProperty"),
	toInput						("toInput"),
	rootParameterOf				("rootParameterOf"),
	predicate					("predicate"),
	subOutputOf					("subOutputOf"),
	hasRestInput				("hasRestInput"),
	subInputOf					("subInputOf"),
	rootInputOf					("rootInputOf"),
	subParameterOf				("subParameterOf"),
	subject						("subject"),
	fromLogicalInput			("fromLogicalInput"),
	toRestParameter				("toRestParameter"),
	topServiceObjectProperty	("topServiceObjectProperty"),
	topDomainObjectProperty		("topDomainObjectProperty"),
	toOutput					("toOutput"),
	hasRootParameter			("hasRootParameter"),
	hasRootInput				("hasRootInput"),
	hasRootOutput				("hasRootOutput"),
	hasIORelation				("hasIORelation"),
	outputFormat				("outputFormat")
	;
	
	private ObjectProperty _property;
	
	private ObjectProperties(String rdfID){
		_property = Ontology.getModel().getObjectProperty(Ontology.baseUri + rdfID);
		if (_property == null)
			throw new NullPointerException("Cannot read property '" + rdfID + "'.");
	}
	
	public ObjectProperty get() { return this._property; }
}
