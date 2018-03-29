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
	    
	    System.out.println("Current graph size is " + windowWidth + " x " + windowHeight);
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
