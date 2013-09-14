/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import app.Mashup;
import app.Query;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Property;
import core.service.LogicalInput;
import core.service.LogicalParameter;
import core.service.Parameter;
import core.service.RestInput;
import core.util.ElementGenerator;
import core.util.Node;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.tuple.Pair;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 * @author anthony
 */
public class NewQuery extends HttpServlet {

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
	
	private static Document generateInputForm(LogicalInput input){
		Document doc;
		
		doc = input.asTree().buildDocument(new ElementGenerator<Pair<Parameter, Property>>(){
			private int counter = 0;
			@Override
			public Element generate(Pair<Parameter, Property> object, Element parent, Node<Pair<Parameter, Property>> parentNode) {
				Document doc = parent.getOwnerDocument();
				if (object.getLeft() instanceof LogicalParameter){
					Element heading = doc.createElement("div");
					parent.appendChild(heading);
					heading.setAttribute("style", "display:inline-block;");
					String propertyLabel = null;
					if (object.getRight() != null){
						propertyLabel = object.getRight().as(OntProperty.class).getLabel(null).trim();
					}
					heading.setTextContent(
							(propertyLabel == null ? "" : propertyLabel + " : ") +
							((LogicalParameter)object.getLeft()).getType().getLabel(null).trim());
					heading.appendChild(doc.createElement("br"));
					Element indent = doc.createElement("span");
					indent.setAttribute("style", "width:10px;color:transparent;");
					indent.setTextContent("_");
					heading.appendChild(indent);
					return heading;
				}
				if (object.getLeft() instanceof RestInput){
					Element div = doc.createElement("div");					
					parent.appendChild(div);
					div.setAttribute("style", "display:inline-block");
					div.setTextContent(object.getRight().as(OntProperty.class).getLabel(null));
					Element input = doc.createElement("input");
					input.setAttribute("type", "text");
					input.setAttribute("name", "prop_" + counter++);
					div.appendChild(input);
					return div;
				}
				return null;
			}
		}, "form", null);
		doc.getDocumentElement().setAttribute("action", "javascript:void(0);");
		doc.getDocumentElement().setAttribute("onsubmit", "submitInput()");
		doc.getDocumentElement().setAttribute("name", "input_form");
		Element submit = doc.createElement("input");
		submit.setAttribute("type", "submit");
		submit.setAttribute("style", "margin: 0 auto;");
		doc.getFirstChild().appendChild(doc.createElement("br"));
		doc.getFirstChild().appendChild(submit);
		return doc;
	}
	
	protected void processRequest(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException, TransformerConfigurationException, TransformerException {
		
		response.setContentType("application/xml;charset=UTF-8");
		PrintWriter out = response.getWriter();
		
		Storage.newSession(request, this);
		
		String query = request.getParameter("query");
		
		Mashup mashup = Query.parse(query).compile();
		
		request.getSession().setAttribute("mashup", mashup);
		
		LogicalInput input = mashup.getInput();
		
		Document form = generateInputForm(input);
		
		DOMSource source = new DOMSource(form);

		// Prepare the output file
		//File file = new File(filename);
		//Result result = new StreamResult(file);
		Result result = new StreamResult(out);

		// Write the DOM document to the file
		Transformer xformer = TransformerFactory.newInstance().newTransformer();
		xformer.transform(source, result);
		
		Storage.storeSession(request, this);
		
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
		try {
			processRequest(request, response);
		} catch (Exception ex) {
			response.getWriter().write("Error, please <a href='.'> try again</a>. <br />" +
					"<em style=\"font-size:60%;\">" + (ex.getMessage() == null? ex.getClass().getName() : ex.getMessage() + "</em>"));
		}
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
		try {
			processRequest(request, response);
		} catch (Exception ex) {
			throw new RuntimeException(ex);
		}
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Ma khassak";
	}// </editor-fold>
}
