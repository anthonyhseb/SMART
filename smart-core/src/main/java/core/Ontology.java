/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import org.mindswap.pellet.jena.PelletReasonerFactory;

/**
 *	Details about the ontology.
 */
public class Ontology {
	public static final String baseUri =
			"http://www.semanticweb.org/anthony/services.owl#";
	public static final String rdfNS =
			"http://www.w3.org/1999/02/22-rdf-syntax-ns#";
	public static final String rdfsNS = 
			"http://www.w3.org/2000/01/rdf-schema#";
	public static final String owlNS =
			"http://www.w3.org/2002/07/owl#";
	public static final String xsdNS = 
			"http://www.w3.org/2001/XMLSchema#";
	
	private static OntModel _model;
	
	static {
		SelectQueryBuilder	.addStaticPrefix("svc", baseUri);
		AskQueryBuilder		.addStaticPrefix("svc", baseUri);
	}
	
	/**
	 * Reads the ontology file, starts the reasoner.
	 */
	public static void initialize(){
		_model = ModelFactory.createOntologyModel(PelletReasonerFactory.THE_SPEC, null);
		_model.read(Thread.currentThread().getContextClassLoader().getResourceAsStream("services.owl"), "RDF/XML");
	}
	
	/**
	 * Uses the specified model instead of reading the ontology from reading it form the file.
	 * @param model 
	 */
	public static void initialize(OntModel model){
		_model = model;
		if (_model == null) initialize();
	}
	/** get the OntModel object of the ontology. */
	public static OntModel getModel(){return _model;}
	
	/** return the XSDDatatype object with the given URI. */
	public static XSDDatatype parseDatatype(String URI){
		String _uri = URI.trim();
		if(!_uri.startsWith(xsdNS))
			throw new IllegalArgumentException("Invalid XSD Datatype: " + URI);
		return new XSDDatatype(_uri.split("#")[1]);
	}
}
