/* ExpressionCalculator
 *  
 * Developed for ECE 309 Lab 9
 *
 * 
 * Allows for calculations using operators '()','^' or 'r', '*' or '/', '+' or '-' operators
 * in said order of precendence. 'ArB' indicates a Bth root of A
 * 
 * Allows for use of pi, e and x symbols 
 *
 * **Note, unary '-' or negation is supported by the interface
 * **Note, complex numbers in the form 'a+bi' are supported by
 *   the interface
 *
 * -----------------------------------------------------------
 */

import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.*;
import javax.swing.text.DefaultCaret;

public class GraphingCalculator implements Calculator, ActionListener
{
	/* --- Comp. # Type --- */
	public class Complex 
	{
		double real;
		double imag;
		
		public Complex(double real_num, double imag_num)
		{
			this.real = real_num;
			this.imag = imag_num;
		}
	}
	
	/* --- Constants --- */
    final String CENTER = "Center";
	final String TOP    = "North";
	final String RIGHT  = "East";
	final String BOTTOM = "South";

	/* ---GUI objects--- */
	JFrame          calculatorWindow = new JFrame("Expression Calculator"); 
	JFrame 			graphWindow;
	RefreshGraphPanel graphPanel;
	JPanel          buttonPanel      = new JPanel();
	JPanel          entryPanel       = new JPanel();
	JPanel          expressionPanel  = new JPanel();
	JPanel          varPanel         = new JPanel();
	JPanel          outputPanel      = new JPanel();
	JTextArea       resultsDisplay   = new JTextArea();
	JScrollPane     resultsScrollP   = new JScrollPane(resultsDisplay);
	JTextArea       errorDisplay     = new JTextArea("Errors will be displayed here");
	JTextField      expressionEntry  = new JTextField("Enter an expression here");
	JTextField      xValueEntry      = new JTextField();
	JTextField		deltaXEntry		 = new JTextField();
	JTextField      numPtsEntry      = new JTextField();
	JLabel		    xValueLabel      = new JLabel("For X =");
	JLabel			deltaXLabel		 = new JLabel("Delta X: ");
	JLabel          numPtsLabel      = new JLabel("Num pts: ");
	JButton         clearButton      = new JButton("Clear");
    JButton         recallButton     = new JButton("Recall");

