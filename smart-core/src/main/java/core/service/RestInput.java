/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import core.DataProperties;
import core.ObjectProperties;
import core.OntClasses;
import core.Ontology;
import core.PropertyPath;
import java.util.LinkedList;
import java.util.List;

/**
 * Wrapper for VariableRestInputParamter individuals.
 */
public class RestInput implements Input{
	private Individual		restInput;
	private PropertyPath	path;
	private String			name;
	private Service			service;
	private DatatypeProperty	fromProperty;
	
	public RestInput(Individual restInput, Service service){
		this.restInput = restInput;
		this.service = service;
		this.fromProperty = restInput.getPropertyResourceValue(
				ObjectProperties.fromDataProperty.get())
				.as(DatatypeProperty.class);
		
		//build path from root
		LinkedList<Property> properties = new LinkedList<Property>();
		properties.add(
				restInput.getPropertyValue(ObjectProperties.fromDataProperty.get())
				.as(Property.class));
		Individual curInput =
				restInput.getPropertyValue(ObjectProperties.fromLogicalInput.get())
				.as(Individual.class);
		while (!curInput.hasOntClass(OntClasses.RootInputParameter.get().asResource())){
			Individual parentInput =
					curInput.getPropertyValue(ObjectProperties.fromLogicalInput.get())
					.as(Individual.class);
			properties.addFirst(
					curInput.getPropertyValue(ObjectProperties.fromObjectProperty.get())
					.as(Property.class));
			curInput = parentInput;
		}
		path = new PropertyPath(Ontology.getModel(), properties);
		
		name = this.restInput
				.getPropertyValue(DataProperties.parameterName.get())
				.asLiteral()
				.getString();
	}
	/** extract value from the specified individual. */
	public String evaluate(Individual rootInput){
		List<RDFNode> nodes = path.listObjects(rootInput);
		if (nodes.isEmpty()) return "";
		return	nodes.get(0).asLiteral().getString();
	}
	/** return the name of the rest parameter. */
	public String getName(){
		return name;
	}
	
	@Override
	public Individual asIndividual(){
		return restInput;
	}
	@Override
	public DatatypeProperty getFromProperty(){return fromProperty;}
	@Override
	public Service	getService(){return service;}
	public static void main(String args[]){
		RestInput test = 
				new RestInput(Ontology.getModel().getIndividual(Ontology.baseUri + "GeoNamesSearchQueryString"), null);
		System.out.println(test.getName());
	}
}
