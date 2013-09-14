/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.hp.hpl.jena.ontology.OntClass;

/**
 * Enumeration of all subclasses of ServiceThing.
 */
public enum OntClasses{
	DomainClass					("DomainClass"),
	DomainThing					("DomainThing"),
	RestOutputParameter			("RestOutputParameter"),
	Service						("Service"),
	VariableRestInputParameter	("VariableRestInputParameter"),
	ServiceThing				("ServiceThing"),
	OutputToInputRelation		("OutputToInputRelation"),
	OutputParameter				("OutputParameter"),
	DomainObjectProperty		("DomainObjectProperty"),
	RootInputParameter			("RootInputParameter"),
	InputOutputRelation			("InputOutputRelation"),
	DomainDataProperty			("DomainDataProperty"),
	SubInputParameter			("SubInputParameter"),
	LogicalOutputParameter		("LogicalOutputParameter"),
	SubOutputParameter			("SubOutputParameter"),
	LogicalInputParameter		("LogicalInputParameter"),
	StaticRestInputParameter	("StaticRestInputParameter"),
	RestParameter				("RestParameter"),
	InputParameter				("InputParameter"),
	DomainProperty				("DomainProperty"),
	VariableRestParameter		("VariableRestParameter"),
	Parameter					("Parameter"),
	RootOutputParameter			("RootOutputParameter"),
	RestInputParameter			("RestInputParameter"),
	LogicalParameter			("LogicalParameter"),
	InputToOutputRelation		("InputToOutputRelation"),
	SISOService					("SISOService"),
	OutputFormat				("OutputFormat")
	;
	
	
	private OntClass _class;
	
	private OntClasses(String rdfID){
		_class = Ontology.getModel().getOntClass(Ontology.baseUri + rdfID);
		if (_class == null)
			throw new NullPointerException("Cannot read class '" + rdfID + "'.");
	}
	
	public OntClass get() { return this._class; }
}
