/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author User
 */
public class Servlet extends HttpServlet {

    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request Servlet request
     * @param response Servlet response
     * @throws ServletException if a Servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.write("<?xml version=\"1.0\"?>");
		out.write("<root>");
        
        String operator = "";
        String _num = request.getParameter("n");
        try {
            char[] digits = _num.toCharArray();
            String code = digits[0] + "" + digits[1];
            if (code.equals("03") || code.equals("70")) {
                if (digits[2] > '0' && digits[2] < '6') {
                    operator = "Alfa";
                } else {
                    operator = "MTC";
                }
            } else if (code.equals("71")) {
                if(digits[2] > '0' && digits[2] < '6') {
                    operator = "MTC";
                } else {
                    operator = "Alfa";
                }
            } else if (code.equals("76")) {
                operator = "MTC";
            }
            /* TODO output your page here. You may use following sample code. */
            out.write("<Operator>");
            out.write(operator);
            out.write("</Operator>");
			out.write("</root>");
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request Servlet request
     * @param response Servlet response
     * @throws ServletException if a Servlet-specific error occurs
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
     * @param request Servlet request
     * @param response Servlet response
     * @throws ServletException if a Servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the Servlet.
     *
     * @return a String containing Servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
