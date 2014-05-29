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
	
	private boolean left = true;
	private int positionX = 450;
	private int positionY = 350;
	private static final long serialVersionUID = 625962120099128913L;
	public Draw() {
		
		// Nur zum testen der Funktion des zeichnens
		this.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent arg0) {
			}

			@Override
			public void mouseDragged(MouseEvent arg0) {
				try {
					drawLeft();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				//drawPoint(arg0.getX(), arg0.getY());
			}
		});
	}
	public void drawLeft() throws InterruptedException{
		while(left == true){
			drawPoint(positionX, positionY);
			positionX--;
			wait(10);
		}
	}
	public void drawRight() throws InterruptedException{
		while(left == true){
			drawPoint(positionX, positionY);
			positionX++;
			wait(10);
		}
	}
	public void drawUp() throws InterruptedException{
		while(left == true){
			drawPoint(positionX, positionY);
			positionY++;
			wait(10);
		}
	}
	public void drawDown() throws InterruptedException{
		while(left == true){
			drawPoint(positionX, positionY);
			positionY--;
			wait(10);
		}
	}
	public void drawPoint(int x, int y) {
		this.setBackground(Color.WHITE);
		Graphics g = this.getGraphics();
		
		g.fillOval(x, y, 3, 3);
		
	}
	
}
