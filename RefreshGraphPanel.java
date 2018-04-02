import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.AffineTransform;

import javax.swing.JTextField;
import javax.swing.JFrame;
import javax.swing.JPanel;

public class RefreshGraphPanel extends JPanel implements MouseListener {
	String expression;
	double[] xValues;
	double[] yValues;
	String[] yPrintValues;
	
	int[][] functPointLocations;
	
	int xBase = 15;
	int yBase = 15;
	int rightMargin = 0;
	int topMargin = 0;
	
	double yMin;
	double yMax;
	double deltaX;
	
	JFrame          coordWindow = new JFrame();
	JTextField      xCoord      = new JTextField();
	JTextField      yCoord      = new JTextField();
	
	GraphingCalculator gc;
	
	public RefreshGraphPanel(GraphingCalculator gc,
							 String		expression,
							 double[] 	xValues,
							 double[] 	yValues) throws IllegalArgumentException, Exception {
		
		this.gc = gc;
		this.expression = expression;
		this.xValues = xValues;
		this.yValues = yValues;
		this.deltaX  = xValues[1] - xValues [0];
		
		  //find min and max
		  yMax = yValues[0];
		  yMin = yValues[0];
		  for(int i=0;i<yValues.length; i++) {
			  if(yValues[i] > yMax) yMax = yValues[i];
			  if(yValues[i] < yMin) yMin = yValues[i];
		  }
		
		this.yPrintValues = calcYAxisPrintValues(yMin, yMax);
		for(String item: yPrintValues) {
			System.out.println(item);
		}
		
		
		this.functPointLocations = new int[xValues.length][yValues.length];
		
		// Window for displaying points from mouse press
		coordWindow.getContentPane().add(xCoord, "North");
		coordWindow.getContentPane().add(yCoord, "South");
		coordWindow.setLocation(0,0);
		coordWindow.setSize(300, 100);
		coordWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		
		this.addMouseListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
	    int windowWidth  = this.getWidth();
	    int windowHeight = this.getHeight(); 
	    
	    super.paint(g);

	    drawGraph(expression, windowWidth, windowHeight, g);
	
	   // System.out.println("Current graph size is " + windowWidth + " x " + windowHeight);
	}
	
