/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import app.web.ApplicationStorage;
import com.hp.hpl.jena.datatypes.xsd.XSDDatatype;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import core.Ontology;
import core.service.ResultSet;
import core.service.Service;
import core.util.ElementGenerator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;

/**
 * A user Query to be processed.
 */
public class Query {
	private	LinkedList<Concept>		concepts;
	private	OntClass				inputType;
	private LinkedList<SubQuery>	subQueries;
	private ListIterator<Concept>	conceptIt;	//positionned at next property
	/** Builds the Query object using the list of Concepts extracted from the
	 * textual form of the query as well as the output class.
	 */
	private	void	build(LinkedList<Concept> concepts, OntClass inputType){
		this.concepts = concepts;
		this.inputType = inputType;
		this.conceptIt = this.concepts.listIterator(this.concepts.size());
		// if firstConcept is a class, the first property is expected next,
		// if firstConcept is a property, reset the iterator.
		if (conceptIt.hasPrevious()){
			Concept firstConcept = conceptIt.previous();
			if(firstConcept.isProperty()) conceptIt.next();
		}
	}
	
	private Query(){}
	/**
	 * Builds a Query object from a String.
	 * @param query
	 * @return 
	 */
	public static Query parse(String query){
		System.out.println("parsing query:" + query + "\n");
		LinkedList<Concept> concepts = new LinkedList();
		String _query = query.trim().toLowerCase();
		
		String words[] = _query.split(" ");
		
		String buffer = "";
		boolean accept = true;
		int i;
		for (i=0 ; i<words.length ; i++){
			// words to ignore
			if (words[i].equals("the"))		continue;
			if (words[i].equals("that"))	continue;
			if (words[i].equals("a"))		continue;
			if (words[i].equals("are"))		continue;
			if (words[i].equals("which"))	continue;
			// input marking keywords
			if (words[i].equals("this"))	{break;}
			if (words[i].equals("these"))	{break;}
			
			
			if (accept) buffer += " " + words[i];
			buffer = buffer.trim();
			Concept curConcept = ApplicationStorage.instance.suggestionGenerator.match(buffer);
			if (curConcept != null){
				concepts.add(curConcept);
				buffer = "";
				continue;
			}
			if (buffer.endsWith("s")){
				String singular = buffer.substring(0, buffer.length() - 1);
				curConcept = ApplicationStorage.instance.suggestionGenerator.match(singular);
				if (curConcept != null){
					concepts.add(curConcept);
					buffer = "";
					continue;
				}
			}
			accept = true;
		}
		OntClass _inputType = null;
		
		i++;
		
		for (; i<words.length ; i++){
		/*	buffer += " " + words[i];
			Concept curConcept = ApplicationStorage.instance.suggestionGenerator.match(buffer.trim().toLowerCase());
			if (curConcept == null) continue;
			if (!curConcept.isClass()) continue;
			_inputType = curConcept.asClass();
			break;
		}
		*/
		if (accept) buffer += " " + words[i];
			buffer = buffer.trim();
			Concept curConcept = ApplicationStorage.instance.suggestionGenerator.match(buffer);
			if (curConcept != null){
				_inputType = curConcept.asClass();
				break;
			}
			if (buffer.endsWith("s")){
				String singular = buffer.substring(0, buffer.length() - 1);
				curConcept = ApplicationStorage.instance.suggestionGenerator.match(singular);
				if (curConcept != null){
					_inputType = curConcept.asClass();
					break;
				}
			}
			accept = true;
		}
		return new Query(concepts, _inputType);
	}
	
	
	/**
	 * Builds the Query object using the list of Concepts extracted from the
	 * textual form of the query as well as the output class.
	 */
	public	Query(List<Concept> concepts, OntClass inputType){
		LinkedList<Concept> _concepts = new LinkedList<Concept>();
		_concepts.addAll(concepts);
		build(_concepts, inputType);
	}
	/**
	 * Builds the Query object using the list of Concepts extracted from the
	 * textual form of the query as well as the output class.
	 */
	public	Query(LinkedList<Concept> concepts, OntClass inputType){
		build(concepts, inputType);
	}
	/**
	 * Extracts a single SubQuery object from the list of concepts.
	 */
	private void readSubQuery(){
		//read class or property
		OntClass	_inputType = null;
		OntClass	_outputType = null;
		OntProperty property;
		
		if(subQueries.size()==0) _inputType = inputType;
		
		Concept		curConcept = conceptIt.previous();
		
		if(!curConcept.isProperty())
			throw new InvalidQueryException("Invalid structure: missing property");
		//setProperty
		property = curConcept.asProperty();
		
		// set inputType
		if (conceptIt.hasNext()){
			curConcept = conceptIt.next();
			if(curConcept.isClass()) _inputType = curConcept.asClass();
			conceptIt.previous();
		}
		
		if(!conceptIt.hasPrevious()){
			subQueries.add(new SubQuery(_inputType, property, _outputType));
			return;
		}
		
		// setOutputType
		curConcept = conceptIt.previous();
		
		if(curConcept.isProperty()) {
			conceptIt.next();
			subQueries.add(new SubQuery(_inputType, property, _outputType));
			return;
		}
		
		_outputType = curConcept.asClass();
		
		subQueries.add(new SubQuery(_inputType, property, _outputType));			//verify addFirst/add/addLast
	}
	
