package swing;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import javax.swing.JPanel;
/**
 *
 * 
 * @author Tobias Fleischer
 * 
 */
public class Draw  extends JPanel{
	
	
	
	private static final long serialVersionUID = 625962120099128913L;
	public Draw() {
		
		// Nur zum testen der Funktion des zeichnens
		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent arg0) {
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				drawPoint(arg0.getX(), arg0.getY());
				// Ausgabe der Mauskoordinaten in der Kommandozeile
				System.out.println(arg0.getX() + " / " + arg0.getY());
			}
		});
	}
	//
	public void drawPoint(int x, int y) {
		this.setBackground(Color.WHITE);
		Graphics g = this.getGraphics();
		
		g.fillOval(x, y, 3, 3);
		
	}
	
}
