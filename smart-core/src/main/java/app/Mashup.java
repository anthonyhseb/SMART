/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.NodeIterator;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import core.Ontology;
import core.service.LogicalInput;
import core.service.LogicalParameter;
import core.service.Parameter;
import core.service.Result;
import core.service.ResultSet;
import core.service.Service;
import core.util.ElementGenerator;
import core.util.NodeVisitor;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A sequence of Services in which the output of each service is the input of
 * the next.
 */
public class Mashup {
	private	LinkedList<Service>							services;
	public core.util.Node<Pair<Parameter, Property>>	propertyTree;
	
	private Mashup() {};
	
	/** Builds a Mashup with the specified list of services. */
	public	Mashup(List<Service> services){
		this.services = new LinkedList<Service>(services);
		// build property tree;
		
		LogicalParameter firsInput = this.services.getFirst().getRootInputs().get(0);
		this.propertyTree = firsInput.asTree();
		core.util.Node<Pair<Parameter, Property>> curParent = propertyTree;

		Iterator<Service> serviceIt = this.services.iterator();

		while (serviceIt.hasNext()){
			Service curService = serviceIt.next();
			LogicalInput curInput = curService.getRootInputs().get(0);
			Property curIORel = curInput.listIORelProperties().get(0);
			core.util.Node<Pair<Parameter, Property>> newChild = 
					//prevOutput.asTree();
					curService.getRootOutputs().get(0).asTree();
			newChild.set(new ImmutablePair<Parameter, Property>(
						newChild.get().getLeft(),
						curIORel
					));
			curParent.appendChild(newChild);
			//prepare next iteration
			curParent = newChild;
			
		}
	}
	
	
	/** Answers the logical input of the first service in the Mashup. */
	public	LogicalInput	getLogicalInput(){
		if (!services.getFirst().hasSingleInput()){
			throw new RuntimeException();
		}
		return services.getFirst().getRootInputs().get(0);
	}
	
	/** Executes the Mashup and returns a ResultSet containing all the final
	 * outputs.
	 * @param rootInput An individual representing the root input of the Mashup
	 */
	public	ResultSet	execute(Individual rootInput){
		LinkedList<ResultSet> curResultSets = new LinkedList<ResultSet>();
		LinkedList<Individual> curInputs = new LinkedList<Individual>();
		curInputs.add(rootInput);
		Iterator<Service> serviceIt = services.iterator();
		curResultSets.add(serviceIt.next().execute(curInputs));
		while (serviceIt.hasNext()){
			Service curService = serviceIt.next();
			LinkedList<ResultSet> newResultSets = new LinkedList<ResultSet>();
			Iterator<ResultSet> resultSetIt = curResultSets.iterator();
			while (resultSetIt.hasNext()){
				ResultSet curResultSet = resultSetIt.next();
				while(curResultSet.hasNext()){
					Result curResult = curResultSet.next();
					curInputs = curResult.toList();
					newResultSets.add(curService.execute(curInputs));
				}
			}
			curResultSets = newResultSets;
		}
		Iterator<ResultSet> resultSetIt = curResultSets.iterator();
		ResultSet output = resultSetIt.next();
		while (resultSetIt.hasNext()) {
			output.append(resultSetIt.next());
		}
		return output;
	}

	public LogicalInput getInput(){
		return services.getFirst().getRootInputs().get(0);
	}
	
