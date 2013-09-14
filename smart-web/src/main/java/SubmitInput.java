/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import app.Mashup;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ResourceFactory;
import core.Ontology;
import core.service.LogicalInput;
import core.service.Parameter;
import core.service.RestInput;
import core.util.Node;
import core.util.NodeVisitor;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;

/**
 *
 * @author anthony
 */
public class SubmitInput extends HttpServlet {

	/**
	 * Processes requests for both HTTP
	 * <code>GET</code> and
	 * <code>POST</code> methods.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		response.setContentType("application/xml;charset=UTF-8");
		PrintWriter out = response.getWriter();
		HttpSession session = request.getSession(true);
		if (session.isNew()){
			response.sendRedirect("index.jsp");
			return;
		}
		Storage.loadSession(request, this);
		
		Object _mashup = session.getAttribute("mashup");
		if (_mashup == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		
		Mashup mashup = (Mashup) _mashup;
		
		LogicalInput input = mashup.getInput();
		
		Individual inputIndividual = fillInput(input, request);
		
		//Ontology.initialize(Storage.instance.model);
		
		mashup.execute(inputIndividual);
		
		Document doc = mashup.toXML(inputIndividual);

		request.getSession().setAttribute("results", doc);
		
		Storage.storeSession(request, this);
		
		response.sendRedirect("map.html");
	}

	// <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
	/**
	 * Handles the HTTP
	 * <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Handles the HTTP
	 * <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		processRequest(request, response);
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}// </editor-fold>

	private Individual fillInput(LogicalInput input, HttpServletRequest request) {
		InputParser parser = new InputParser(input.getType(), request);
		input.asTree().traverse(parser, new ParserState(parser.individual));
		return parser.individual;
	}
	
	private class InputParser implements NodeVisitor<Pair<Parameter, Property>> {
		public Individual individual;
		public int		  counter = 0;
		public HttpServletRequest request;
		
		public InputParser(OntClass type, HttpServletRequest request){
			individual = Storage.instance.model.createIndividual(ResourceFactory.createResource());
			individual.setOntClass(type);
			this.request = request;
		}
		
		
		@Override
		public Object visit(Node<Pair<Parameter, Property>> node, Object state) {
			ParserState cur  = (ParserState) state;
			if (node.get().getRight() == null) return new ParserState(individual);
			ParserState next = new ParserState(null);
			
			if(node.get().getLeft() instanceof LogicalInput){
				LogicalInput curLI = (LogicalInput) node.get().getLeft();
				next.parentIndividual = Storage.instance.model.createIndividual(ResourceFactory.createResource());
				next.parentIndividual.setOntClass(curLI.getType());
				//if (Storage.instance.model == null) throw new Error();
				Storage.instance.model.add(
						cur.parentIndividual,
						node.get().getRight(),
						next.parentIndividual);
				System.out.println(node.get().getRight());
				return next;
			}
			if(node.get().getLeft() instanceof RestInput){
				RestInput curRI = (RestInput) node.get().getLeft();
				Literal value = ResourceFactory.createTypedLiteral(
							request.getParameter("prop_" + counter++),
							Ontology.parseDatatype(node.get().getRight().as(OntProperty.class).getRange().getURI()));
				System.out.println(node.get().getRight());
				System.out.println(value.toString());
				Storage.instance.model.add(
						cur.parentIndividual,
						node.get().getRight(),
						value);
			}
			return next;
		}

		
	}
	private class ParserState {
		//public Node<Pair<Parameter, Property>> parentNode = null;
		public Individual parentIndividual;
		public ParserState(Individual parentIndividual){
			this.parentIndividual = parentIndividual;
		}
	}
}
