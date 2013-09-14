/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import core.DataProperties;
import core.ObjectProperties;
import core.OntClasses;
import core.Ontology;
import core.OntologyFormatException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;
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
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;


/**
 * Wrapper for Service individuals.
 */
public class Service {
	private Individual	service;
	
	private LinkedList<RestInput>			restInputs;
	private LinkedList<LogicalInput>		rootInputs;
	private LinkedList<LogicalOutput>		rootOutputs;
	private	LinkedList<XPathExpression>		rootXPaths;
	private LinkedList<StaticInput>			staticInputs;
	private	XPathExpression					resultXPath;
	private boolean							has_json_output;
	
	private	static final XPath xpath = XPathFactory.newInstance().newXPath();
	private static final DocumentBuilder documentBuilder;
	
	private static final Individual	XML = Ontology.getModel().getIndividual(
			Ontology.baseUri + "XML");
	private static final Individual JSON = Ontology.getModel().getIndividual(
			Ontology.baseUri + "JSON");
	
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
	
	
	public	Service(String rdfID){
		service = Ontology.getModel().getIndividual(rdfID);
		
		//get outputFormat
		has_json_output = 
				service.getPropertyResourceValue(
				ObjectProperties.outputFormat.get()).equals(JSON);
		
		//Read restInputs and staticInputs
		restInputs		= new LinkedList<RestInput>();
		staticInputs	= new LinkedList<StaticInput>();
		
		Iterator<RDFNode> nodes =
				service.listPropertyValues(ObjectProperties.hasRestInput.get());
		
		while (nodes.hasNext()) {
			Individual curNode = nodes.next().as(Individual.class);
			if (curNode.hasOntClass(OntClasses.VariableRestInputParameter.get())){
				restInputs.add(new RestInput(curNode, this));
			} else
			if (curNode.hasOntClass(OntClasses.StaticRestInputParameter.get())){
				staticInputs.add(new StaticInput(curNode, this));
			}

		}
		
		//Read rootInputs
		rootInputs = new LinkedList<LogicalInput>();
		
		nodes =	service.listPropertyValues(ObjectProperties.hasRootInput.get());
		
		while (nodes.hasNext()) {
			rootInputs.add(new LogicalInput(nodes.next().as(Individual.class),
					this, null));
		}
		
		//Read rootOutputs and correspondig XPaths
		rootOutputs = new LinkedList<LogicalOutput>();
		rootXPaths	= new LinkedList<XPathExpression>();
		
		nodes =	service.listPropertyValues(ObjectProperties.hasRootOutput.get());
		
		while (nodes.hasNext()) {
			Individual curRootOutput = nodes.next().as(Individual.class);
			rootOutputs.add(new LogicalOutput(curRootOutput, this));
			String expr = curRootOutput.getPropertyValue(
				DataProperties.rootOutputXPath.get()).asLiteral().getString();
			XPathExpression curXPath;
			try {
				curXPath = xpath.compile(expr);
			} catch (XPathExpressionException ex) {
					throw new OntologyFormatException(
							"Could not compile resultXPath of " + curRootOutput.getURI(),
							ex);
			}
			rootXPaths.add(curXPath);
		}
		
		//Read resultXPath
		String expr = service.getPropertyValue(
				DataProperties.resultXPath.get()).asLiteral().getString();
		try {
			resultXPath = xpath.compile(expr);
		} catch (XPathExpressionException ex) {
			throw new OntologyFormatException(
					"Could not compile resultXPath of " + service.getURI(),
					ex);
		}
		
	}
	/** construct the request URL by extracting the parameters from the input
	 * individuals. 
	 */
	private	String	buildUrl(List<Individual> rootInputs){
		
		Iterator<LogicalInput>	rootTypesIt		= this.rootInputs.iterator();
		Iterator<Individual>	rootInputsIt	= rootInputs.iterator();
		
		LinkedList<String> names	= new LinkedList<String>();
		LinkedList<String> values	= new LinkedList<String>();
		
		if (rootInputs.size() != this.rootInputs.size())
			throw new IllegalArgumentException("Input count does not match!");
		
		while(rootTypesIt.hasNext()){
			LogicalInput	curRootType		= rootTypesIt.next();
			Individual		curRootInput	= rootInputsIt.next();
			Iterator<RestInput> restInputIt =
					curRootType.getRestSubParameters().iterator();
			while(restInputIt.hasNext()){
				RestInput curRestInput = restInputIt.next();
				names.add(curRestInput.getName());
				values.add(curRestInput.evaluate(curRootInput));
			}
		}

		Iterator<StaticInput> staticInputIt = staticInputs.iterator();
		while(staticInputIt.hasNext()){
			StaticInput curStaticInput = staticInputIt.next();
			names	.add(curStaticInput.getName()	);
			values	.add(curStaticInput.getValue()	);
		}
		//Build url
		String url = service.getPropertyValue(DataProperties.endpoint.get())
				.asLiteral().getString() + '?';
		
		Iterator<String> nameIt		= names.iterator();
		Iterator<String> valueIt	= values.iterator();
		boolean first = true;
		while(nameIt.hasNext()){
			try {
				url += first? "" : '&';
				url += URLEncoder.encode(nameIt.next(), "UTF-8");
				url += '=';
				url += URLEncoder.encode(valueIt.next(), "UTF-8");
				first = false;
			} catch (UnsupportedEncodingException ex) {
				throw new RuntimeException(ex);
			}
		}
		return url;
	}
	/** call the service and build the Document object of the response */
	private	Document getResponse(String url){
		if (hasJsonOutput()){return getJsonResponseAsXML(url);}
		try {
			return documentBuilder.parse(url);
		} catch (Exception ex){
			throw new RuntimeException(ex);
		}
	}
	