	public void drawGraph(String expression, int winWidth, int winHeight, Graphics g) {
		g.setFont(new Font("Times Roman", Font.PLAIN, 12));
	    //draw x-axis
	    int x_axis_length_pxls = winWidth - xBase - rightMargin; 
	    int deltaPX = x_axis_length_pxls / xValues.length;
		double xConversionFactor;
	  
	    //draw x-axis
	    for(int i=0;i<xValues.length;i++) {
	    	xConversionFactor = xBase + deltaPX*i;
	    	
	    	if(xValues[i] == 0) {
	    		g.setColor(Color.green);
	    		g.drawLine((int)xConversionFactor + 1, 0, (int) xConversionFactor + 1, winHeight);
	    		g.setColor(Color.black);
	    	}
	    	g.drawString("|", (int) xConversionFactor, winHeight);
	    	functPointLocations[i][0] = (int) xConversionFactor;
	    }
	    
	    g.setFont(new Font("Times Roman", Font.PLAIN, 10));
	    for(int i=0;i<xValues.length;i++) {
	    	xConversionFactor = xBase + deltaPX*i;
	    	g.drawString(Double.toString(xValues[i]), (int) xConversionFactor + 5, winHeight-1);
	    }
	    
	    //draw y axis
	    int y_axis_length_pxls = winHeight - yBase - topMargin; 
	    int deltaPY = y_axis_length_pxls / yPrintValues.length; //deltaPY is how many pixels between each Y point on graph
	    
	    int yConversionFactor;
	    
	    g.setFont(new Font("Times Roman", Font.PLAIN, 10));
	    for(int i=0;i<yPrintValues.length;i++) {
	    	yConversionFactor = winHeight - yBase - deltaPY*i;
	    	g.drawString(yPrintValues[i], 1 , yConversionFactor - 3);
	    }
	    
	    for(int i=0;i<yPrintValues.length;i++) {
	    	yConversionFactor = winHeight - yBase - deltaPY*i;
	    	g.drawLine(0, yConversionFactor, 8, yConversionFactor);
	    	
	    	if(Double.parseDouble(yPrintValues[i]) == 0) {
	    		g.setColor(Color.green);
	    		g.drawLine(xBase, (int)yConversionFactor, winWidth - rightMargin, (int) yConversionFactor);
	    		g.setColor(Color.black);
	    	}
	    }  
	    
	    //calculate y points on graph
	    Double axis_Min = Double.parseDouble( yPrintValues[0]);
	    Double axis_Max = Double.parseDouble(yPrintValues[yPrintValues.length - 1]);
	    
	    double rangeRatio, axisPxls;
	    double totalValidGraphPixels = deltaPY * (yPrintValues.length - 1);
	    for(int i=0;i<xValues.length;i++) {
	    	rangeRatio = (yValues[i]- axis_Min)/(axis_Max - axis_Min);
	    	axisPxls = rangeRatio * totalValidGraphPixels;	    	
	    	functPointLocations[i][1] =  winHeight - yBase - (int) Math.round(axisPxls);
	    }
	    
	    //graph points
	    g.setColor(Color.red);
	    for(int i=0; i < xValues.length; i++) {
	    	g.drawOval(functPointLocations[i][0],functPointLocations[i][1],2,2);//draw dots
	    	if(i+1 < xValues.length)
	    		g.drawLine(functPointLocations[i][0]+1, functPointLocations[i][1]+1, functPointLocations[i + 1][0]+1, functPointLocations[i+1][1]+1);
	    }
	}
	
