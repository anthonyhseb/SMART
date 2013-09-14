/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.hp.hpl.jena.ontology.DatatypeProperty;

/**
 * Enumeration of the sub properties of topSeviceDataProperty.
 */
public enum DataProperties {
	mandatory				("mandatory"),
	topServiceDataProperty	("topServiceDataProperty"),
	topDomainDataProperty	("topDomainDataProperty"),
	parameterName			("parameterName"),
	resultXPath				("resultXPath"),
	restOutputXPath			("restOutputXPath"),
	rootOutputXPath			("rootOutputXPath"),
	endpoint				("endpoint"),
	parameterValue			("parameterValue")
	;
	
	
	private DatatypeProperty _property;
	
	private DataProperties(String rdfID){
		_property = Ontology.getModel().getDatatypeProperty(Ontology.baseUri + rdfID);
		if (_property == null)
			throw new NullPointerException("Cannot read property '" + rdfID + "'.");
	}
	
	public DatatypeProperty get() { return this._property; }
}
