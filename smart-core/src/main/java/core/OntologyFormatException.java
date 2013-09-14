/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

/**
 * Exception thrown when mandatory properties are missing in the ontology,
 * or information is not structured in the required way.
 */
public class OntologyFormatException extends RuntimeException{
	public OntologyFormatException(String message, Throwable reason){
		super(message, reason);
	}
	public OntologyFormatException(String message){super(message);}
}