	/**
	 * Reads the concept list in reverse to build the list of SubQueries.
	 */
	private	void extractSubQueries(){
		subQueries = new LinkedList<SubQuery>();
		if (!conceptIt.hasPrevious())
			throw	new	InvalidQueryException("Incomplete query: no properties found.");
		while (conceptIt.hasPrevious()){
			readSubQuery();
		}
	}
	/**
	 * Validates the Query, throw an exception if invalid.
	 * @throws InvalidQueryException
	 */
	private void validateQuery(){
		ListIterator<SubQuery> subQueryIt = subQueries.listIterator();
		SubQuery previousQuery = null;
		SubQuery curQuery;
		
		if(!(subQueryIt.hasNext()))
			throw new InvalidQueryException("Empty Query.");
		curQuery = subQueryIt.next();
		if(!curQuery.supportsInputType(inputType))
			throw new InvalidQueryException("Bad input type.");
		
		while(subQueryIt.hasNext()){
			curQuery = subQueryIt.next();
			boolean valid, compatible;
			valid		= curQuery.isValid();
			compatible	= curQuery.compatibleWith(previousQuery);
//			if(!valid) throw new InvalidQueryException("invalid subquery");
			if(!compatible) throw new InvalidQueryException("incompatible proprety");
			previousQuery = curQuery;
		}
	}
	/**
	 * Builds the list of Services required to answer the Query.
	 */
	private LinkedList<Service> extractServices(){
		LinkedList<Service> output = new LinkedList<Service>();
		
		ListIterator<SubQuery> subQueryIt = subQueries.listIterator();
		Service previousService = null;
		SubQuery curQuery;
		while(subQueryIt.hasNext()){
			curQuery = subQueryIt.next();
			if (curQuery.derivableFromService(previousService)){continue;}
			previousService = QueryEngine.matchService(
					curQuery.getSuggestedInput(),
					curQuery.getProperty(),
					curQuery.getSuggestedOutput());
			if (previousService == null){
				throw new ServiceNotFoundException();
			}
			output.add(previousService);
		}
		return output;
	}
	/**
	 * Creates the Mashup object that answers the Query.
	 */
	public Mashup compile(){		
		extractSubQueries();
		validateQuery();
		return new Mashup(extractServices());
	}
	
/*	
//	@SuppressWarnings({"null", "ConstantConditions"})
//	public	Mashup compile() {
//		LinkedList<Service> services = new LinkedList<Service>();
//		ListIterator<Concept> conceptIt = concepts.listIterator(concepts.size());
//		OntClass		curInputType		= inputType;
//		OntClass		curOutputType		= null;
//		OntProperty		curProperty			= null;
//		OntProperty		lastProperty		= null;
//		Service			lastService			= null;
//		Service			curService			= null;
//		LogicalInput	lastLogicalInput	= null;
//		boolean			expectingProperty	= true;
//		Concept			curConcept;
//		Concept			nextConcept = conceptIt.previous();
//		concepts:while(conceptIt.hasPrevious()){
//			curConcept	= nextConcept;
//			nextConcept = conceptIt.previous();
//			// read or infer class curInput type
//			// check for conflict with last outputType
//			OntClass curClass = null;
//			if (curConcept.isClass()){
//				if (expectingProperty) throw new InvalidQueryException();
//				curClass = curConcept.as(OntClass.class);
//				expectingProperty = false;
//				continue;
//			}
//			else if (!expectingProperty) {
//				curClass = lastProperty.getRange().as(OntClass.class);
//			}
//			// error if curClass is not a subclass of the previous output (stored in curInputType)
//			if (curClass == null) throw new Error();
//			if (!(curClass.hasSuperClass(curInputType))){
//				throw new InvalidQueryException();
//			}
//			curInputType = curClass;
//			//
//			
//			curProperty = curConcept.as(OntProperty.class);
//			//verify that the curProperty and curInputType are compatibe
//			//ask { ?curInputType rdfs:subClassOf ?curPropertyDomain }
//			if (!(curInputType.hasSuperClass(curProperty.getDomain()))){
//				throw new InvalidQueryException();
//			}
//			
//			// set curOutputType
//			if (nextConcept.isClass()){curOutputType = nextConcept.asClass();}
//			else { curOutputType = curProperty.getDomain().asClass(); }
//			
//			//verify that the curProperty and curInputType are compatibe
//			//ask { ?curOutputType rdfs:subClassOf ?curPropertyRange }
//			if(!(curOutputType.hasSuperClass(curProperty.getRange().asClass()))){
//				throw new InvalidQueryException();
//			}
//			
//			//if the last service can provide the object of the current property
//			//continue because no new service is needed.
//			if (lastService != null) {
//				Iterator<Input> subInputIt =
//						lastLogicalInput.getDirectSubParameters().iterator();
//				subinputs:while(subInputIt.hasNext()){
//					Input curSubInput = subInputIt.next();
//					if(!(curSubInput instanceof LogicalInput))
//						continue subinputs;
//					LogicalInput curLogical = (LogicalInput) curSubInput;
//					if (curLogical.getFromProperty().getInverse() == curProperty){
//						expectingProperty = false;
//						continue concepts;
//					}
//						
//				}
//			}
//			
//			//Find a new service
//			curService = QueryEngine.
//					matchService(curInputType, curProperty, curOutputType);
//			services.add(curService);
//			
//			//prepare next iteration
//			lastService		= curService;
//			lastProperty	= curProperty;
//			curInputType	= curOutputType;
//			
//		}
//		
//		return new Mashup(services);
//	}
*/
	
