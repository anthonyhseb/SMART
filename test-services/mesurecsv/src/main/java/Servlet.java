/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.InputStreamReader;

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
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/xml;charset=UTF-8");
        BufferedReader br = new BufferedReader(new InputStreamReader(Thread.currentThread().getContextClassLoader().getResourceAsStream("output.csv")));
        PrintWriter out = response.getWriter();
         
        try {

            out.write("<?xml version=\"1.0\"?>\n");
            out.write("<root>\n");

            String file = br.readLine();

            while (file != null) {
                String[] tokenArray = file.split(";");

                // the input is always the name of the Provider: ex 'Alfa'
                // while providers are referenced with an id in the csv file
                if (tokenArray[2].equals(request.getParameter("i"))) {
                    out.write("  <GeoMeasurement>\n");
                    out.write("      <longitude>");
                    out.write(tokenArray[0]);
                    out.write("</longitude>\n");
                    out.write("      <latitude>");
                    out.write(tokenArray[1]);
                    out.write("</latitude>\n");
                    out.write("      <provider>");
                    out.write(tokenArray[2]);
                    out.write("</provider>\n");
                    out.write("      <value>");
                    out.write(tokenArray[3]);
                    out.write("</value>\n");
                    out.write("      <unit>percent</unit>\n");
                    out.write("  </GeoMeasurement>\n");
                }
                file = br.readLine();
            }
            out.write("</root>");
            out.flush();
            out.close();
            /* TODO output your page here. You may use following sample code. */
        } finally {
            out.close();
        }
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