	/* ---Class Fields--- */
	private boolean x_used;
	private boolean error_encountered;
	private final int DEFAULT_NUM_PTS = 11;
	private Complex result = new Complex(0, 0);
	private String  lastExpressionEntered      = "";
	private String  lastXvalueEntered          = "";														// Will allow us to use Reverse Polish Notation
	private Stack<Integer>       parStack      = new Stack<>(); 														// Keeps index of right parentheses for splitting string
	private LinkedList<Complex>       compStack     = new LinkedList<>();															// Stack for storing results from rpn parsing
	private final Set<Character> operators     = new HashSet<Character>(Arrays.asList('+', '-', '*', '/', 'r', '^'));   // Set of valid operators for checking against implicit multiplication
	private final Set<Character> doNotCheckSet = new HashSet<Character>(Arrays.asList(' ', '('));                       // Set of characters to avoid recalling for checking against implicit multiplication
	
	
	public GraphingCalculator()
	{
		calculatorWindow.getContentPane().add(buttonPanel, RIGHT);
			buttonPanel.setLayout(new GridLayout(2,1));
			buttonPanel.add(clearButton);
			buttonPanel.add(recallButton);
		calculatorWindow.getContentPane().add(entryPanel, TOP);
			entryPanel.setLayout(new GridLayout(2,1));
			entryPanel.add(expressionPanel);
			entryPanel.add(errorDisplay);
			errorDisplay.setEditable(false);
			expressionPanel.setLayout(new GridLayout(1, 3));//1,2
			expressionPanel.add(varPanel);
			expressionPanel.add(expressionEntry);
			varPanel.setLayout(new GridLayout(1,6));
			varPanel.add(deltaXLabel);
			deltaXLabel.setHorizontalAlignment(JLabel.RIGHT);
			varPanel.add(deltaXEntry);
			varPanel.add(xValueLabel);
			xValueLabel.setHorizontalAlignment(JLabel.RIGHT);
			varPanel.add(xValueEntry);
			varPanel.add(numPtsLabel);
			numPtsLabel.setHorizontalAlignment(JLabel.RIGHT);
			varPanel.add(numPtsEntry);
		calculatorWindow.getContentPane().add(resultsScrollP, CENTER);
			resultsDisplay.setEditable(false);
			DefaultCaret caret = (DefaultCaret) resultsDisplay.getCaret();
			caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
			
		calculatorWindow.setLocation(0, 0);
		calculatorWindow.setSize(1000, 400);
		calculatorWindow.setVisible(true);
		calculatorWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		clearButton.addActionListener(this);
		recallButton.addActionListener(this);
			
		/* Enter key redirection */
		expressionEntry.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
					calculate(expressionEntry.getText(), xValueEntry.getText());
					if(error_encountered)
						return;
					
					
					while(!compStack.isEmpty())
						compStack.remove();
					
					if(x_used)
						if(result.imag != 0)
							if(result.real != 0) // Complex answer
								resultsDisplay.append(expressionEntry.getText() + " = " + result.real + " + " + result.imag + "i for x = " + xValueEntry.getText() + "\n");
							else
								resultsDisplay.append(expressionEntry.getText() + " = " + result.imag + "i for x = " + xValueEntry.getText() + "\n");
						else
							resultsDisplay.append(expressionEntry.getText() + " = " + result.real + " for x = " + xValueEntry.getText() + "\n");
								
					else
						if(result.imag != 0)
							if(result.real != 0)
								resultsDisplay.append(expressionEntry.getText() + " = " + result.real + " + " + result.imag + "i\n");
							else
								resultsDisplay.append(expressionEntry.getText() + " = " + result.imag + "i \n");
						else
							resultsDisplay.append(expressionEntry.getText() + " = " + result.real + "\n");
					
					if (!deltaXEntry.getText().isEmpty()) {
						//does expression have x?
						// is delta x > 0?
						//open new graphing window
						errorDisplay.setText(""); //just in case other errors were shown before
						errorDisplay.setBackground(Color.white);
						try 
						{
							if (!expressionEntry.getText().contains("x"))
								throw new Exception("Expression does not contain symbolic x value for graphing");
							if (Double.parseDouble(deltaXEntry.getText()) < 0)
								throw new Exception("Delta x value cannot be negative.");
						}
						catch (Exception e) {
							errorDisplay.setText(e.toString());
							expressionEntry.setText("");
							errorDisplay.setBackground(Color.pink);
							if(!parStack.isEmpty())
							{
								// Clear parentheses stack
								while(!parStack.isEmpty())
								parStack.pop();
							}
							return;
						}
						
						//build graph window
						graphWindow = new JFrame(expressionEntry.getText());
						if(numPtsEntry.getText().trim().equals(""))
							// Print default 11 points
							graphPanel	= new RefreshGraphPanel(new GraphingCalculator(0), expressionEntry.getText(),
															CalculateXAxisValues(Double.parseDouble(xValueEntry.getText()), Double.parseDouble(deltaXEntry.getText()), DEFAULT_NUM_PTS),
															CalculateYAxisValues(Double.parseDouble(xValueEntry.getText()), Double.parseDouble(deltaXEntry.getText()), DEFAULT_NUM_PTS));
						else
							// Print specified number of points
							graphPanel	= new RefreshGraphPanel(new GraphingCalculator(0), expressionEntry.getText(),
															CalculateXAxisValues(Double.parseDouble(xValueEntry.getText()), Double.parseDouble(deltaXEntry.getText()), Integer.parseInt(numPtsEntry.getText())),
															CalculateYAxisValues(Double.parseDouble(xValueEntry.getText()), Double.parseDouble(deltaXEntry.getText()), Integer.parseInt(numPtsEntry.getText())));
						graphPanel.setBackground(Color.white);
						graphWindow.getContentPane().add(graphPanel, CENTER);
						
						graphWindow.setLocation(0, 0);
						graphWindow.setSize(500, 400);
						graphWindow.setVisible(true);
						graphWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
					}
					
					expressionEntry.setText("");
					
				}
			}
		});
	
		xValueEntry.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent ke) {
				if(ke.getKeyCode() == KeyEvent.VK_ENTER) {
					expressionEntry.requestFocusInWindow();
				}
			}
		});
	}
	
	// GraphingCalculator worker object (no GUI)
	public GraphingCalculator(int becauseImSpecial) {}
	
	@Override
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource() == clearButton)
		{
			xValueEntry.setText("");
			expressionEntry.setText("");
			expressionEntry.requestFocusInWindow();
		}
		if(ae.getSource() == recallButton)
		{
			xValueEntry.setText(lastXvalueEntered);
			expressionEntry.setText(lastExpressionEntered);
			expressionEntry.requestFocusInWindow();
		}
	}
	
	public double[] CalculateYAxisValues(double startPoint, double delta, int numPts) {
		//compute the y axis values for each x in the given expression
		//right now, it is returning a test vector
		//so CHANGE THIS
		
		double[] y_vals = new double[numPts+1];
		for(int i=0;i<numPts+1;i++) {
			y_vals[i] = calculate(expressionEntry.getText(), Double.toString(startPoint+i*delta));
		}
		return y_vals;
	}
	
	public double[] CalculateXAxisValues(double startPoint, double delta, int numPts) {
		boolean hasZero = false;
		
		for (int i=0;i<numPts-1;i++) {
			if ((startPoint + delta*i)==0) hasZero = true;
		}
		
		if (hasZero) {
			double[] x_vals = new double[numPts];
			for(int i=0;i<numPts;i++) {
				x_vals[i] = startPoint + delta*i;
			}
			return x_vals;
		}
		
		double[] x_vals = new double[numPts-1];
		for(int i=0;i<numPts-1;i++) {
			x_vals[i] = startPoint + delta*i;
		}
		return x_vals;
	}

	@Override
	public double calculate(String expression, String x)
	{
		double tmpResult;
		
		if(expressionEntry.getText().contains("x"))
			x_used = true;
		
		// Save modified expression and x value
		expression = expression.toLowerCase();
		lastExpressionEntered = expression;
		lastXvalueEntered = String.valueOf(x);
		expression = expression.replaceAll("\\s+", "");              // Remove all white space from input
		expression = expression.replaceAll("pi", "p");               // Ensure that all variables are a single character
		
		// Run computation, returning value on the complex stack
		try {
			try
			{
				if(expression.startsWith("+"))
					throw new Exception("Unary \'+\' not supported. Please consider the delightful unary \'-\' as an alternative.");
				parseInput(expression, Double.parseDouble(x));
				if(result.imag != result.imag || result.real != result.real)
					throw new Exception("Error: DIvision by zero.");
			}
			catch(NumberFormatException nfe)
			{
				if(expressionEntry.getText().contains("x"))
					throw new Exception("Attempted calculation using variable x with invalid or no listed value. Please enter an single, numerical x value in the upper left box.");
				if(expressionEntry.getText().isEmpty())
					throw new Exception("No expression entered. Please enter an expression in the upper right box.");
				calculate(expressionEntry.getText(), "0");
					x_used = false;
			}	
		}
		catch(Exception e)
		{
			error_encountered = true;
			errorDisplay.setText(e.toString());
			expressionEntry.setText("");
			errorDisplay.setBackground(Color.pink);
			System.out.println(e);
			if(!parStack.isEmpty())
			{
				// Clear parentheses stack
				while(!parStack.isEmpty())
				parStack.pop();
			}
			return 0;
		}
		
		// Return proper result
		error_encountered = false;
		result = compStack.remove();
		if(result.imag == 0)
			return result.real;
		else 
			return 0;
	}

	// Locates beginning and ending address of each expression; sends expressions to be evaluated
	public void parseInput(String expression, double x) throws Exception
	{
		String toEvaluateNS = expression.toLowerCase();
		// Check for character-based implicit multiplication
		if(toEvaluateNS.matches("(.*)[a-z&&[^eiprx]](.*)"))
			throw new Exception("Unrecognized variable used. Please only include variables e, pi, i and x");
		if(toEvaluateNS.matches("(.*)[0-9|e|i|p]p(.*)") || toEvaluateNS.matches("(.*)p[0-9|e|i|p](.*)") || toEvaluateNS.contains("pp")
		|| toEvaluateNS.matches("(.*)[0-9|e|i|p]e(.*)") || toEvaluateNS.matches("(.*)e[0-9|e|i|p](.*)") || toEvaluateNS.contains("ee")
	    || toEvaluateNS.matches("(.*)[0-9|e|i|p]x(.*)") || toEvaluateNS.matches("(.*)x[0-9|e|i|p](.*)") || toEvaluateNS.contains("xx"))
			throw new Exception("Implicit multiplication cannot be computed. Please list \'*\' between variables to multiply.");
		if(toEvaluateNS.matches("(.*)[0-9|a-z]i(.*)") || toEvaluateNS.matches("(.*)i[0-9|a-z](.*)") || toEvaluateNS.contains("ii"))
			throw new Exception("Implicit multiplication cannot be computed. Please list \'*\' between i and numbers to multiply.");
		
		Character lastChar = '(';
		// Parse expression until no parentheses remain
		for(int stringIndex = 0; stringIndex < expression.length(); stringIndex++)
		{
			if(!doNotCheckSet.contains(expression.charAt(stringIndex)))
				lastChar = expression.charAt(stringIndex);
			if(expression.charAt(stringIndex) == '(')
				if(stringIndex != 0 && lastChar != '(' && !operators.contains(lastChar)) 
					throw new Exception("Implicit multiplication cannot be computed. Please list \'*\' before a \'(\'");
				else
				{
					parStack.push(stringIndex);
				}
					
			if(expression.charAt(stringIndex) == ')')
				{
					// Evaluate the parentheses-enclosed expression; revert parsing index to last '(' + 1 or beginning
					if(parStack.isEmpty())
						throw new Exception("An additional \')\' is included without a pair");
					expression = evaluateExpression(expression, parStack.pop(), stringIndex + 1, x); //include final ')'
					
					if(parStack.isEmpty())
						stringIndex = 0;
					else
						stringIndex = parStack.peek() + 1;                                               
				}
		}
		if(!parStack.isEmpty())
		{
			// Clear parentheses stack
			while(!parStack.isEmpty())
				parStack.pop();
			throw new Exception("An additional \'(\' is included without a pair");
		}
		// Run final parse of expression		
		expression = evaluateExpression(expression, 0, expression.length(), x);
	}
	
	
	public String evaluateExpression(String expression, int leftIndex, int rightIndex, double x) throws Exception
	{
		String toEvaluate = expression.substring(leftIndex, rightIndex);
		String toEvaluateNS = toEvaluate.replace(" ", "");    // Used to check for implicit multiplication
		expression = expression.replaceFirst(Pattern.quote(toEvaluate), "c");
		// Check for erroneous operators
		if(toEvaluate.contains("%"))
			throw new Exception("Invalid operator \'%\' included.");
		
		
		toEvaluate = toEvaluate.replaceAll("[()]", "");
		toEvaluate = toEvaluate.replaceAll("r", "%");
		toEvaluate = toEvaluate.replaceAll("x", Double.toString(x));	
		toEvaluate = toEvaluate.replaceAll("p", Double.toString(Math.PI));	
		toEvaluate = toEvaluate.replaceAll("e", Double.toString(Math.exp(1)));	
		toEvaluate = toEvaluate.replaceAll("x", " " + Double.toString(x) + " ");
		execRPN(orgRPNStr(toEvaluate));
		
		return expression; //return result as string
	}
	
	//arrange expression in RPN form
	public String orgRPNStr(String expression) throws Exception{
		//powers
		expression = setUniform(expression);
		Stack<String> strSt = new Stack<>();
		char c;
		String temp;
		String binaryPattern;
		String exMatch = "((u?)([0-9]*[.])?[0-9|x-z|p|e|i|c]+)"; //number or x,y,z
		
		//unary (-)
		if ((expression.charAt(0) == '-') && (Character.isDigit(expression.charAt(2))))
			expression = expression.replaceFirst("\\- ", "\\u");
		expression = expression.replaceAll("([^0-9])\\s(\\-)(\\s)([0-9])", "$1 \\u$4");
		
		for(int i=0;i<expression.length();i++) {
			c = expression.charAt(i);
			if ((c == '^') || (c == '%')) {
				binaryPattern = exMatch + "\\s\\" + Character.toString(c) + "\\s" + exMatch;
				Pattern p = Pattern.compile(binaryPattern);
				Matcher m = p.matcher(expression);
				
				m.find();
				temp = m.group(0);
				expression = expression.replaceFirst(Pattern.quote(temp), "x");
				temp = shiftToEnd(temp,c);
				i = 0;
				strSt.push(temp);
			}
		}
		
		for(int i=0;i<expression.length();i++) {
			c = expression.charAt(i);
			if ((c == '*') || (c == '/')) {
				binaryPattern = exMatch + "\\s\\" + Character.toString(c) + "\\s" + exMatch;
				Pattern p = Pattern.compile(binaryPattern);
				Matcher m = p.matcher(expression);
				
				m.find();
				temp = m.group(0);
				expression = expression.replaceFirst(Pattern.quote(temp), "y");
				temp = shiftToEnd(temp,c);
				strSt.push(temp);
				i = 0;
			}
		}
		
		for(int i=0;i<expression.length();i++) {
			c = expression.charAt(i);
			if ((c == '+') || (c == '-')) {
				binaryPattern = exMatch + "\\s\\" + Character.toString(c) + "\\s" + exMatch;
				Pattern p = Pattern.compile(binaryPattern);
				Matcher m = p.matcher(expression);
				
				m.find();
				temp = m.group(0);
				expression = expression.replaceFirst(Pattern.quote(temp), "z");
				temp = shiftToEnd(temp,c);
				i = 0;
				strSt.push(temp);
			}
		}
		if(strSt.empty())return expression;
		return buildRPNExp(strSt);
	}
	
	public String buildRPNExp(Stack<String> strSt) {
		String rpnExp;
		rpnExp = strSt.pop();
		while(rpnExp.contains("z")) rpnExp = replaceLast(rpnExp, "z", strSt.pop());
		while(rpnExp.contains("y")) rpnExp = replaceLast(rpnExp, "y", strSt.pop());
		while(rpnExp.contains("x")) rpnExp = replaceLast(rpnExp, "x", strSt.pop());	
		return rpnExp;
	}
	
	//support function to replace last instance of string in string
	public String replaceLast(String string, String toReplace, String replacement) {
	    int pos = string.lastIndexOf(toReplace);
	    if (pos > -1) {
	        return string.substring(0, pos)
	             + replacement
	             + string.substring(pos + toReplace.length(), string.length());
	    } else {
	        return string;
	    }
	}
	
	//arrange string so that there is a space between every number, letter and operator
	public String setUniform(String s) throws Exception {
		String numMatch = "([0-9]*[.])?[0-9|p|e|i|c]+";
		
		s = s.replaceAll("(" + numMatch + ")([^\\.0-9])", "$1 $3");
		s = s.replaceAll("([^\\.0-9])(" + numMatch + ")", "$1 $2");
		s = s.replaceAll("([\\+|\\-|*|/|r|%|\\^])([-])", "$1 $2");
		s = s.replaceAll("\\s+", " ");
		return s;
	}
	
	//extracts c from string and places at end of string
	public String shiftToEnd(String s, char c) {
		String r = "";
	    for (int i = 0; i < s.length(); i++) {
	      if (s.charAt(i) != c)
	        r += s.charAt(i);
	    }
	    r+=" " + c;
	    return r.replaceAll("\\s+",  " ");
	}
	
	public void execRPN(String expression) throws Exception {
		Stack<Complex> rpnStack = new Stack<>();
		Complex        operand1;
		Complex        operand2;
		
				for(String token: expression.split("\\s+")){
						switch (token) {
						case "p":
							 rpnStack.push(new Complex(Math.PI, 0));
							break;
						case "e":
							 rpnStack.push(new Complex(Math.E, 0));
							break;
			            case "+":
							operand1 = rpnStack.pop();
							operand2 = rpnStack.pop();
			                rpnStack.push(new Complex(operand1.real+operand2.real, operand1.imag+operand2.imag));
			                break;
			            case "-":
			                operand1 = rpnStack.pop();
							operand2 = rpnStack.pop();
			                rpnStack.push(new Complex(operand2.real-operand1.real, operand2.imag-operand1.imag));
			                break;
			            case "*":
							operand1 = rpnStack.pop();
							operand2 = rpnStack.pop();
			                rpnStack.push(new Complex(operand1.real*operand2.real - operand1.imag*operand2.imag, operand1.real*operand2.imag + operand1.imag*operand2.real));
			                break;
			            case "/":
			                operand2 = rpnStack.pop();
							operand1 = rpnStack.pop();
			                rpnStack.push(new Complex((operand1.real*operand2.real + operand1.imag*operand2.imag)/(Math.pow(operand2.real, 2)+Math.pow(operand2.imag, 2)), 
							                      (-operand1.real*operand2.imag + operand1.imag*operand2.real)/(Math.pow(operand2.real, 2)+Math.pow(operand2.imag, 2))));
			                break;
			            case "^":
							operand1 = rpnStack.pop();
							operand2 = rpnStack.pop();
							if(operand1.imag != 0 || operand2.imag != 0)
								throw new Exception("Complex numbers cannot use the \'^\' operator");
			                rpnStack.push(new Complex(Math.pow(operand2.real, operand1.real), 0));
			                break;
			            case "%":
			                operand1 = rpnStack.pop();
							operand2 = rpnStack.pop();
							if(operand1.imag != 0 || operand2.imag != 0)
								throw new Exception("Complex numbers cannot use the \'r\' operator");
			                rpnStack.push(new Complex(Math.pow(operand2.real, 1/operand1.real), 0));
			            	break;
						case "i":
							rpnStack.push(new Complex(0, 1));
							break;
						case "c":
							rpnStack.push(compStack.remove());
							break;
			            default:
			            	if(token.charAt(0)=='u') token = token.replace("u", "-");
			                rpnStack.push(new Complex(Double.parseDouble(token), 0));
			                break;
						}
				}
		operand1 = rpnStack.pop();
		compStack.add(operand1);
	}

	public static void main(String[] args) throws Exception
	{
		System.out.println("ExpressionCalculator.java was created by Paul Leimer and Sam Messick for ECE 309\nExpressions can be evaluated with complex numbers and a unary minus character as well\nso give it a go!");
		new GraphingCalculator();
	}
}
