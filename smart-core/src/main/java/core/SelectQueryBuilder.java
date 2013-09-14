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
import com.hp.hpl.jena.query.ResultSet;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

/**
 * Builder for Select SPARQL queries.
 */
public class SelectQueryBuilder {
	private static String defaultPrefixSet =
			"prefix rdf:     <http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"	+
			"prefix rdfs:    <http://www.w3.org/2000/01/rdf-schema#>\n"			+
			"prefix owl:     <http://www.w3.org/2002/07/owl#>\n"				+
			"prefix xsd:     <http://www.w3.org/2001/XMLSchema#>\n"				;
	
	private String prefixSet;
	private String selectClause;
	private String fromClause;
	private String whereClause;
	private String remainder;
	/** adds a shared prefix for to all instances. */
	public static void addStaticPrefix(String shorthand, String iri){
		defaultPrefixSet += "prefix " + shorthand + ":\t" + "<" + iri + ">\n";
	}
	
	public SelectQueryBuilder addPrefix(String shorthand, String iri){
		prefixSet = prefixSet + "prefix " + shorthand + ":\t" + "<" + iri + ">\n";
		return this;
	}
	public SelectQueryBuilder setColumns(String columns){
		selectClause = "select "+ columns;
		return this;
	}
	public SelectQueryBuilder setFromClause(String fullFromClause){
		fromClause = fullFromClause;
		return this;
	}
	public SelectQueryBuilder addWhereTriple(String triple) {
		String _adjusted = triple.trim();
		_adjusted = "\t" + _adjusted +
				(_adjusted.charAt(_adjusted.length() - 1) == '.'? "" : ".");
		whereClause = whereClause + _adjusted + "\n";
		return this;
	}
	public SelectQueryBuilder addWhereTriple(Resource subject, Property predicate, RDFNode object){
		String triple;
		triple	= "<" + subject.getURI()	+ "> ";
		triple += "<" + predicate.getURI()	+ "> ";
		if ( object.isURIResource()){
			triple += "<" + object.asResource().getURI() + ">.";
		}else{
			triple += object.asLiteral().getLexicalForm() + " .";
		}
		addWhereTriple(triple);
		return this;
	}
	
	public SelectQueryBuilder addWhereTriple(String subjectVar, Property predicate, String objectVar){
		String triple;
		triple	= "?" + subjectVar.trim() + ' ';
		triple += "<" + predicate.getURI()	+ "> ";
		triple += "?" + objectVar.trim() + '.';
		addWhereTriple(triple);
		return this;
	}
	
	public SelectQueryBuilder addWhereTriple(String subjectVar, Property predicate, RDFNode object){
		String triple;
		triple	= "?" + subjectVar.trim() + ' ';
		triple += "<" + predicate.getURI()	+ "> ";
		if ( object.isURIResource()){
			triple += "<" + object.asResource().getURI() + ">.";
		}else{
			triple += object.asLiteral().getLexicalForm() + " .";
		}
		addWhereTriple(triple);
		return this;
	}
	
	public SelectQueryBuilder addWhereTriple(Resource subject, Property predicate, String objectVar){
		String triple;
		triple	= "<" + subject.getURI()	+ "> ";
		triple += "<" + predicate.getURI()	+ "> ";
		triple += "?" + objectVar.trim() + '.';
		addWhereTriple(triple);
		return this;
	}
	
	public SelectQueryBuilder addWhereTriple(Resource subject, String predicateVar, RDFNode object){
		String triple;
		triple	= "<" + subject.getURI()	+ "> ";
		triple += "?" + predicateVar.trim();
		if ( object.isURIResource()){
			triple += "<" + object.asResource().getURI() + ">.";
		}else{
			triple += object.asLiteral().getLexicalForm() + " .";
		}
		addWhereTriple(triple);
		return this;
	}
	public SelectQueryBuilder appendToWhereClause(String string){
		whereClause = whereClause + string.trim() + "\n";
		return this;
	}
	public SelectQueryBuilder appendString(String string){
		remainder = remainder + string.trim() + "\n";
		return this;
	}
	public SelectQueryBuilder(){
		prefixSet		= defaultPrefixSet;
		selectClause	= "select *";
		fromClause		= "";
		whereClause		= "";
		remainder		= "";
	}
	@Override
	public String toString(){
		return
				prefixSet		+ "\n" +
				selectClause	+ "\n" +
				fromClause		+ "\n" +
				"where {\n" +
					whereClause +
				"}\n" +
				remainder;
	}
	/** builds the query object */
	public Query build(){
		return QueryFactory.create(this.toString());
	}
	/** builds and executes the query */
	public ResultSet run(OntModel model) {
		QueryExecution _qexec = QueryExecutionFactory.create(build(), model);
		return _qexec.execSelect();
	}
	
	public static void main(String[] argv){
		SelectQueryBuilder builder = new SelectQueryBuilder();
		builder.addWhereTriple("?thing a owl:Thing");
		System.out.println(builder);
	}
}
