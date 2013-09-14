/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import core.service.LogicalOutput;
import core.service.Parameter;
import core.service.RestOutput;
import core.service.Service;
import java.util.Iterator;

/**
 * A portion of a query consisting of a property and the input and output types
 * required by the user.
 */
public class SubQuery {
	private	OntClass	suggestedInput;
	private OntProperty	property;
	private OntClass	suggestedOutput;
	/** gets the input type suggested in the query. */
	public OntClass getSuggestedInput() {return suggestedInput;	}
	/** gets the property of the of the sub query. */
	public OntProperty getProperty()	{return property;		}
	/** get the output type suggested in the query. */
	public OntClass	getSuggestedOutput(){return suggestedOutput;}
	/** builds a SubQuery with the specified parameters.
	 * 
	 * @param suggestedInput Input class suggested in the query.
	 * @param property	Property represented by the SubQuery.
	 * @param suggestedOutput Output class suggested in the query.
	 */
	public SubQuery(OntClass suggestedInput, OntProperty property, OntClass suggestedOutput){
		this.suggestedInput		= suggestedInput;
		this.suggestedOutput	= suggestedOutput;
		this.property			= property;
	}
	/** checks whether the property in the sub query is applicable to the specified
	 * type.
	 */
	public boolean supportsInputType(OntClass inputType){
		return	(inputType == null) ||
				(property.getDomain().asClass().hasEquivalentClass(inputType))||
				(property.getDomain().asClass().hasSubClass(inputType));
	}

	/** checks whether the suggested input and output types are applicable
	 * to the property. */
	public boolean isValid(){
		boolean inputOk  = (suggestedInput == null) ||
				(property.getDomain().asClass().hasSuperClass(suggestedInput));
		boolean outputOk = (suggestedOutput == null) ||
				(property.getRange().asClass().hasSuperClass(suggestedOutput));
		return inputOk && outputOk;
	}
	/** checks whether this SubQuery is a valid successor of the specified
	 * SubQuery.
	 */
	public boolean compatibleWith(SubQuery previousSubQuery){
		if (previousSubQuery == null) return true;
		return previousSubQuery.property.getRange().asClass().
				hasSuperClass(this.property.getDomain().asClass());
	}
	/** 
	 * Checks whether the output of the specified Service contains a pattern
	 * matching this sub query.
	 */
	public boolean derivableFromService(Service previousService){
		if (previousService == null) return false;
		Iterator<LogicalOutput> it = previousService.getRootOutputs().iterator();
		while (it.hasNext()){
			Iterator<Parameter> subIt = it.next().getDirectSubParameters().iterator();
			while (subIt.hasNext()){
				Parameter curSubOutput = subIt.next();
				if (curSubOutput instanceof RestOutput) continue;
				//LogicalOutput curLogicalSubOutput = (LogicalOutput)curSubOutput;
				if (curSubOutput.getFromProperty().as(OntProperty.class)
						.hasSuperProperty(property, false)){return true;}
			}
		}
		return false;
	}
}
