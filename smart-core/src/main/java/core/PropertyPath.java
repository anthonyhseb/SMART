/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core;

import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * A chain of properties.
 */
public class PropertyPath {
	public	LinkedList<Property>	list;
	/** new empty property path for the given model */
	public PropertyPath(OntModel model){
		list		= new LinkedList<Property>();
	}
	/** Build the property path from the specified list of properties in the 
	 * given model. 
	 */
	public PropertyPath(OntModel model, List<Property> properties){
		this.list	= new LinkedList<Property>();
		this.list.addAll(properties);
	}
	/** add a property at the end of the chain. */
	public boolean	add(Property property){return list.add(property);}
	/**
	 * List all objects of this property path for initial subjects
	 * contained in subjectList
	 * @param subjectList	subjects of the first property in the path
	 * @return list of the distinct objects of the last property in the path
	 */
	public List<RDFNode> listObjects(List<RDFNode> subjectList){
				
		Iterator	<Property>	properties	= list.iterator();
		Iterator	<RDFNode>	subjects;
		Iterator	<RDFNode>	objects;
		
		LinkedList	<RDFNode>	objectList	= new LinkedList<RDFNode>();
		objectList.addAll(subjectList);
		
		Property	curProperty;
		Resource	curSubject;
		Resource	curObject;
		String		curURI;
		
		HashSet<String>	matchedURIs;
		
		while (properties.hasNext()){
			//System.out.println("DEBUG: loop 1");								//DEBUG
			curProperty = properties.next();
			matchedURIs	= new HashSet<String>();
			subjectList = objectList;
			subjects	= subjectList.iterator();
			objectList	= new LinkedList<RDFNode>();
			while (subjects.hasNext()){
				//System.out.println("DEBUG: loop 2");							//DEBUG
				
				//RDFNode debug = subjects.next();								//DEBUG
				//curSubject = debug.asResource();
								
				curSubject = subjects.next().asResource();
				
				objects = Ontology.getModel().listObjectsOfProperty(curSubject, curProperty);
				while (objects.hasNext()){
					//System.out.println("DEBUG: loop 3");						//DEBUG
					RDFNode curObjectNode = objects.next();
					if (curProperty.canAs(ObjectProperty.class)){
						curObject	= curObjectNode.asResource();
						curURI		= curObject.isAnon()?
									curObject.getId().getLabelString()
								:	curObject.getURI();
						if (matchedURIs.contains(curURI)) continue;
						matchedURIs.add(curURI);
						objectList.add(curObject);
					} else
					if (curProperty.canAs(DatatypeProperty.class)){
						objectList.add(curObjectNode);
					}
						//System.out.println("DEBUG: found object: " + curURI);	//DEBUG
				}
			}
		}
		
		return objectList;
	}
	
