/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;


import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import core.DataProperties;
import core.ObjectProperties;
import core.OntClasses;
import core.util.Dictionary;
import core.util.DictionaryFactory;
import java.util.Iterator;
import java.util.LinkedList;

/**
 * Generates suggestions to help the user when entering a query.
 */
public class SuggestionGenerator {
	private Dictionary<Concept> propertyDic;
	private Dictionary<Concept> classDic;
	
	public SuggestionGenerator(){
		readProperties();
		readClasses();
	}
	
	private void readProperties(){
		propertyDic = DictionaryFactory.create(Concept.class);
		
		Iterator it = ObjectProperties.topDomainObjectProperty.get()
				.listSubProperties();
		while (it.hasNext()){
			OntProperty curProp = (OntProperty)it.next();
			String label = curProp.getLabel(null);
			if ((label==null) || ("".equals(label))) continue;
			propertyDic.insert(label.trim().toLowerCase(), new Concept(curProp.asResource()));
		}
		
		it = DataProperties.topDomainDataProperty.get()
				.listSubProperties();
		while (it.hasNext()){
			OntProperty curProp = (OntProperty)it.next();
			String label = curProp.getLabel(null);
			if ((label==null) || ("".equals(label))) continue;
			propertyDic.insert(label.trim().toLowerCase(), new Concept(curProp.asResource()));
		}	
	}
	private void readClasses(){
		classDic = DictionaryFactory.create(Concept.class);
		
		Iterator it = OntClasses.DomainThing.get()
				.listSubClasses();
		while (it.hasNext()){
			OntClass curClass = (OntClass)it.next();
			String label = curClass.getLabel(null);
			if ((label==null) || ("".equals(label))) continue;
			classDic.insert(label.trim().toLowerCase(), new Concept(curClass.asResource()));
		}
	}
	
	/** Returns an iterator over all classes having a label that starts with query. */
	public Iterator<Concept>		suggestClass(String query){
		return propertyDic.lookup(query).iterator();
	}
	/** Returns an iterator over all properties having a label that starts with query. */
	public Iterator<Concept>	suggestProperty(String query){
		return propertyDic.lookup(query).iterator();
	}
	/** Returns an iterator over all concepts having a label that starts with query. */
	public Iterator<Concept>		suggest(String query){
		LinkedList<Concept> output = new LinkedList<Concept>();
		output.addAll(propertyDic.lookup(query).toList());
		output.addAll(classDic.lookup(query).toList());
		return output.iterator();
	}
	
	/** Returns the concept with the given label, or null. */
	public Concept	match(String label){
		Concept output;
		output = propertyDic.get(label);
		if (output != null) return output;
		return classDic.get(label);
	}
}
