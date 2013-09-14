/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package core.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Hidden implementation of the Node interface.
 */
class NodeImpl<T> implements Node<T>{

	private LinkedList<NodeImpl<T>>	children;
	private T						payload;

	/** create a new Node object */
	public NodeImpl() {
		children = new LinkedList<NodeImpl<T>>();
		payload = null;

	}

	public NodeImpl(T object){
		children = new LinkedList<NodeImpl<T>>();
		payload = object;
	}

	/** gets the object inside the node. */
	@Override
	public	T	get(){return payload;}
	
	/** sets the content of the node. */
	@Override
	public	void	set(T obj){payload = obj;}

	/** converts the dictionary to a LinkedList */	
	public LinkedList<T> toList() {
		LinkedList<T> output = new LinkedList<T>();
		if (payload != null) {
			output.add(payload);
		}
		Iterator<NodeImpl<T>> it = children.iterator();
		while(it.hasNext()){
			output.addAll(it.next().toList());
		}
		return output;
	}

	@Override
	public	void	appendChild(T object){
		children.add(new NodeImpl<T>(object));
	}

	@Override
	public	void	appendChild(Node<T> node){
		children.add((NodeImpl<T>)node);
	}
	
	@Override
	public	void	appendAllChildNodes(Collection<Node<T>> nodes){
		Iterator<Node<T>> nodeIt = nodes.iterator();
		while(nodeIt.hasNext()){
			appendChild(nodeIt.next());
		}
	}
	@Override
	public	void	appendAllChildren(Collection<T> children){
		Iterator<T> childIt = children.iterator();
		while(childIt.hasNext()){
			appendChild(childIt.next());
		}
	}
	
	@Override
	public	List<Node<T>>	listChildNodes(){
		LinkedList<Node<T>> output = new LinkedList<Node<T>>();
		output.addAll(children);
		return output;
	}
	/** returns an iterator for this Dictionary */
	@Override
	public Iterator<T> iterator(){
		return new NodeIterator<T>(this);
	}

	public	LinkedList<NodeImpl<T>> listChildren() {
		return children;
	}
	
