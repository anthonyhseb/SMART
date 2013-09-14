/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.ResultSet;
import core.ObjectProperties;
import core.OntClasses;
import core.Ontology;
import core.SelectQueryBuilder;
import core.service.Service;
import core.service.ServiceRegistry;
import java.util.Iterator;

/**
 * Defines methods required for compiling Mashup objects, initializes all Service
 * objects available in the ontology.
 */
public class QueryEngine {
	
	private	static ServiceRegistry registry;
	
	/** set the ServiceRegistry object used. */
	public	static void setRegistry(ServiceRegistry registry){
		QueryEngine.registry = registry;
	}
	
	public	static ServiceRegistry getRegistry (){return registry;}
	/** populates the registry with the services in the ontology. */
	public	static void	loadServices(){
		registry = new ServiceRegistry();
		Iterator<Individual> it =
				Ontology.getModel().listIndividuals(OntClasses.SISOService.get());
		while(it.hasNext()){
			registry.register(new Service(it.next().getURI()));
		}
	}
	/** finds a service with the specified input and output classes and
	 * an IORelation with the given property.
	 */
	public	static Service matchService(OntClass inputType, OntProperty property, OntClass outputType) {
		// find a service with single rootInput of type 'type'
		// and with property 'property' available in the output
		// the sameAs property must be considered
		// test Query.compile() then Mashup.execute() after this.
		
		if(registry == null)
			throw new RuntimeException("Registry not initialized!");
		OntClass _inputType = inputType == null ?
				property.getDomain().asClass() : inputType;
		
		OntClass _outputType = outputType == null ?
				property.getRange().asClass() : outputType;
		
		SelectQueryBuilder builder = new SelectQueryBuilder();
		builder.
		setColumns("distinct ?service").
		addWhereTriple(
				_inputType,
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				property.getDomain()).
		addWhereTriple(
				_outputType,
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				property.getRange()).
		addWhereTriple(
				"service",
				Ontology.getModel().getProperty(Ontology.rdfNS +	"type"),
				OntClasses.SISOService.get()).
		addWhereTriple(
				"service",
				ObjectProperties.hasRootInput.get(),
				"rootInput").
		addWhereTriple(
				"service",
				ObjectProperties.hasRootOutput.get(),
				"rootOutput").
		addWhereTriple(
				"rootInput",
				ObjectProperties.type.get(),
				"rootInputClass").
		addWhereTriple(
				"rootOutput",
				ObjectProperties.type.get(),
				"rootOutputClass").
		addWhereTriple(
				"rootInputClass",
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				_inputType
				).
		addWhereTriple(
				"rootOutputClass",
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				_outputType
				).
		addWhereTriple(
				property,	//switched
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subPropertyOf"),
				"property").
		addWhereTriple(
				"service",
				ObjectProperties.hasIORelation.get(),
				"iorel").
		addWhereTriple(
				"iorel",
				ObjectProperties.subject.get(),
				"rootOutput").
		addWhereTriple(
				"iorel",
				ObjectProperties.predicate.get(),
				"property").
		addWhereTriple(
				"iorel",
				ObjectProperties.object.get(),
				"rootInput");
		
		System.out.println(builder);
		
		ResultSet results = builder.run(Ontology.getModel());
		
		while (results.hasNext()){
			Service match = registry.lookup(results.next().get("service")
					.asResource().getURI());
			if (match == null ) continue;
			return match;
		}
		
		SelectQueryBuilder ibuilder = new SelectQueryBuilder();
		ibuilder.
		setColumns("distinct ?service").
		addWhereTriple(
				_inputType,
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				property.getDomain()).
		addWhereTriple(
				_outputType,
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				property.getRange()).
		addWhereTriple(
				"service",
				Ontology.getModel().getProperty(Ontology.rdfNS +	"type"),
				OntClasses.SISOService.get()).
		addWhereTriple(
				"service",
				ObjectProperties.hasRootInput.get(),
				"rootInput").
		addWhereTriple(
				"service",
				ObjectProperties.hasRootOutput.get(),
				"rootOutput").
		addWhereTriple(
				"rootInput",
				ObjectProperties.type.get(),
				"rootInputClass").
		addWhereTriple(
				"rootOutput",
				ObjectProperties.type.get(),
				"rootOutputClass").
		addWhereTriple(
				"rootInputClass",
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				_inputType
				).
		addWhereTriple(
				"rootOutputClass",
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subClassOf"),
				_outputType
				).
		addWhereTriple(
				property,//switched
				Ontology.getModel().getProperty(Ontology.rdfsNS + "subPropertyOf"),
				"property").
		addWhereTriple(
				"iproperty",
				Ontology.getModel().getProperty(Ontology.owlNS + "inverseOf"),
				"property").
		addWhereTriple(
				"service",
				ObjectProperties.hasIORelation.get(),
				"iorel").
		addWhereTriple(
				"iorel",
				ObjectProperties.subject.get(),
				"rootOutput").
		addWhereTriple(
				"iorel",
				ObjectProperties.predicate.get(),
				"iproperty").
		addWhereTriple(
				"iorel",
				ObjectProperties.object.get(),
				"rootInput");
		
		results = ibuilder.run(Ontology.getModel());
		
		System.out.println(builder);
		
		while (results.hasNext()){
			Service match = registry.lookup(results.next().get("service")
					.asResource().getURI());
			if (match == null ) continue;
			return match;
		}
		return null;
	}
	
	public static Service matchService(OntProperty property){
		return matchService(null, property, null);
	}
	
	public static void main(String args[]){
		Ontology.initialize();
		
		Service tcrl = new Service (Ontology.baseUri + "TrueCallerReverseLookup");
		
		ServiceRegistry reg = new ServiceRegistry();
		Iterator<Individual> it = Ontology.getModel().listIndividuals(OntClasses.SISOService.get());
		while(it.hasNext()){
			reg.register(new Service(it.next().getURI()));
		}
		setRegistry(reg);
		
		tcrl.getRootInputs().get(0).listIORelProperties().get(0).toString();
		System.out.println(matchService(
				Ontology.getModel().getOntClass(Ontology.baseUri		+ "SignalStrengthMeasurement"),
				Ontology.getModel().getOntProperty(Ontology.baseUri		+ "signalStrengthMeasurementOf"),
				Ontology.getModel().getOntClass(Ontology.baseUri		+ "ServiceProvider")).
				asIndividual().getURI());
	}
}