	public static void main(String args[]) throws TransformerException{
		
		ApplicationStorage.instance = new ApplicationStorage();
		
		Ontology.initialize();
		
		ApplicationStorage.instance.suggestionGenerator = new SuggestionGenerator();
	
		QueryEngine.loadServices();
		
//		LinkedList<Concept> concepts = new LinkedList<Concept>();
//		concepts.add(new Concept(Ontology.baseUri + "Place"));
//		concepts.add(new Concept(Ontology.baseUri + "relatedTo"));
//		//concepts.add(new Concept(Ontology.baseUri + "Place"));
//		//concepts.add(new Concept(Ontology.baseUri + "similarTo"));
		OntClass inputType = Ontology.getModel().getOntClass(
				Ontology.baseUri + "Place");
//		Query query = new Query(concepts, inputType);
		
		
		Query query = Query.parse("places Related To places related to this place");
		
		Mashup mashup = query.compile();
		
		Document test_tree = mashup.propertyTree.buildDocument(ElementGenerator.Default.newInstance(), "root", null);
		
		DOMSource tmp_source = new DOMSource(test_tree);

		// Prepare the output file
		//File file = new File(filename);
		//Result result = new StreamResult(file);
		Result test_result = new StreamResult(System.out);

		// Write the DOM document to the file
		Transformer transformer = TransformerFactory.newInstance().newTransformer();
		transformer.transform(tmp_source, test_result);
		System.out.println("\n\n");
		
		OntModel model = Ontology.getModel();
		Individual place = model.createIndividual(inputType);
		Individual name = model.createIndividual(model.getOntClass(Ontology.baseUri + "Name"));
		model.add(
				place,
				model.getProperty(Ontology.baseUri + "named"),
				name);
		Literal beirut_name = ResourceFactory.createTypedLiteral("Beirut", XSDDatatype.XSDstring);
		model.add(
				name,
				model.getProperty(Ontology.baseUri + "name"),
				beirut_name);
				
		ResultSet results = mashup.execute(place);
		
		Document doc = mashup.toXML(place);
		
		System.out.println(results.size());
				// Prepare the DOM document for writing
		DOMSource source = new DOMSource(doc);

		// Prepare the output file
		//File file = new File(filename);
		//Result result = new StreamResult(file);
		Result result = new StreamResult(System.out);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
		System.out.println();
	}
}
