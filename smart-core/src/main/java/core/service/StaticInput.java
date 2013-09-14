/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Property;
import core.DataProperties;

/**
 * Wrapper for StaticRestInputParameter individuals.
 */
public class StaticInput implements Input{
	private	Individual	input;
	private String		name;
	private String		value;
	private Service		service;
	
	public StaticInput(Individual input, Service service){
		this.input = input;
		
		name	= input.getPropertyValue(DataProperties.parameterName.get())
				.asLiteral().getString();
		
		value	= input.getPropertyValue(DataProperties.parameterValue.get())
				.asLiteral().getString();
		this.service = service;
	}
	
	public String getName()	{return name;	}
	public String getValue(){return value;	}
	@Override
	public Service getService(){ return service; }
	@Override
	public Individual asIndividual(){
		return input;
	}
	@Override
	public Property getFromProperty(){return null;}
}
