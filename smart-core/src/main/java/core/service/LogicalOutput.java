/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import core.ObjectProperties;
import core.OntClasses;
import core.Ontology;
import core.OntologyFormatException;
import core.SelectQueryBuilder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.w3c.dom.Node;
import com.hp.hpl.jena.query.ResultSet;
import core.util.NodeFactory;
import java.util.HashSet;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.ImmutablePair;

/**
 * Wrapper for LogicalOutput individuals.
 */
public class LogicalOutput extends LogicalParameter implements Output{
	private Individual				logicalOutput;
	private LinkedList<Output>		subOutputs;
	private Service					service;
	private	LinkedList<IORelation>	i2oRelations;
	private	LinkedList<IORelation>	o2iRelations;
	private OntClass				type;
	private ObjectProperty			fromProperty;
	
	public LogicalOutput(Individual logicalOutput, Service service){
		this.logicalOutput	= logicalOutput;
		this.service		= service;
		this.type			= logicalOutput.getPropertyResourceValue(
				ObjectProperties.type.get()).as(OntClass.class);
		//get fromProperty
		Resource property	= logicalOutput.getPropertyResourceValue(
				ObjectProperties.fromObjectProperty.get());
		this.fromProperty	= property == null?
				null	:	property.as(ObjectProperty.class);
		
		//get subOutputs
		subOutputs = new LinkedList<Output>();
		Iterator<RDFNode> subOutputsIt =
				logicalOutput.listPropertyValues(ObjectProperties.toOutput.get());
		
		//System.out.println(subOutputsIt.hasNext());								//DEBUG
		
		while(subOutputsIt.hasNext()){
			Individual subOutput = subOutputsIt.next().as(Individual.class);
			if(subOutput.hasOntClass(OntClasses.LogicalOutputParameter.get(), false)){
				this.subOutputs.add(new LogicalOutput(subOutput, service));
			} else
			if(subOutput.hasOntClass(OntClasses.RestOutputParameter.get(), false)){
				this.subOutputs.add(new RestOutput(subOutput, service));
			} else {
				throw new OntologyFormatException(
						"Parameter " + subOutput.getURI() +
						" is neither a logical nor rest parameter!");
			}
		}
		// read i2oRelations
		i2oRelations = new LinkedList<IORelation>();
		Iterator<Resource> i2oIt = Ontology.getModel().listResourcesWithProperty(
				ObjectProperties.object.get(),
				logicalOutput
				).toList().iterator();
		while (i2oIt.hasNext()){
			Individual curI2O = (Individual)i2oIt.next().as(Individual.class);
			i2oRelations.add(new IORelation(curI2O, service));
		}
		// read o2iRelations
		o2iRelations = new LinkedList<IORelation>();
		Iterator<Resource> o2iIt = Ontology.getModel().listResourcesWithProperty(
				ObjectProperties.subject.get(),
				logicalOutput
				).toList().iterator();
		while (o2iIt.hasNext()){
			Individual curO2I = (Individual)o2iIt.next().as(Individual.class);
			if (curO2I == null) throw new NullPointerException();
			o2iRelations.add(new IORelation(curO2I, service));
		}
	}
	@Override
	public Service getService(){return service;}
	@Override
	public List<Parameter> getDirectSubParameters(){
		LinkedList<Parameter> params = new LinkedList<Parameter>();
		params.addAll(subOutputs);
		return params;
	}
	/** returns all the rest parameters under this LogicalOutput. */
	public List<RestOutput> getRestSubParameters(){
		LinkedList<RestOutput> output = new LinkedList<RestOutput>();
		Iterator<Output> subOutputsIt = this.subOutputs.iterator();
		//System.out.println(subOutputs.size());									//DEBUG
		while (subOutputsIt.hasNext()){
			Output subOutput = subOutputsIt.next();
			if (subOutput instanceof RestOutput){
				output.add((RestOutput)subOutput);
			} else {
				output.addAll(((LogicalOutput)subOutput).getRestSubParameters());
			}
		}
		return output;
	}
	
