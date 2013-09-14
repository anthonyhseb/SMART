/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import core.ObjectProperties;
import core.OntClasses;
import core.Ontology;
import core.OntologyFormatException;
import core.PropertyPath;
import core.SelectQueryBuilder;
import core.util.NodeFactory;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Wrapper for LogicalInput individuals.
 */
public class LogicalInput extends LogicalParameter implements Input{
	private Individual				logicalInput;
	private LinkedList<Input>		subInputs;
	private LogicalInput			parentInput;
	private Service					service;
	private OntClass				type;
	private	ObjectProperty			fromProperty;
	private	PropertyPath			pathFromRoot;
	
	public LogicalInput(Individual logicalInput, Service service, LogicalInput parent){
		this.logicalInput	= logicalInput;
		this.service		= service;
		this.parentInput	= parent;
		this.type			= logicalInput.getPropertyResourceValue(
				ObjectProperties.type.get()).as(OntClass.class);
		//get fromProperty
		Resource property	= logicalInput.getPropertyResourceValue(
				ObjectProperties.fromObjectProperty.get());
		this.fromProperty	= property == null ?
				null	:	property.as(ObjectProperty.class);
		
		//get pathToRoot
		pathFromRoot = new PropertyPath(Ontology.getModel());
		if (!(parentInput == null)){
			pathFromRoot.list.addAll(parent.pathFromRoot.list);
			pathFromRoot.add(fromProperty);
		}
		
		//get subInputs
		subInputs = new LinkedList<Input>();
		Iterator<RDFNode> subInputsIt =
				logicalInput.listPropertyValues(ObjectProperties.toInput.get());
		
		//System.out.println(subInputsIt.hasNext());								//DEBUG
		
		while(subInputsIt.hasNext()){
			Individual subInput = subInputsIt.next().as(Individual.class);
			if(subInput.hasOntClass(OntClasses.LogicalInputParameter.get(), false)){
				this.subInputs.add(new LogicalInput(subInput, service, this));
			} else
			if(subInput.hasOntClass(OntClasses.RestInputParameter.get(), false)){
				this.subInputs.add(new RestInput(subInput, service));
			} else {
				throw new OntologyFormatException(
						"Parameter " + subInput.getURI() +
						" is neither a logical nor rest parameter!");
			}
		}
	}
	@Override
	public Service getService(){return service;}
	/** returns the list of subparameters directly under this LogicalInput. */
	@Override
	public List<Parameter> getDirectSubParameters(){
		LinkedList<Parameter> params = new LinkedList<Parameter>();
		params.addAll(subInputs);
		return params;
	}
	/** returns all Rest parameters under this LogicalInput */
	public List<RestInput> getRestSubParameters(){
		LinkedList<RestInput> output = new LinkedList<RestInput>();
		Iterator<Input> subInputsIt = this.subInputs.iterator();
		//System.out.println(subInputs.size());									//DEBUG
		while (subInputsIt.hasNext()){
			Input subInput = subInputsIt.next();
			if (subInput instanceof RestInput){
				output.add((RestInput)subInput);
			} else {
				output.addAll(((LogicalInput)subInput).getRestSubParameters());
			}
		}
		return output;
	}

	/**
	 * Recursively find the first subparameter sharing the id of parameter.
	 * @param parameter the Individual to find.
	 * @return first LogicalInput matched or null.
	 */
	public LogicalInput findSubParameter(Individual parameter){
		if (this.equals(parameter)) return this;
		
		Iterator<Input> subInputIt = subInputs.iterator();
		while(subInputIt.hasNext()){
			Input curSubInput = subInputIt.next();
			if (curSubInput instanceof RestInput) continue;
			LogicalInput curLogicalInput= (LogicalInput)curSubInput;
			if (curLogicalInput.equals(parameter)) return curLogicalInput;
			LogicalInput result = curLogicalInput.findSubParameter(parameter);
			if (result != null) return result;	
		}
		
		return null;
	}
	
	private String getID(){
		return logicalInput.isAnon()?
				logicalInput.getId().getLabelString() : logicalInput.getURI();
	}
	
	public boolean equals(LogicalInput input){
		return this.getID().equals(input.getID());
	}
	
