/* Calculator interface
 *  
 * Developed for ECE 309 Lab 9
 *
 * 
 * Allows for calculations using operators '()','^' or 'r', '*' or '/', '+' or '-' operators
 * in said order of precendence. 'ArB' indicates the Bth root of A
 * 
 * Allows for use of pi, e and x symbols 
 *
 * **Note, unary '-' or negation is supported by the interface
 * **Note, complex numbers in the form 'a+bi' are supported by
 *   the interface
 *
 * -----------------------------------------------------------
 */

public interface Calculator
{
	public double calculate(String expression, String x) throws Exception;
}