	public	Individual matchInput(LogicalInput type, List<Individual> rootInputs){
		Iterator<LogicalInput> logicalInputIt = this.rootInputs.iterator();
		LinkedList<RDFNode> rdfInputs	= new LinkedList<RDFNode>();
		rdfInputs.addAll(rootInputs);
		RDFNode match;
		while (logicalInputIt.hasNext()) {
			LogicalInput curLogicalInput	= logicalInputIt.next();
			match =
					curLogicalInput.getPathFromRoot().getObject(rdfInputs);
			if (match != null) return match.as(Individual.class);
		}
		return null;
	}
	/** parses the output XML document and return a ResultSet of individuals. */
	private	ResultSet parse(Document response, List<Individual> rootInputs){
		ResultSetImpl output = new ResultSetImpl();
		NodeList results;
		try {
			results = (NodeList) resultXPath.evaluate(
					response, XPathConstants.NODESET);
		} catch (XPathExpressionException ex) {
			throw new RuntimeException(ex);
		}
		for (int i=0 ; i < results.getLength() ; i++ ){
			Iterator<LogicalOutput> rootOutputIt = rootOutputs.iterator();
			ResultImpl curResult = new ResultImpl();
			while(rootOutputIt.hasNext()){
				LogicalOutput curOutput = rootOutputIt.next();
				curResult.add(curOutput.read(results.item(i), rootInputs));
			}
			output.add(curResult);
			curResult.startIterator();
		}
		output.startIterator();
		return output;
	}
	/** calls the Service by extracting parameters from the specified list of
	 * Individuals, and parses the output as a result set of individuals.
	 */
	public	ResultSet execute(List<Individual> rootInputs){
		
		String	url = buildUrl(rootInputs);
		System.out.println(url);												//DEBUG
		Document response = getResponse(url);
		System.out.println("parsing results...");								//DEBUG
		return parse(response, rootInputs);
	}
	
	public	List<LogicalInput>	getRootInputs() { return rootInputs; }
	
	public	List<LogicalOutput>	getRootOutputs() { return rootOutputs; }
	
	public	boolean	hasSingleInput() { return rootInputs.size() == 1; }
	
