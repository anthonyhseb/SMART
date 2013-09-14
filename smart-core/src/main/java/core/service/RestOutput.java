/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import core.DataProperties;
import core.ObjectProperties;
import core.OntClasses;
import core.Ontology;
import core.OntologyFormatException;
import core.PropertyPath;
import java.util.LinkedList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import org.w3c.dom.Node;

/**
 * Wrapper for RestOutputParamter individuals.
 */
public class RestOutput implements Output{
	private Individual		restOutput;
	private PropertyPath	path;
	private Service			service;
	private DatatypeProperty fromProperty;
	private XPathExpression	restOutputXPath;
	private XSDDatatype		datatype;
	
	private	static final XPath xpath = XPathFactory.newInstance().newXPath();
	private static final DocumentBuilder documentBuilder;
	
	static {
		DocumentBuilderFactory domFactory =
					DocumentBuilderFactory.newInstance();
			domFactory.setNamespaceAware(false);
		try {
			documentBuilder = domFactory.newDocumentBuilder();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
	}
	
	public RestOutput(Individual restOutput, Service service){
		this.restOutput = restOutput;
		this.service = service;
		//get fromProperty
		Resource property = restOutput.getPropertyResourceValue(
				ObjectProperties.fromDataProperty.get());
		this.fromProperty = property == null ?
				null	:	property.as(DatatypeProperty.class);
		//get datatype
		String dtype = this.fromProperty.getPropertyValue(
				Ontology.getModel().getProperty(Ontology.rdfsNS + "range")).toString();
		//System.out.println(dtype);											//DEBUG
		this.datatype = Ontology.parseDatatype(dtype);
		
		//build path from root
		LinkedList<Property> properties = new LinkedList<Property>();
		properties.add(
				restOutput.getPropertyValue(ObjectProperties.fromDataProperty.get())
				.as(Property.class));
		Individual curOutput =
				restOutput.getPropertyValue(ObjectProperties.fromLogicalOutput.get())
				.as(Individual.class);
		while (!curOutput.hasOntClass(OntClasses.RootOutputParameter.get().asResource())){
			Individual parentOutput =
					curOutput.getPropertyValue(ObjectProperties.fromLogicalOutput.get())
					.as(Individual.class);
			properties.addFirst(
					curOutput.getPropertyValue(ObjectProperties.fromObjectProperty.get())
					.as(Property.class));
			curOutput = parentOutput;
		}
		path = new PropertyPath(Ontology.getModel(), properties);
		
		
		//Read XPath
		String expr = restOutput.getPropertyValue(
				DataProperties.restOutputXPath.get()).asLiteral().getString();
		try {
			restOutputXPath = xpath.compile(expr);
		} catch (XPathExpressionException ex) {
			throw new OntologyFormatException(
					"Could not compile restOutputXPath of " + restOutput.getURI(),
					ex);
		}
	}
	
	public String evaluate(Individual rootOutput){
		return path.listObjects(rootOutput).get(0).asLiteral().getString();
	}

	/** read the value of the parameter from the specified context. */
	public Literal read(Node documentNode, OntModel model){
		String literalValue;
		try {
			literalValue = (String)restOutputXPath.evaluate(
					documentNode, XPathConstants.STRING);
		} catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
		return ResourceFactory.createTypedLiteral(literalValue, datatype);
	}
	
	@Override
	public Service getService(){return service;}
	@Override
	public DatatypeProperty getFromProperty(){return fromProperty;}
	@Override
	public Individual asIndividual(){
		return restOutput;
	}

	@Override
	public RDFNode read(Node documentNode, List<Individual> rootInputs) {
		return read(documentNode, Ontology.getModel());
	}
}