	public boolean equals(Individual input){
		String inputID = input.isAnon()?
				input.getId().getLabelString() : input.getURI();
		return inputID.equals(this.getID());
	}
	
	@Override
	public Individual asIndividual(){
		return logicalInput;
	}
	/** returns the property path that links to the root input parameter */
	public PropertyPath getPathFromRoot(){
		return pathFromRoot;
	}
	/** returns the property path form the root to this LogicalInput */
	public PropertyPath getPathToRoot(){
		return pathFromRoot.getInverse();
	}
	
	@Override
	public ObjectProperty getFromProperty(){return fromProperty;}
	
	@Override
	public OntClass getType(){return type;}
	public static void main(String args[]){
		LogicalInput test = new LogicalInput(
				Ontology.getModel().getIndividual(
					Ontology.baseUri + "GeoNamesSearchPlaceRootInput")
				, null, null
				);
		System.out.println(test.logicalInput == null);
		System.out.println(test.getRestSubParameters().size());
		
		Individual parameter = Ontology.getModel().getIndividual(
					Ontology.baseUri + "GeoNamesSearchPlaceNameInput");
		
		System.out.println(
				test.findSubParameter(parameter).getID());
	}
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
		Iterator<Input> subParamIt = subInputs.iterator();
		//get direct properties
		while(subParamIt.hasNext()){
			properties.add(subParamIt.next().getFromProperty());
		}
		if (!includeIORel) return properties;
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.subject.get(), logicalInput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			properties.add(curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(Property.class));
		}
		return properties;
	}
	/** list properties that connect this input to the output of the service. */
	private List<Property> listDirectIORelProperties(){
		LinkedList<Property> properties = new LinkedList<Property>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.subject.get(), logicalInput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			properties.add(curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(Property.class));
		}
		return properties;
	}
	/** list the inverse of properties that connect outputs of the service to
	 * this input.
	 * @return 
	 */
	private List<Property> listInverseIORelProperties(){
		LinkedList<Property> properties = new LinkedList<Property>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.object.get(), logicalInput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			OntProperty curProperty = curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(OntProperty.class);
			if (curProperty.hasInverse()){
				properties.add(curProperty.getInverseOf());
			}
		}
		return properties;
	}
	/**
	 * List all the properties that connect this input to outputs of the service
	 * including the inverse of properties that connect outputs to this input.
	 * @return 
	 */
	@Override
	public List<Property> listIORelProperties(){
		List<Property> output = listDirectIORelProperties();
		output.addAll(listInverseIORelProperties());
		return output;
	}
	/**
	 * List Outputs that are connected this input by InputToOutputRelations
	 * @return 
	 */
	private List<LogicalParameter> listDirectIORelParameters(){
		LinkedList<LogicalParameter> params =
				new LinkedList<LogicalParameter>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.subject.get(), logicalInput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			Resource curObject = curIORel.getPropertyResourceValue(
					ObjectProperties.object.get());
			params.add(service.findLogicalOutput(curObject.as(Individual.class)));
		}
		return params;
	}
	/**
	 * List Outputs that this input is connected to by OutputToInputRelations.
	 * @return 
	 */
	private List<LogicalParameter> listInverseIORelParameters(){
		LinkedList<LogicalParameter> params =
				new LinkedList<LogicalParameter>();
		ExtendedIterator<Resource> it = Ontology.getModel().listSubjectsWithProperty(
				ObjectProperties.object.get(), logicalInput);
		while(it.hasNext()){
			Resource curIORel = it.next();
			Individual curObject = curIORel.getPropertyResourceValue(
					ObjectProperties.subject.get()).as(Individual.class);
			OntProperty predicate = curIORel.getPropertyResourceValue(
					ObjectProperties.predicate.get()).as(OntProperty.class);
			if (predicate.hasInverse())
				params.add(service.findLogicalOutput(curObject));
		}
		return params;
	}
	/**
	 * List all Outputs that are connected to this input by an InputToOutputRelation
	 * or OutputToInputRelation
	 * @return 
	 */
	private List<LogicalParameter> listIORelParameters(){
		List<LogicalParameter> output = listDirectIORelParameters();
		output.addAll(listInverseIORelParameters());
		return output;
	}
	/**
	 * Lists pairs outputs (left value) that are connected to this input
	 * by InputToOutputRelations, and the properties (right value) that connect
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
		com.hp.hpl.jena.query.ResultSet resultset = qbuilder.run(Ontology.getModel());
		while(resultset.hasNext()){
			QuerySolution curSolution = resultset.next();
			Pair<Parameter, Property> newPair =
					new ImmutablePair<Parameter, Property>(
						service.findLogicalOutput(curSolution.get("object").as(Individual.class)),
						curSolution.get("property").as(Property.class)
					);
			nodes.add(newPair);
		}
		return nodes;
	}
	/**
	 * Lists pairs outputs (left value) that are connected to this input
	 * by OutputToInputRelations, and the inverse of properties (right value) 
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
		com.hp.hpl.jena.query.ResultSet resultset = qbuilder.run(Ontology.getModel());
		while(resultset.hasNext()){
			QuerySolution curSolution = resultset.next();
			LogicalOutput curOutput = service.findLogicalOutput(
					curSolution.get("subject").as(Individual.class));
			OntProperty curProperty =
					curSolution.get("property").as(OntProperty.class);
			if (curProperty.hasInverse()){
				nodes.add(new ImmutablePair<Parameter, Property>(
						curOutput, curProperty.getInverse()));		
			}
		}
		return nodes;
	}
	/**
	 * Lists pairs outputs (left value) that are connected to this input
	 * by OutputToInputRelations or InputToOutputRelations, and the properties
	 * (right value) that connect the input (as subject) to the output.
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
	 * @param includeIORel include nodes from output parameters by following InputToOutputRelations.
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
		Iterator<Input>	subInputIt = subInputs.iterator();
		while(subInputIt.hasNext()){
			if(!propertyNodeIt.hasNext())
				throw new OntologyFormatException(
						"Service '"+ service.asIndividual().getLocalName()
						+ "' is missing a fromProperty declaration for one"
						+ "of it's parameters");
			core.util.Node<Property>	curPropertyNode = propertyNodeIt.next();
			Input						curSubInput 	= subInputIt.next();
			if(curSubInput instanceof LogicalInput){
				((LogicalInput)curSubInput)
						.getPropertyTree(includeIORel, curPropertyNode);
			}
		}
		
		if (!includeIORel) return parent;
		
		getIORelPropertyTree(parent);
		
		return parent;
	}
	/**
	 * return a tree of properties that connect this parameter to output parameters
	 * and their sub-parameters by following InputToOutputRelations.
	 * @param parent leave null, used for recursivity.
	 * @return 
	 */
	public core.util.Node<Property> getIORelPropertyTree(core.util.Node<Property> parent){
		LinkedList<Property>		properties;
		Iterator<Property>			propertyIt;
		LinkedList<core.util.Node<Property>>	propertyNodes;
		Iterator<core.util.Node<Property>>		propertyNodeIt;
		
		properties = (LinkedList<Property>)listIORelProperties();
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
			com.hp.hpl.jena.query.ResultSet resultSet = qbuilder.run(Ontology.getModel());
			while (resultSet.hasNext()){
				QuerySolution curSolution = resultSet.next();
				LogicalOutput curLogicalOutput = service.findLogicalOutput(
						curSolution.getResource("object").as(Individual.class));
				curLogicalOutput.getPropertyTree(false, parent);
			}
			processedProperties.add(curPropertyNode.get());
		}
		return parent;
	}
	/**
	 * return a tree of properties that connect this parameter to its sub-parameters
	 * ending with the DataTypeProperties (the leaves) that connect to restParameter.
	 * @param includeIORel include nodes from output parameters by following InputToOutputRelations
	 * @return 
	 */
	public core.util.Node<Property> getPropertyTree(boolean includeIORel){
		return getPropertyTree(
				includeIORel,
				core.util.NodeFactory.create(Property.class, null));
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
		
		Iterator<Input> parameterIt = subInputs.iterator();
		while (parameterIt.hasNext()){
			Parameter curParameter = parameterIt.next();
			if (curParameter instanceof LogicalInput){
				node.appendChild(((LogicalInput)curParameter).asTree());
			}else{
				node.appendChild(new ImmutablePair<Parameter, Property>(
						curParameter, curParameter.getFromProperty()));
			}
		}
		return node;
	}
}
