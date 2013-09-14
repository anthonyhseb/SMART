/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import java.util.HashMap;

/**
 * A HashMap of services indexed by URI.
 */
public class ServiceRegistry {
	private HashMap<String, Service>	registry;

	/** creates an empty ServiceRegistry */
	public ServiceRegistry() {
		registry = new HashMap<String, Service>();
	}
	/** adds a service to the registry */
	public void register(Service service){
		Individual individual = service.asIndividual();
		String key = individual.isAnon()?
				individual.getId().getLabelString() : individual.getURI();
		registry.put(key, service);
	}
	/** returns the service with the the specified IRI */
	public Service lookup(String IRI){
		return registry.get(IRI);
	}
}
