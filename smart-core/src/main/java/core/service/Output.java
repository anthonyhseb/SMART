/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.service;

import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.rdf.model.RDFNode;
import java.util.List;
import org.w3c.dom.Node;

/**
 *
 * Interface for OutputParameter individuals.
 */
public interface Output extends Parameter{
	/** read a resource from the given context node, and connect it to the
	 * input individual using IORelations.
	 */
	RDFNode read(Node documentNode, List<Individual> rootInputs);
}
