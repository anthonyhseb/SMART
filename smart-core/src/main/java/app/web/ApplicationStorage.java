/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app.web;

import app.SuggestionGenerator;
import com.hp.hpl.jena.ontology.OntModel;
import core.service.ServiceRegistry;

/**
 * Holds the resources that need to be stored in the Session scope or Application scope.
 */
public class ApplicationStorage {
	public ServiceRegistry		registry;
	public SuggestionGenerator	suggestionGenerator;
	public OntModel				model;
	
	public static ApplicationStorage instance;
//	public ApplicationStorage(
//			ServiceRegistry registry,
//			Dictionary properties,
//			Dictionary classes){
//		this.registry = registry;
//		this.properties = properties;
//		this.classes = classes;
//	}
}