	@Override
	public Document buildDocument(ElementGenerator<T> generator, String rootNodeName, String rootNodeNS){
		
		Document doc;
		
		try {
			doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Element rootElement = doc.createElementNS(rootNodeNS, rootNodeName);
		doc.appendChild(rootElement);
		
		Stack<NodeImpl<T>>			stack;
		Stack<Iterator<NodeImpl<T>>>itstack;
		Stack<Element>				elstack;
		boolean						backtracking;
		NodeImpl<T>					rootNode;
		
		rootNode	= this;
		stack		= new Stack<NodeImpl<T>>();
		itstack		= new Stack<Iterator<NodeImpl<T>>>();
		elstack		= new Stack<Element>();
		elstack.push(rootElement);
		stack.push(rootNode);
		itstack.push(rootNode.listChildren().iterator());
		backtracking	= false;
		loop1:while(!stack.isEmpty()){

			// if node has payload set nextNode unless already done (noReturn true)
			if(!backtracking) {
				//nextNode = stack.peek();
				//backtracking = true;
				Element newElement =
						generator.generate(stack.peek().get(), elstack.peek(), stack.peek());
				//elstack.peek().appendChild(newElement);
				elstack.push(newElement);
						
			}
			
			if (!itstack.peek().hasNext()) {
				stack.pop();
				elstack.pop();
				if(stack.isEmpty()){break;}
				itstack.pop();
				backtracking = true;
				continue loop1;
			}

			NodeImpl<T> newChild = itstack.peek().next();
			stack.push(newChild);
			itstack.push(newChild.listChildren().iterator());
			backtracking = false;
		}
		
		return doc;
	}
	
	@Override
	public Document buildDocument(ElementListGenerator<T> generator, String rootNodeName, String rootNodeNS){
		
		Document doc;
		
		try {
			doc = DocumentBuilderFactory.newInstance()
					.newDocumentBuilder().newDocument();
		} catch (ParserConfigurationException ex) {
			throw new RuntimeException(ex);
		}
		Element rootElement = doc.createElementNS(rootNodeNS, rootNodeName);
		doc.appendChild(rootElement);
		
		Stack<NodeImpl<T>>			stack;
		Stack<Iterator<NodeImpl<T>>>itstack;
		Stack<LinkedList<Element>>	elstack;
		boolean						backtracking;
		NodeImpl<T>					rootNode;
		
		
		rootNode	= this;
		stack		= new Stack<NodeImpl<T>>();
		itstack		= new Stack<Iterator<NodeImpl<T>>>();
		elstack		= new Stack<LinkedList<Element>>();
		elstack.push(new LinkedList<Element>());
		elstack.peek().add(rootElement);
		stack.push(rootNode);
		itstack.push(rootNode.listChildren().iterator());
		backtracking	= false;
		loop1:while(!stack.isEmpty()){

			// if node has payload set nextNode unless already done (noReturn true)
			if(!backtracking) {
				//nextNode = stack.peek();
				//backtracking = true;
				LinkedList<Element> newElements = new LinkedList<Element>();
				Iterator<Element> parentIt = elstack.peek().iterator();
				while(parentIt.hasNext()){
					newElements.addAll(
							generator.generate(stack.peek().get(),
							parentIt.next(), stack.peek()));
				}
				elstack.push(newElements);
					
			}
			
			if (!itstack.peek().hasNext()) {
				stack.pop();
				elstack.pop();
				if(stack.isEmpty()){break;}
				itstack.pop();
				backtracking = true;
				continue loop1;
			}

			NodeImpl<T> newChild = itstack.peek().next();
			stack.push(newChild);
			itstack.push(newChild.listChildren().iterator());
			backtracking = false;
		}
		
		return doc;
	}
	
	@Override
	public void traverse(NodeVisitor visitor, Object initialState){
		
		Stack<NodeImpl<T>>			stack;
		Stack<Iterator<NodeImpl<T>>>itstack;
		Stack						statestack;
		boolean						backtracking;
		NodeImpl<T>					rootNode;
		
		
		rootNode	= this;
		stack		= new Stack<NodeImpl<T>>();
		itstack		= new Stack<Iterator<NodeImpl<T>>>();
		statestack	= new Stack();

		stack.push(rootNode);
		itstack.push(rootNode.listChildren().iterator());
		statestack.push(initialState);
		backtracking	= false;
		loop1:while(!stack.isEmpty()){

			// if node has payload set nextNode unless already done (noReturn true)
			if(!backtracking) {
				//nextNode = stack.peek();
				//backtracking = true;
				Object newState = visitor.visit(stack.peek(), statestack.peek());
				statestack.push(newState);
					
			}
			
			if (!itstack.peek().hasNext()) {
				stack.pop();
				statestack.pop();
				if(stack.isEmpty()){break;}
				itstack.pop();
				backtracking = true;
				continue loop1;
			}

			NodeImpl<T> newChild = itstack.peek().next();
			stack.push(newChild);
			itstack.push(newChild.listChildren().iterator());
			backtracking = false;
		}
	}
	
	
	public static void main(String args[]) throws TransformerException {
		NodeImpl<Integer> root = new NodeImpl<Integer>(1);
		root.appendChild(2);
		Node<Integer> sec_child = new NodeImpl<Integer>(3);
		root.appendChild(sec_child);
		sec_child.appendChild(4);
		
		Iterator<Integer> it = root.iterator();
		//Iterator<Integer>	it = ((NodeImpl<Integer>)root).toList().iterator();
		while(it.hasNext())System.out.println(it.next());
				
		Document doc = root.buildDocument(new ElementListGenerator<Integer>(){
			@Override
			public LinkedList<Element> generate(Integer object, Element parent, Node<Integer> parentNode) {
				Element ret = parent.getOwnerDocument().createElement("value");
				LinkedList<Element> _return = new LinkedList<Element>();
				ret.setTextContent(object.toString());
				parent.appendChild(ret);
				_return.add(ret);
				ret = parent.getOwnerDocument().createElement("neg");
				ret.setTextContent(Integer.toString(-object));
				parent.appendChild(ret);
				_return.add(ret);
				return _return;
			}
		}, "test", "http://www.example.com/test#");
		/*
		System.out.println("Iterator<T>.Subclass".split("^.*\\.|<.*$")[1]);
		doc = root.buildDocument(
				ElementGenerator.Default.newInstance(Integer.class),
				"root", "http://www.example.com/test#");
		*/
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