	/**
	 * Recursively find the first subparameter sharing the id of parameter.
	 * @param parameter the Individual to find.
	 * @return first LogicalOutput matched or null.
	 */
	public LogicalOutput findSubParameter(Individual parameter){
		if (this.equals(parameter)) return this;
		
		Iterator<Output> subOutputIt = subOutputs.iterator();
		while(subOutputIt.hasNext()){
			Output curSubOutput = subOutputIt.next();
			if (curSubOutput instanceof RestOutput) continue;
			LogicalOutput curLogicalOutput= (LogicalOutput)curSubOutput;
			if (curLogicalOutput.equals(parameter)) return curLogicalOutput;
			LogicalOutput result = curLogicalOutput.findSubParameter(parameter);
			if (result != null) return result;	
		}
		
		return null;
	}
	
	private String getID(){
		return logicalOutput.isAnon()?
				logicalOutput.getId().getLabelString() : logicalOutput.getURI();
	}
	
	public boolean equals(LogicalOutput output){
		return this.getID().equals(output.getID());
	}
	
	public boolean equals(Individual output){
		String outputID = output.isAnon()?
				output.getId().getLabelString() : output.getURI();
		return outputID.equals(this.getID());
	}
	/** reads an individual from the specified node, and connects if to the 
	 * corresponding logical input using the IORelations.
	 */
	@Override
	public Individual read(Node documentNode, List<Individual> rootInputs){
		Individual newInstance = newInstance(Ontology.getModel());
		Iterator<IORelation> i2oIt = i2oRelations.iterator();
		while (i2oIt.hasNext()){
			IORelation curIORelation = i2oIt.next();
			Ontology.getModel().add(
					service.matchInput((LogicalInput)curIORelation.getSubject(), rootInputs),
					curIORelation.getProperty().inModel(Ontology.getModel()),
					newInstance);
		}
		Iterator<IORelation> o2iIt = o2iRelations.iterator();
		while(o2iIt.hasNext()){
			IORelation curIORelation = o2iIt.next();
			Ontology.getModel().add(
					newInstance,
					curIORelation.getProperty().inModel(Ontology.getModel()),
					service.matchInput((LogicalInput)curIORelation.getSubject(), rootInputs));
		}
		Iterator<Output> subOutputIt = subOutputs.iterator();
		while (subOutputIt.hasNext()) {
		Output curOutput = subOutputIt.next();
			RDFNode subNode = curOutput.read(documentNode, rootInputs);
			if (newInstance == null) throw new NullPointerException("newInstance");
			if (curOutput.getFromProperty() == null)
				throw new NullPointerException("fromProperty");
			if (subNode		== null) throw new NullPointerException("subNode");
			Ontology.getModel().add(
					newInstance,
					curOutput.getFromProperty(),
					subNode);
		}
		
		return newInstance;
	}
	