	public String[] calcYAxisPrintValues(double yMin, double yMax) throws Exception{
		
	  double dPlotRange;
	  
	  int    plotRange, initialIncrement, upperIncrement, 
	         lowerIncrement, selectedIncrement, numberOfYscaleValues,
	         lowestYscaleValue, highestYscaleValue;
	  String zeros = "0000000000";
	  
	  // 1) Determine the RANGE to be plotted.
	  dPlotRange = yMax - yMin;
	  System.out.println("Plot range (Ymax-Ymin) = " + dPlotRange);

	  // 2) Determine an initial increment value.
	  if (dPlotRange > 10)
	     {
		 plotRange = (int)dPlotRange;
		 System.out.println("Rounded plot range = " + plotRange);
	     }
	 else {
		 throw new Exception("Function range too small to graph. ");
	     }
	/*ASSUME*/ // 10 scale values as a starting assumption.
	  initialIncrement = plotRange/10;
	  System.out.println("Initial increment value = " + initialIncrement);
	  // Please excuse this clumsy "math"!
	  String initialIncrementString = String.valueOf(initialIncrement);
	  
	  // 3) Find even numbers above and below the initial increment. 
	  String leadingDigit = initialIncrementString.substring(0,1);
	  int leadingNumber = Integer.parseInt(leadingDigit);
	  int bumpedLeadingNumber = leadingNumber + 1;
	  String bumpedLeadingDigit = String.valueOf(bumpedLeadingNumber);
	  String upperIncrementString = bumpedLeadingDigit + zeros.substring(0,initialIncrementString.length()-1);
	  String lowerIncrementString = leadingDigit       + zeros.substring(0,initialIncrementString.length()-1);
	  upperIncrement = Integer.parseInt(upperIncrementString);
	  lowerIncrement = Integer.parseInt(lowerIncrementString);
	  System.out.println("Upper increment alternative = " + upperIncrement);
	  System.out.println("Lower increment alternative = " + lowerIncrement);

	  // 4) Pick the upper or lower even increment depending on which is closest.
	  int distanceToUpper = upperIncrement - initialIncrement;
	  int distanceToLower = initialIncrement - lowerIncrement;
	  if (distanceToUpper > distanceToLower)
		  selectedIncrement = lowerIncrement;
	    else
	      selectedIncrement = upperIncrement;
	  System.out.println("The closest even increment (and therefore the one chosen) = " + selectedIncrement);

	  // 5) Determine lowest Y scale value
	  numberOfYscaleValues = 0;
	  lowestYscaleValue    = 0;
	  if (yMin < 0)
	     {
	     for (; lowestYscaleValue > yMin; lowestYscaleValue-=selectedIncrement)
	          numberOfYscaleValues++;
	     }
	  if (yMin > 0)
	     {
		 for (; lowestYscaleValue < yMin; lowestYscaleValue+=selectedIncrement)
		      numberOfYscaleValues++;
	     numberOfYscaleValues--;
	     lowestYscaleValue -= selectedIncrement;
	     }
	  System.out.println("The lowest Y scale value will be " + lowestYscaleValue + ")");
	  
	  
	  // 6) Determine upper Y scale value
	  numberOfYscaleValues = 1;
	  for (highestYscaleValue = lowestYscaleValue; highestYscaleValue < yMax; highestYscaleValue+=selectedIncrement)
		  numberOfYscaleValues++;
	  System.out.println("The highest Y scale value will be " + highestYscaleValue);
	  System.out.println("The number of Y scale click marks will be " + numberOfYscaleValues);
	  if ((numberOfYscaleValues < 5) || (numberOfYscaleValues > 20))
	     {
		 System.out.println("Number of Y scale click marks is too few or too many!");
		 return new String[0]; //empty string
	     }
	  // 7) Determine if Y scale will be extended to include the 0 point.
	  if ((lowestYscaleValue < 0) && (highestYscaleValue > 0))
	       System.out.println("The Y scale includes the 0 point.");
	   else // Y scale does not include 0.
	     {   //	Should it be extended to include the 0 point?
	     if ((lowestYscaleValue > 0) && (lowestYscaleValue/selectedIncrement <= 3))
	        {
	    	lowestYscaleValue = 0;
	    	System.out.println("Lower Y scale value adjusted down to 0 to include 0 point. (Additional click marks added.)");
	        }
	     if ((highestYscaleValue < 0) && (highestYscaleValue/selectedIncrement <= 3))
	        {
	     	highestYscaleValue = 0;
	    	System.out.println("Upper Y scale value adjusted up to 0 to include 0 point. (Additional click marks added.)");
	        }
	     }
	  int yScaleValue = lowestYscaleValue;
	  int numValues=0;
	  while(yScaleValue < highestYscaleValue){
		   yScaleValue += selectedIncrement;
		   numValues++;
	  }
	  String[] yScalePrintValues = new String[numValues];
	  for(int i=0; i<numValues;i++) {
		  yScalePrintValues[i] = Double.toString(lowestYscaleValue + selectedIncrement * i);
	  }
	  return yScalePrintValues;
	}      
	
	@Override
	public void mouseClicked(MouseEvent me) {
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent me) {
		// Draw x, y coordinate of function at x-value of pressed point
		double xInPixels = me.getX() - xBase;
		double xFraction = (xInPixels / (this.getWidth() - xBase - rightMargin));
		double xValue = xValues[0] + (deltaX) * (xFraction * xValues.length); 
		double yValue;
		//System.out.println("X = " + xValues[0] + " X2 = " + xValues[1]);
		try
		{
			yValue = gc.calculate(expression, Double.toString(xValue));
		} catch(Exception e){return;}
		xCoord.setText("X = " + Double.toString(xValue));
		yCoord.setText("Y = " + Double.toString(yValue));
		coordWindow.setVisible(true);
	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		coordWindow.setVisible(false);
	}

}
