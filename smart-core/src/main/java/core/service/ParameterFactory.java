/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import core.OntClasses;

/**
 * Builds Parameter objects.
 */
public class ParameterFactory {
	public static Parameter create(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.StaticRestInputParameter.get())){
			return new StaticInput(parameter, service);
		}
		if(parameter.hasOntClass(OntClasses.VariableRestInputParameter.get())){
			return new RestInput(parameter, service);
		}
		if(parameter.hasOntClass(OntClasses.RestOutputParameter.get())){
			return new RestOutput(parameter, service);
		}
		if(parameter.hasOntClass(OntClasses.LogicalInputParameter.get())){
			return new LogicalInput(parameter, service, null);
		}
		if(parameter.hasOntClass(OntClasses.LogicalOutputParameter.get())){
			return new LogicalOutput(parameter, service);
		}
		throw new IllegalArgumentException(
				parameter.getURI() + " is not a parameter");
	}
	
	public static LogicalParameter createLogicalParameter(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.LogicalInputParameter.get())){
			return new LogicalInput(parameter, service, null);
		}
		if(parameter.hasOntClass(OntClasses.LogicalOutputParameter.get())){
			return new LogicalOutput(parameter, service);
		}
		throw new IllegalArgumentException(
				parameter.getURI() + " is not a LogicalParameter");
	}

	public static StaticInput createStaticInput(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.StaticRestInputParameter.get())){
			return new StaticInput(parameter, service);
		}
		throw new IllegalArgumentException(
				parameter.getURI() +" is not a StaticInput");
	}
	
	public static RestInput createRestInput(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.VariableRestInputParameter.get())){
			return new RestInput(parameter, service);
		}
		throw new IllegalArgumentException(
				parameter.getURI() +" is not a RestInput");
	}
	
	public static RestOutput createRestOutput(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.RestOutputParameter.get())){
			return new RestOutput(parameter, service);
		}
		throw new IllegalArgumentException(
				parameter.getURI() +" is not a RestOutput");
	}
	
	public static LogicalInput createLogicalInput(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.LogicalInputParameter.get())){
			return new LogicalInput(parameter, service, null);
		}
		throw new IllegalArgumentException(
				parameter.getURI() +" is not a LogicalInput");
	}
	
	public static LogicalOutput createLogicalOutput(Individual parameter, Service service){
		if(parameter.hasOntClass(OntClasses.LogicalOutputParameter.get())){
			return new LogicalOutput(parameter, service);
		}
		throw new IllegalArgumentException(
				parameter.getURI() +" is not a LogicalOutput");
	}
}
