/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

/**
 *
 * @author anthony
 */
class ServiceNotFoundException extends RuntimeException {

	public ServiceNotFoundException() {super();}
	
	public ServiceNotFoundException(String message){super(message);}
}