	/**
	 * Called after execute(Individual i) to return an XML document containing
	 * all the information gathered from the services
	 * @param rootInput
	 * @return 
	 */
	public Document toXML(Individual rootInput){
//		//Create empty document
//		Document doc;
//		
//		try {
//			doc = DocumentBuilderFactory.newInstance()
//					.newDocumentBuilder().newDocument();
//		} catch (ParserConfigurationException ex) {
//			throw new RuntimeException(ex);
//		}
//		
//		//Create root node "results"
//		Element rootElement = doc.createElementNS(
//				"http://www.example.com/ontresults#", "results");
//		doc.appendChild(rootElement);
//		
//		LogicalParameter firstParam = services.getFirst().getRootInputs().get(0);
//		
//		Element firstIndElement = doc.createElement(
//				firstParam.getType().getLocalName());
//		
//		firstIndElement.setAttribute("label", firstParam.getType().getLabel(null));
//		firstIndElement.setAttribute("type", "class");
//		
//		rootElement.appendChild(firstIndElement);
//		
//		Iterator<Service>			serviceIt	 = services.iterator();
//		Stack<Element>				nodeStack	 = new Stack<Element>();
//		Stack<LogicalParameter>		paramStack	 = new Stack<LogicalParameter>();
//		Stack<Individual>			indStack	 = new Stack<Individual>();
//		Stack<Iterator<Property>>	propItStack	 = new Stack<Iterator<Property>>();
//		Stack<Iterator<Parameter>>	paramItStack = new Stack<Iterator<Parameter>>();
//		
//		nodeStack.push(firstIndElement);
//		paramStack.push(firstParam);
//		indStack.push(rootInput);
//		propItStack.push(firstParam.getAvailableProperties().iterator());
//		paramItStack.push(firstParam.getDirectSubParameters().iterator());
//		
//		
//		
//		while(serviceIt.hasNext()){
//			//if (nodeStack.isEmpty()){serviceIt.next();}
//			
//			Element				curElement		= nodeStack.peek();
//			LogicalParameter	curParam		= paramStack.peek();
//			Individual			curIndividual	= indStack.peek();
//			Iterator<Property>	curPropIt		= propItStack.peek();
//			
//			//build a property node for the currentProperty
//			if (curPropIt.hasNext()){
//				Property curProp = curPropIt.next();
//				Element propElement = doc.createElement(curProp.getLocalName());
//				propElement.setAttribute("label", curProp.getLocalName());
//				propElement.setAttribute("type", getTypeString(curProp));
//				curElement.appendChild(propElement);
//				// if dataptoperty fill text content with value and continue with same stack
//				if(curProp.canAs(DatatypeProperty.class)){
//					propElement.setTextContent(
//						curIndividual.getPropertyValue(curProp)
//								.asLiteral().getString());
//					continue;
//				}
//				// if objectproperty append element for child
//				Individual child = curIndividual.
//						getPropertyResourceValue(curProp).as(Individual.class);
//				Element childElement =
//						doc.createElement(child.getOntClass().getLocalName());
//				childElement.setAttribute(
//						"label", child.getOntClass().getLabel(null));
//				childElement.setAttribute("type", "class");
//				propElement.appendChild(childElement);
//				
//				//prepare next iteration
//				nodeStack.push(childElement);
//				paramStack.push(curParam.get);
//			}
//
//		}
//		
//		
//		return doc;
		
		DocumentBuilder docbuilder = new DocumentBuilder();
		
		BuilderState initialState = new BuilderState();
		initialState.initial = true;
		initialState.parentElements = new LinkedList<Element>();
		initialState.parentElements.add(docbuilder.document.getDocumentElement());
		initialState.parentIndividuals = new LinkedList<Individual>();
		initialState.parentIndividuals.add(rootInput);
		
		
		propertyTree.traverse(docbuilder, initialState);
		
		return docbuilder.document;
	}
	/**
	 * Builds an XML document with the same structure as Mashup.propertyTree.
	 */
	private class DocumentBuilder implements NodeVisitor<Pair<Parameter, Property>> {
		public	Document document;
		public	int	counter = 0;
		
		public DocumentBuilder(){
			try {
				document = DocumentBuilderFactory.newInstance()
			   .newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException ex) {
				throw new RuntimeException(ex);
			}
			Element root =
					document.createElement("results");
			document.appendChild(root);
		}
		
