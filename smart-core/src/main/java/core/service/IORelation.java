/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Property;
import core.ObjectProperties;
import core.OntClasses;

/**
 * Wrapper for InputOutputRelation individuals.
 */
public class IORelation {
	private	Individual				iorel;
	private	Property				property;
	private	LogicalParameter		subject;
	private LogicalParameter		object;
	
	public	IORelation(Individual io_relation, Service service){
		iorel		= io_relation;
		property	= (Property) io_relation.getPropertyResourceValue(
				ObjectProperties.predicate.get()).as(Property.class);
		Individual sub = iorel.getPropertyResourceValue(
				ObjectProperties.subject.get()).as(Individual.class);
		Individual obj = iorel.getPropertyResourceValue(
				ObjectProperties.object.get()).as(Individual.class);
		
		if (io_relation.hasOntClass(OntClasses.InputToOutputRelation.get())){
			subject	= service.findLogicalInput(sub);
			object	= service.findLogicalOutput(obj);
		} else
		if (io_relation.hasOntClass(OntClasses.OutputToInputRelation.get())){
			subject = service.findLogicalOutput(sub);
			object	= service.findLogicalInput(obj);
		} else
			throw new IllegalArgumentException("io_relation not a relation");
		
	}
	
	public LogicalParameter getSubject()	{return subject;}
	public LogicalParameter getObject()		{return object;}
	public Property			getProperty()	{return property;}
}