	public	OntModel executeAll(OntModel model){
		throw new UnsupportedOperationException("executeAll()");
	}
	/** finds a logical input by Individual */
	public	LogicalInput findLogicalInput(Individual parameter){
		Iterator<LogicalInput> rootInputIt = rootInputs.iterator();
		while(rootInputIt.hasNext()){
			LogicalInput curRootInput = rootInputIt.next();
			LogicalInput result = curRootInput.findSubParameter(parameter);
			if (result != null) return result;
		}
		
		return null;
	}
	/** finds a logical output by Individual */
	public	LogicalOutput findLogicalOutput(Individual parameter){
		Iterator<LogicalOutput> rootOutputIt = rootOutputs.iterator();
		while(rootOutputIt.hasNext()){
			LogicalOutput curRootOutput = rootOutputIt.next();
			LogicalOutput result = curRootOutput.findSubParameter(parameter);
			if (result != null) return result;
		}
		
		return null;
	}
	public	boolean	hasJsonOutput(){return has_json_output;}
	
	public	Individual asIndividual(){ return service; }
	

	/**
	 * Fetches the URL's JSON response and returns the equivalent XML document ;
	 * adds a root node named "root".
	 * 
	 * @param url URL to fetch.
	 *
	 */
	private Document getJsonResponseAsXML(String url) {
		Document doc;
		try {
			URL _url = new URL(url);
			InputStream is = _url.openStream();
			int ptr;
			StringBuilder buffer = new StringBuilder();
			while ((ptr = is.read()) != -1) {
				buffer.append((char)ptr);
			}
			JSONObject json = new JSONObject(buffer.toString());
			String xml_str = org.json.XML.toString(json);
			
			xml_str = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<root>\n"
					+ xml_str + "\n</root>";
			
			System.out.println(xml_str);
			
			doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder()
					.parse(new InputSource(new StringReader(xml_str)));
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
			return doc;
	}
	
	public static void main(String args[]) throws IOException{
		OntModel model = Ontology.getModel();
		Resource fakePlace	= ResourceFactory.createResource(Ontology.baseUri + "FakePlace");
		Resource fakeName	= ResourceFactory.createResource();
		Literal  fakeNameValue = ResourceFactory.createTypedLiteral(
				"London", XSDDatatype.XSDstring);
		
		Property named	= model.getProperty(Ontology.baseUri	+ "named");
		Property name	= model.getProperty(Ontology.baseUri	+ "name");
		Property type	= model.getProperty(Ontology.rdfNS		+ "type");
		
		OntClass placeClass	= model.getOntClass(Ontology.baseUri + "Place");
		OntClass nameClass	= model.getOntClass(Ontology.baseUri + "Name");
		
		fakePlace = fakePlace.inModel(model);
		fakeName  = fakeName.inModel(model);
		fakeNameValue = fakeNameValue.inModel(model);
		
		model.add(fakePlace,	type,	placeClass);
		model.add(fakeName,		type,	nameClass);
		model.add(fakePlace,	named,	fakeName);
		model.add(fakeName,		name,	fakeNameValue);
		
		//System.out.println(fakeNameValue.asResource());
		
		Service geonames = new Service(Ontology.baseUri + "GeoNamesSearch");
		
		LinkedList<Individual> inputList = new LinkedList<Individual>();
		inputList.add((Individual)fakePlace.as(Individual.class));
		ResultSet output = geonames.execute(inputList);
		
		System.out.println("Results: " + output.size());
		Result	firstResult	= output.next();
		Individual firstRootIndividual = firstResult.next();
		System.out.println("Root Individuals per Result: " + firstResult.size());
		
		ExtendedIterator classes;
		classes = firstRootIndividual.listOntClasses(false);
		System.out.println("\nClasses:");
		while(classes.hasNext()){
			System.out.println(classes.next().toString());
		}
		ExtendedIterator statements;
		statements = firstRootIndividual.listProperties();
		System.out.println("\nStatements:");
		while(statements.hasNext()){
			System.out.println(statements.next().toString());
		}
	}
}
