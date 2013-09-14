/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.query.Query;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Builder for Ask SPARQL queries.
 */
public class AskQueryBuilder {
	private static String defaultPrefixSet =
			"prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"	+
			"prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n"			+
			"prefix owl:     <http://www.w3.org/2002/07/owl#>\n"				+
			"prefix xsd:     <http://www.w3.org/2001/XMLSchema#>\n"				;
	
	private String prefixSet;
	private String askClause;
	private String remainder;
	
	/** add a prefix shared among all Instances */
	public static void addStaticPrefix(String shorthand, String iri){
		defaultPrefixSet += "prefix " + shorthand + ":\t" + "<" + iri + ">\n";
	}
	public AskQueryBuilder addPrefix(String shorthand, String iri){
		prefixSet = prefixSet + "prefix " + shorthand + ":\t" + "<" + iri + ">\n";
		return this;
	}

	public AskQueryBuilder addTriple(String triple) {
		String _adjusted = triple.trim();
		_adjusted = "\t" + _adjusted +
				(_adjusted.charAt(_adjusted.length() - 1) == '.'? "" : ".");
		askClause = askClause + _adjusted + "\n";
		return this;
	}
	public AskQueryBuilder addTriple(Resource subject, Property predicate, RDFNode object){
		String triple;
		triple	= "<" + subject.getURI()	+ "> ";
		triple += "<" + predicate.getURI()	+ "> ";
		if ( object.isURIResource()){
			triple += "<" + object.asResource().getURI() + ">.";
		}else{
			triple += object.asLiteral().getLexicalForm() + " .";
		}
		addTriple(triple);
		return this;
	}
	public AskQueryBuilder appendToWhereClause(String string){
		askClause = askClause + string.trim() + "\n";
		return this;
	}
	public AskQueryBuilder appendString(String string){
		remainder = remainder + string.trim() + "\n";
		return this;
	}
	public AskQueryBuilder(){
		prefixSet		= defaultPrefixSet;
		askClause		= "";
		remainder		= "";
	}
	@Override
	public String toString(){
		return
				prefixSet		+ "\n" +
				"ask {\n" +
					askClause +
				"}\n" +
				remainder;
	}
	/* build the query object */
	public Query build(){
		return QueryFactory.create(this.toString());
	}
	/* build and execute the query */
	public Boolean run(OntModel model) {
		QueryExecution _qexec = QueryExecutionFactory.create(build(), model);
		return _qexec.execAsk();
	}
	
	public static void main(String[] argv){
		AskQueryBuilder builder = new AskQueryBuilder();
		builder.addTriple("?thing a owl:Thing");
		System.out.println(builder);
	}
}
