/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import org.w3c.dom.Document;

/**
 *
 * @author anthony
 */
public class GetResults extends HttpServlet {

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
		//Storage.loadSession(request, this);
		
		Object _results = session.getAttribute("results");
		if (_results == null) {
			response.sendRedirect("index.jsp");
			return;
		}
		
		Document results = (Document)_results;
		
		TransformerFactory factory = TransformerFactory.newInstance();
        //Source xslt = new StreamSource(new File("/Users/anthony/Dropbox/Projet/Projet Rima/for hseb/final/xsltTransform.xsl"));
        Source xslt = new StreamSource(Thread.currentThread().getContextClassLoader().getResourceAsStream("xsltTransform.xsl"));
	Transformer transformer;
		try{transformer = factory.newTransformer(xslt);}
		catch(Exception ex){ throw new RuntimeException(ex);}

        DOMSource source = new DOMSource(results);
		try{
        transformer.transform(source, new StreamResult(out));}
		catch(Exception ex){ throw new RuntimeException(ex);}
		
				
//		DOMSource source = new DOMSource(results);
//
//		// Prepare the output file
//		//File file = new File(filename);
//		//Result result = new StreamResult(file);
//		Result result = new StreamResult(out);
//
//		// Write the DOM document to the file
//		try{
//		Transformer xformer = TransformerFactory.newInstance().newTransformer();
//		xformer.transform(source, result);}catch(Exception ex){throw new RuntimeException(ex);}
		
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
}