	/**
	 * List all objects of this property path excluding duplicates.
	 * @param subject Subject of the first predicate in the property path.
	 * @return List of the distinct objects of the last property in the path
	 */
	public List<RDFNode> listObjects(Individual subject){
		
		if (subject == null)
			throw new NullPointerException("Input 'sbuject' is null");
		
		LinkedList<RDFNode> subjects = new LinkedList<RDFNode>();
		subjects.add(subject);
		
		return listObjects(subjects);
	}
	/**
	 * List all objects of this property path excluding duplicates
	 * @return List of the distinct objects of the last property in the path
	 */
	public List<RDFNode> listObjects(){
		List<Resource> resources =
				Ontology.getModel().listSubjectsWithProperty(list.getFirst()).toList();
		LinkedList<RDFNode> subjects = new LinkedList<RDFNode>();
		subjects.addAll(resources);
		return subjects;
	}
	/** returns the inverse chain, or thows an InverseNotFoundException.
	 * @throws InversNotFoundException
	 * @return 
	 */
	public PropertyPath	getInverse(){
		PropertyPath inversePath = new PropertyPath(Ontology.getModel());
		Iterator<Property> it = list.iterator();
		OntProperty curProperty, inverseProperty;
		while (it.hasNext()){
			curProperty		= (OntProperty) it.next();
			inverseProperty	= curProperty.getInverse();
			if (inverseProperty == null ){
				throw new InverseNotFoundException(curProperty);
			}
			inversePath.list.addFirst(inverseProperty);
		}
		return inversePath;
	}
	/**
	 * List all subjects of this property path for the given list of objects,
	 * excluding duplicates.
	 * @param objectList List of objects of the final predicate in the property path
	 * @return List of the distinct subjects of the first property in the path
	 */
	public List<RDFNode> listSubjects(List<RDFNode> objectList){
		ListIterator	<Property>	properties	=
				list.listIterator(list.size());
		ListIterator	<RDFNode>	objects;
		ListIterator	<Resource>	subjects;
		
		List		<RDFNode>	subjectList	= objectList;
		
		Property	curProperty;
		Resource	curObject;
		Resource	curSubject;
		String		curURI;
		
		HashSet<String>	matchedURIs;
		
		while (properties.hasPrevious()){
			//System.out.println("DEBUG: loop 1");								//DEBUG
			curProperty = properties.previous();
			matchedURIs	= new HashSet<String>();
			objectList	= subjectList;
			objects		= objectList.listIterator();
			subjectList	= new LinkedList<RDFNode>();
			while (objects.hasNext()){
				//System.out.println("DEBUG: loop 2");							//DEBUG
				
				//RDFNode debug = objects.next();								//DEBUG
				//curObject = debug.asResource();
								
				curObject = objects.next().asResource();
				
				subjects = Ontology.getModel().listResourcesWithProperty(
						curProperty, curObject).toList().listIterator();
				while (subjects.hasNext()){
					//System.out.println("DEBUG: loop 3");						//DEBUG
					curSubject	= subjects.next();
					curURI		= curSubject.isAnon()?
								curSubject.getId().getLabelString()
							:	curSubject.getURI();
					if (matchedURIs.contains(curURI)) continue;
					matchedURIs.add(curURI);
					subjectList.add(curSubject);
					//System.out.println("DEBUG: found subject: " + curURI);		//DEBUG
				}
			}
		}
		
		return subjectList;
	}

	/**
	 * List all subjects of this property path excluding duplicates.
	 * @param object Object of the last predicate in the property path.
	 * @return List of the distinct subjects of the first property in the path
	 */
	public List<RDFNode> listSubjects(Individual object){
		
		if (object == null)
			throw new NullPointerException("Input 'object' is null");
		
		LinkedList<RDFNode> objects = new LinkedList<RDFNode>();
		objects.add(object);
		
		return listSubjects(objects);
	}
	/**
	 * List all subjects of this property path excluding duplicates
	 * @return List of the distinct subjects of the first property in the path
	 */
	public List<RDFNode> listSubjects(){
		return listSubjects(Ontology.getModel().listObjectsOfProperty(list.getLast())
				.toList());
	}
	
	public static void main(String args[]){
		PropertyPath propertyPath = new PropertyPath(Ontology.getModel());
		
		propertyPath.add(ObjectProperties.hasRestInput.get());
		propertyPath.add(ObjectProperties.fromLogicalInput.get());
		propertyPath.add(ObjectProperties.fromLogicalInput.get());
		
		Individual individual = Ontology.getModel().getIndividual(Ontology.baseUri+"GeoNamesSearch");
		
		Iterator<RDFNode> it = 
		propertyPath.listObjects(individual).iterator();
		
		int c = 1;
		
		while (it.hasNext()){
			System.out.print(c++ + ") ");
			System.out.println(it.next().asResource().toString());
		}

	}

	public RDFNode getObject(Individual subject) {
		return listObjects(subject).get(0);
	}
	public RDFNode getObject(List<RDFNode> subjectList){
		return listObjects(subjectList).get(0);
	}

	public List<Property> listProperties(){
		return list;
	}
	private static class InverseNotFoundException extends RuntimeException {

		public InverseNotFoundException(Property p) {
			super(p.getURI());
		}
	}
}
