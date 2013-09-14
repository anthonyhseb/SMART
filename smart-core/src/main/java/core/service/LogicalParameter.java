/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.rdf.model.Property;
import core.util.Node;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 * interface for LogicalParameter individuals.
 */
public abstract class LogicalParameter implements Parameter{
	public abstract OntClass			getType();
	/**
	 * Get the list of properties of this parameter, including those
	 * inferred by IORelations
	 * @return 
	 */
	public abstract List<Property>	getAvailableProperties();
	/** List the children of this parameter. */
	public abstract List<Parameter>	getDirectSubParameters();
	/** get the list of properties that connect this parameter to its children. */
	public abstract List<Property>	getProperties();
	/** list properties that connect this parameter to others through IORelations */
	public abstract List<Property>	listIORelProperties();
	/**
	 * get a tree representation of the structure of this parameter where each node
	 * is a pair containing a parameter (left) and a property (right) ; the properties connect
	 * the parameter of the node to the parameter in the parent node. The root has null as property.
	 * @return 
	 */
	public abstract Node<Pair<Parameter, Property>>	asTree();
}
