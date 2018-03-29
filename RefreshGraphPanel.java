import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

public class RefreshGraphPanel extends JPanel implements MouseListener {
	String expression;
	double[] xValues;
	double[] yValues;
	
	public RefreshGraphPanel(//GraphingCalculator gc,
							 String		expression,
							 double[] 	xValues,
							 double[] 	yValues) throws IllegalArgumentException {
		
		this.expression = expression;
		this.xValues = xValues;
		this.yValues = yValues;
		
		this.addMouseListener(this);
	}
	
	@Override
	public void paint(Graphics g) {
	    int windowWidth  = this.getWidth();
	    int windowHeight = this.getHeight(); 
	    
	    super.paint(g);
		g.setFont(new Font("Times Roman", Font.PLAIN, 12));
	    //draw x-axis
	    int x_axis_length_pxls = windowWidth - 50; //50 pixle buffer margin on either side
	    int deltaP = x_axis_length_pxls / xValues.length;
	    
	    for(int i=0;i<xValues.length;i++) {
	    	g.drawString("|", 50 + deltaP*i, windowHeight);
	    }
	    g.setFont(new Font("Times Roman", Font.PLAIN, 10));
	    for(int i=0;i<xValues.length;i++) {
	    	g.drawString(Double.toString(xValues[i]), 55 + deltaP*i, windowHeight-1);
	    }
	    		
	    System.out.println("Current graph size is " + windowWidth + " x " + windowHeight);
	    System.out.println(xValues.length);
	}
	
	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub

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
	public void mousePressed(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseReleased(MouseEvent arg0) {
		// TODO Auto-generated method stub

	}

}