	/**
	 * Create an individual as value of this output.
	 * @param model
	 * @return 
	 */
	public Individual newInstance(OntModel model){
		Resource resource = ResourceFactory.createResource();
		model.add(
				resource,
				model.getProperty(Ontology.rdfNS + "type"),
				type);
		return resource.inModel(model).as(Individual.class);
	}
	/** return the property that connects this output to its parent output or null if root. */
	@Override
	public ObjectProperty getFromProperty(){return fromProperty;}
	@Override
	public Individual asIndividual(){
		return logicalOutput;
	}
	/** return the OntClass of individuals that can be used as values of this parameter. */
	@Override
	public OntClass getType(){return type;}
	/**
	 * Get the list of properties available for a value of this parameter in the
	 * output of the service.
	 * @return 
	 */
	@Override
	public List<Property> getProperties(){
		return listProperties(false);
	}
	/**
	 * Get the list of properties available for a value of this parameter after
	 * execution of the service (including relations with the input).
	 * @return 
	 */
	@Override
	public List<Property> getAvailableProperties(){
		return listProperties(true);
	}
	private List<Property> listProperties(boolean includeIORel){
		LinkedList<Property> properties = new LinkedList<Property>();
		Iterator<Output> subParamIt = subOutputs.iterator();
		//get direct properties
		while(subParamIt.hasNext()){
			properties.add(subParamIt.next().getFromProperty());
		}
		if (!includeIORel) return properties;
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.subject.get(), logicalOutput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			properties.add(curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(Property.class));
		}
		return properties;
	}
	
	/** list properties that connect this output to the input of the service. */
	private List<Property> listDirectIORelProperties(){
		LinkedList<Property> properties = new LinkedList<Property>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.subject.get(), logicalOutput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			properties.add(curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(Property.class));
		}
		return properties;
	}
	/** list the inverse of properties that connect inputs of the service to
	 * this output.
	 * @return 
	 */
	private List<Property> listInverseIORelProperties(){
		LinkedList<Property> properties = new LinkedList<Property>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.object.get(), logicalOutput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			OntProperty curProperty = curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(OntProperty.class);
			if (curProperty.hasInverse()){
				properties.add(curProperty);
			}
		}
		return properties;
	}
	/**
	 * List all the properties that connect this output to inputs of the service
	 * including the inverse of properties that connect inputs to this output.
	 * @return 
	 */
	public List<Property> listIORelProperties(){
		List<Property> output = listDirectIORelProperties();
		output.addAll(listInverseIORelProperties());
		return output;
	}
	/**
	 * List Outputs that are connected this input by OutputToInputRelations.
	 * @return 
	 */
	private List<LogicalParameter> listDirectIORelParameters(){
		LinkedList<LogicalParameter> params =
				new LinkedList<LogicalParameter>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.subject.get(), logicalOutput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			Resource curObject = curIORel.getPropertyResourceValue(
					ObjectProperties.object.get());
			params.add(service.findLogicalInput(curObject.as(Individual.class)));
		}
		return params;
	}
	/**
	 * List Inputs that this input is connected to by InputToOutputRelations.
	 * @return 
	 */
	private List<LogicalParameter> listInverseIORelParameters(){
		LinkedList<LogicalParameter> params =
				new LinkedList<LogicalParameter>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.object.get(), logicalOutput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			Individual curObject = curIORel.getPropertyResourceValue(
					ObjectProperties.subject.get()).as(Individual.class);
			OntProperty predicate = curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(OntProperty.class);
			if (predicate.hasInverse())
				params.add(service.findLogicalInput(curObject));
		}
		return params;
	}
	/**
	 * List all Inputs that are connected to this output by an InputToOutputRelation
	 * or OutputToInputRelation
	 * @return 
	 */
	private List<LogicalParameter> listIORelParameters(){
		List<LogicalParameter> output = listDirectIORelParameters();
		output.addAll(listInverseIORelParameters());
		return output;
	}
	/**
	 * Lists pairs of inputs (left value) that are connected to this output
	 * by OutputToInputRelations, and the properties (right value) that connect
	 * then.
	 * @return 
	 */
	private List<Pair<Parameter, Property>> listDirectIORelNodes(){
		List<Pair<Parameter, Property>> nodes =
				new LinkedList<Pair<Parameter, Property>>();
		SelectQueryBuilder qbuilder = new SelectQueryBuilder();
		qbuilder.setColumns("object, property")
				.addWhereTriple(
				"iorel",
				ObjectProperties.subject.get(),
				asIndividual())
				.addWhereTriple(
				"iorel",
				ObjectProperties.subject.get(),
				"object")
				.addWhereTriple(
				"iorel",
				ObjectProperties.predicate.get(),
				"property");
		ResultSet resultset = qbuilder.run(Ontology.getModel());
		while(resultset.hasNext()){
			QuerySolution curSolution = resultset.next();
			Pair<Parameter, Property> newPair =
					new ImmutablePair<Parameter, Property>(
						service.findLogicalInput(curSolution.get("object")
							.as(Individual.class)),
						curSolution.get("property").as(Property.class)
					);
			nodes.add(newPair);
		}
		return nodes;
	}
	/**
	 * Lists pairs inputs (left value) that are connected to this output
	 * by InputToOutputRelations, and the inverse of properties (right value) 
	 * that connect then.
	 * @return 
	 */
	private List<Pair<Parameter, Property>> listInverseIORelNodes(){
		List<Pair<Parameter, Property>> nodes =
				new LinkedList<Pair<Parameter, Property>>();
		SelectQueryBuilder qbuilder = new SelectQueryBuilder();
		qbuilder.setColumns("subject, property")
				.addWhereTriple(
				"iorel",
				ObjectProperties.object.get(),
				asIndividual())
				.addWhereTriple(
				"iorel",
				ObjectProperties.subject.get(),
				"subject")
				.addWhereTriple(
				"iorel",
				ObjectProperties.predicate.get(),
				"property");
		ResultSet resultset = qbuilder.run(Ontology.getModel());
		while(resultset.hasNext()){
			QuerySolution curSolution = resultset.next();
			LogicalInput curInput = service.findLogicalInput(
					curSolution.get("subject").as(Individual.class));
			OntProperty curProperty =
					curSolution.get("property").as(OntProperty.class);
			if (curProperty.hasInverse()){
				nodes.add(new ImmutablePair<Parameter, Property>(
						curInput, curProperty.getInverse()));		
			}
		}
		return nodes;
	}
	/**
	 * Lists pairs inputs (left value) that are connected to this output
	 * by InputToOutputRelations or OutputToInputrelations, and the properties
	 * (right value) that connect the output (as subject) to the input.
	 * @return 
	 */
	private List<Pair<Parameter, Property>> listIORelNodes(){
		List<Pair<Parameter, Property>> out = listDirectIORelNodes();
		out.addAll(listInverseIORelNodes());
		return out;
		
	}
	/**
	 * return a tree of properties that connect this parameter to its sub-parameters
	 * ending with the DataTypeProperties (the leaves) that connect to restParameter.
	 * @param includeIORel include nodes from output parameters by following OutputToInputRelations.
	 * @param parent leave null, used for recursivity.
	 * @return 
	 */
	public core.util.Node<Property> getPropertyTree(
			boolean includeIORel, core.util.Node<Property> parent){
		LinkedList<Property> properties= (LinkedList<Property>)getProperties();
		LinkedList<core.util.Node<Property>> propertyNodes =
				new LinkedList<core.util.Node<Property>>();		
		Iterator<Property>	propertyIt	= properties.iterator();
		while(propertyIt.hasNext()) propertyNodes.add(
				core.util.NodeFactory.create(Property.class,propertyIt.next()));
		parent.appendAllChildNodes(propertyNodes);
		Iterator<core.util.Node<Property>> propertyNodeIt;
		propertyNodeIt = propertyNodes.iterator();
		Iterator<Output>	subOutputIt = subOutputs.iterator();
		while(subOutputIt.hasNext()){
			if(!propertyNodeIt.hasNext())
				throw new OntologyFormatException(
						"Service '"+ service.asIndividual().getLocalName()
						+ "' is missing a fromProperty declaration for one"
						+ "of it's parameters");
			core.util.Node<Property>	curPropertyNode = propertyNodeIt.next();
			Output						curSubOutput	= subOutputIt.next();
			if(curSubOutput instanceof LogicalOutput){
				((LogicalOutput)curSubOutput)
						.getPropertyTree(includeIORel, curPropertyNode);
			}
		}
		
		if (!includeIORel) return parent;
		
		getIORelPropertyTree(parent);
		
		return parent;
	}
	/**
	 * return a tree of properties that connect this parameter to input parameters
	 * and their sub-parameters by following OutputToInputRelations.
	 * @param parent leave null, used for recursivity.
	 * @return 
	 */
	public core.util.Node<Property> getIORelPropertyTree(core.util.Node<Property> parent){
		LinkedList<Property>		properties;
		Iterator<Property>			propertyIt;
		LinkedList<core.util.Node<Property>>	propertyNodes;
		Iterator<core.util.Node<Property>>		propertyNodeIt;
		
		properties = (LinkedList<Property>)listDirectIORelProperties();
		propertyIt = properties.iterator();
		propertyNodes = new LinkedList<core.util.Node<Property>>();
		while(propertyIt.hasNext()) propertyNodes.add(
				core.util.NodeFactory.create(Property.class,propertyIt.next()));
		parent.appendAllChildNodes(propertyNodes);
		propertyNodeIt = propertyNodes.iterator();
		HashSet processedProperties = new HashSet<Property>();
		while(propertyNodeIt.hasNext()){
			core.util.Node<Property> curPropertyNode = propertyNodeIt.next();
			if (processedProperties.contains(curPropertyNode.get())) continue;
			SelectQueryBuilder qbuilder = new SelectQueryBuilder();
			qbuilder.setColumns("distinct ?object")
					.addWhereTriple(
					"iorel",
					ObjectProperties.predicate.get(),
					curPropertyNode.get()
					).addWhereTriple(
					"iorel",
					ObjectProperties.object.get(),
					"object");
			ResultSet resultSet = qbuilder.run(Ontology.getModel());
			while (resultSet.hasNext()){
				QuerySolution curSolution = resultSet.next();
				LogicalInput curLogicalInput = service.findLogicalInput(
						curSolution.getResource("object").as(Individual.class));
				curLogicalInput.getPropertyTree(false, parent);
			}
			processedProperties.add(curPropertyNode.get());
		}
		return parent;
	}
	/**
	 * return a tree of properties that connect this parameter to its sub-parameters
	 * ending with the DataTypeProperties (the leaves) that connect to restParameter.
	 * @param includeIORel include nodes from input parameters by following OutputToInputRelations.
	 * @return 
	 */
	public core.util.Node<Property> getPropertyTree(boolean includeIORel){
		return getPropertyTree(
				includeIORel,
				core.util.NodeFactory.create(Property.class, null));
	}
	
	public core.util.Node<Parameter> getIORelSubtree(core.util.Node<Parameter> parent){
		List<LogicalParameter>		parameters;
		Iterator<LogicalParameter>	parameterIt;
		LinkedList<core.util.Node<Parameter>>	parameterNodes;
		Iterator<core.util.Node<Parameter>>		parameterNodeIt;
		
		parameters = listIORelParameters();
		
		parameterIt = parameters.iterator();
		parameterNodes = new LinkedList<core.util.Node<Parameter>>();
		while(parameterIt.hasNext()) parameterNodes.add(
				core.util.NodeFactory.create(Parameter.class,parameterIt.next()));
		parent.appendAllChildNodes(parameterNodes);
		parameterNodeIt = parameterNodes.iterator();

		
		return parent;
	}
	/**
	 * get a tree representation of the structure of this parameter where each node
	 * is a pair containing a parameter (left) and a property (right) ; the properties connect
	 * the parameter of the node to the parameter in the parent node. The root has null as property.
	 * @return 
	 */
	@Override
	public core.util.Node<Pair<Parameter, Property>> asTree(){
		
		core.util.Node<Pair<Parameter, Property>> node =
				new NodeFactory<Pair<Parameter,Property>>().create(
					new ImmutablePair<Parameter, Property>(
						this,
						this.fromProperty
					));
		
		Iterator<Output> parameterIt = subOutputs.iterator();
		while (parameterIt.hasNext()){
			Parameter curParameter = parameterIt.next();
			if (curParameter instanceof LogicalOutput){
				node.appendChild(((LogicalOutput)curParameter).asTree());
			}else{
				node.appendChild(new ImmutablePair<Parameter, Property>(
						curParameter, curParameter.getFromProperty()));
			}
		}
		return node;
	}
	
}