		@Override
		public Object visit(core.util.Node<Pair<Parameter, Property>> node, Object state) {
			BuilderState cur = (BuilderState)state;
			BuilderState next = new BuilderState();
			
			
			next.parentElements		= new LinkedList<Element>();
			next.parentIndividuals	= new LinkedList<Individual>();
			
			next.initial = false;
			if (cur.initial) {
				next.parentIndividuals = cur.parentIndividuals;
				Iterator<Individual> individualIt = cur.parentIndividuals.iterator();
				while(individualIt.hasNext()){
					Individual curIndividual = individualIt.next();
					
					Element newElement = document.createElement(curIndividual.getOntClass(true).getLocalName());
					newElement.setAttribute("id", curIndividual.getOntClass(true).getLocalName() + '_' + counter++);
					newElement.setAttribute("label", curIndividual.getOntClass(true).getLabel(null));
					newElement.setAttribute("type", "class");
					
					cur.parentElements.get(0).appendChild(newElement);
					next.parentElements.add(newElement);
				}
				
				return next;
			}
			
			Iterator<Element>		elementIt = cur.parentElements.iterator();
			Iterator<Individual> individualIt = cur.parentIndividuals.iterator();
			
			Parameter curParameter = node.get().getLeft();
			Property curProperty = node.get().getRight();
			
			if (cur.parentElements.size()!=cur.parentElements.size())
				throw new Error();
			
			if (curParameter instanceof LogicalParameter){
				
				while(elementIt.hasNext()){
					Element curElement = elementIt.next();
					Individual curIndividual = individualIt.next();
					
					
					NodeIterator inds = curIndividual.listPropertyValues(curProperty);
					while(inds.hasNext()){
						Individual newIndividual = inds.next().as(Individual.class);
						Element newElement = document.createElement(newIndividual.getOntClass(true).getLocalName());
						newElement.setAttribute("label", newIndividual.getOntClass(true).getLocalName());
						newElement.setAttribute("type", "class");
						newElement.setAttribute("id", newIndividual.getOntClass(true).getLocalName()+ '_' + counter++);
						
						Element propElement = document.createElement(curProperty.getLocalName());
						propElement.setAttribute("label", curProperty.as(OntProperty.class).getLabel(null));
						propElement.setAttribute("type", "objectp");
						propElement.setAttribute("id", curProperty.getLocalName() + '_' + counter++);
						
						curElement.appendChild(propElement);
						propElement.appendChild(newElement);
						
						next.parentIndividuals.add(newIndividual);
						next.parentElements.add(newElement);
					}
				}
			}else{
				
				while(elementIt.hasNext()){
					Element curElement = elementIt.next();
					Individual curIndividual = individualIt.next();
					
					NodeIterator values = curIndividual.listPropertyValues(curProperty);
					
					while(values.hasNext()){
						String value = values.next().asLiteral().getString();
						
						Element propElement = document.createElement(curProperty.getLocalName());
						propElement.setAttribute("label", curProperty.as(OntProperty.class).getLabel(null));
						propElement.setAttribute("type", "datap");
						propElement.setAttribute("id", curProperty.getLocalName() + '_' + counter++);
						propElement.setTextContent(value);

						curElement.appendChild(propElement);
						
						if(values.hasNext())values.next();	//DEBUG
					}
					
				}
			
			}
			return next;
		}
		
	}
	
	/**
	 * State of a Mashup.DocumentBuilder instance at a given Node, contains
	 * variables the DocumentBuilder uses to remember its position in the tree.
	 */
	private class BuilderState {
		public	boolean				initial;
		public	List<Element>		parentElements;
		public	List<Individual>	parentIndividuals;
	}
	
	public static void main(String args[]) throws TransformerConfigurationException, TransformerException{
		OntModel model = Ontology.getModel();
		String base = Ontology.baseUri;
		Mashup mashup = new Mashup();
		Service gns = new Service(Ontology.baseUri + "GeoNamesSearch");
		
		mashup.propertyTree = gns.getRootOutputs().get(0).asTree();
		
		Document doc = mashup.propertyTree.buildDocument(
				ElementGenerator.Default.newInstance()
				, "root", null);
		DOMSource source = new DOMSource(doc);

		// Prepare the output file
		//File file = new File(filename);
		//Result result = new StreamResult(file);
		javax.xml.transform.Result result = new StreamResult(System.out);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
		System.out.println();
		
		
		Individual place_rlo = model.createIndividual(ResourceFactory.createResource(base + "place_rlo"));
		place_rlo.setOntClass(model.getOntClass(base + "Place"));
		
		Individual location_lo = model.createIndividual(ResourceFactory.createResource(base + "location_lo"));
		location_lo.setOntClass(model.getOntClass(base + "Location"));
		model.add(place_rlo, model.getProperty(base + "located"), location_lo);
		
		Individual name_lo = model.createIndividual(ResourceFactory.createResource(base + "name_lo"));
		name_lo.setOntClass(model.getOntClass(base + "Name"));
		model.add(place_rlo, model.getProperty(base + "named"), name_lo);
		
		Literal toponym_ro = ResourceFactory.createTypedLiteral("Tripoly", XSDDatatype.XSDstring);
		model.add(name_lo, model.getProperty(base + "name"), toponym_ro);
		
		Literal lng_ro = ResourceFactory.createTypedLiteral("33.3333", XSDDatatype.XSDdecimal);
		Literal lat_ro = ResourceFactory.createTypedLiteral("22.2222", XSDDatatype.XSDdecimal);
		model.add(location_lo, model.getProperty(base + "longitude"), lng_ro);
		model.add(location_lo, model.getProperty(base + "latitude"), lat_ro);
		
		
		doc = mashup.toXML(place_rlo);

		System.out.println();System.out.println();
		
		
		 source = new DOMSource(doc);

		// Prepare the output file
		//File file = new File(filename);
		//Result result = new StreamResult(file);
		 result = new StreamResult(System.out);

		// Write the DOM document to the file
		 xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
		System.out.println();
		
		
		
	}
}
