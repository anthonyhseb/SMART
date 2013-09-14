
import app.QueryEngine;
import app.SuggestionGenerator;
import app.web.ApplicationStorage;
import core.Ontology;
import javax.servlet.http.HttpServletRequest;
import com.hp.hpl.jena.ontology.OntModel;
import core.service.ServiceRegistry;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpSession;


/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */


/**
 *
 * @author anthony
 */
public class Storage extends ApplicationStorage{
	static {
		instance = new ApplicationStorage();
	}
	public static void newSession(HttpServletRequest request, HttpServlet servlet){
		// application level storage :	registry, suggestionGenerator
		// session level storage :		model
		request.getSession(true).invalidate();
		
		readToInstance(request, servlet);
		Ontology.initialize();
		request.getSession(true).setAttribute("model", Ontology.getModel());
		
		instance.model = Ontology.getModel();
	}
	
	public static void loadSession(HttpServletRequest request, HttpServlet servlet){
		readToInstance(request, servlet);
	}
	private static void readToInstance(HttpServletRequest request, HttpServlet servlet){
		HttpSession session = request.getSession();
		Object model = session == null? null : session.getAttribute("model");
		Ontology.initialize(model == null? null : (OntModel) model);
		Object reg = servlet.getServletContext().getAttribute("registry");		//ctx
		if (reg != null) instance.registry = (ServiceRegistry) reg;
		else {
			QueryEngine.loadServices();
			instance.registry = QueryEngine.getRegistry();
		}
		Object sug = request.getSession().getAttribute("suggestionGenerator");	//ctx
		instance.suggestionGenerator = sug == null ?
				new SuggestionGenerator() : (SuggestionGenerator)sug;
	}
	public static void storeSession(HttpServletRequest request, HttpServlet servlet){

		servlet.getServletContext().setAttribute("registry", instance.registry);						//ctx
		servlet.getServletContext().setAttribute("suggestionGenerator", instance.suggestionGenerator);		//ctx
		request.getSession(true).setAttribute("model", instance.model);
	}
}
