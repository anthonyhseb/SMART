/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package app;

/**
 * Exception thrown when a Query contains errors.
 */
class InvalidQueryException extends RuntimeException {

	public InvalidQueryException() {
	}
	public InvalidQueryException(String message) {
		super(message);
	}
}
