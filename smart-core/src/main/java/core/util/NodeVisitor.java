/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

/**
 * Instances of this class are passed as argument to the traverse() function of
 * core.util.Node.&nbsp;to run an algorithm on each node of the tree.&nbsp;
 * Useful for transforming trees (i.e create a tree based on the original with
 * certain modifications, or create DOM trees).
 * @author anthony
 */
public interface NodeVisitor<T> {
	/**
	 * runs an algorithm at each node of a tree and returns a state object which is used to store the
	 * variables used by the algorithm.&nbsp;The state objects are stacked by the
	 * traversal algorithm and returned to the visitor at the right time to allow the visit
	 * function to behave as if it were traversing single branch of the tree as a list.&nbsp;
	 * The visit() function must prepare the variables for the next iteration and return them in an object;
	 * the traversal algorithm automatically passes the corresponding state object to
	 * emulate iteration over a simple list.
	 * @param node the current node in the traversal algorithm.
	 * @param state the variable used by the function to remember its state.
	 * @return 
	 */
	public Object visit(Node<T> node, Object state);
}
