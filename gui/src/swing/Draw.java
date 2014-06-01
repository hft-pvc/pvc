package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import rxtx.Connection;

/**
 * 
 * 
 * @author Tobias Fleischer
 * 
 */
public class Draw extends JPanel implements Runnable {

	public enum Move {
		LEFT, RIGHT, FWD, BWD, IDLE, STOP
	}

	public enum Direction {
		LEFT, RIGHT, UP, DOWN, IDLE
	}
	private String pngPfad = "src/robot.png";
	private Image robot;
	JLabel roboter;
	Toolkit toolkit = Toolkit.getDefaultToolkit();
	private int positionX = 450;
	private int positionY = 350;
	private Direction dir = Direction.UP;
	private boolean drawNeverCalledBefore = true;
	private Connection con = Connection.getInstance();
	private Move lastMove = Move.FWD;
	private static final long serialVersionUID = 625962120099128913L;

	public Draw() {
		//Muss so sein sonst kann man keine Bild verschieben!!!
		this.setLayout(null);
		//
		roboter = new JLabel();
		robot = toolkit.getImage(pngPfad);
		roboter.setIcon(new ImageIcon(robot));
		this.add(roboter);
		roboter.setBounds(positionX-16,positionY-32,50,35);
	}
	public void draw(Move curMove) throws InterruptedException {
	if(curMove == Move.FWD){
		if(dir == Direction.IDLE){
			
			drawUp();
		}else if (dir == Direction.UP) {
			drawUp();
		}else if (dir == Direction.LEFT) {
			drawLeft();
		}
	}
	}

	private void drawLeft() {
		System.out.println(" ### Drawing Line LEFT ###");
		drawPoint(positionX, positionY);
		positionX--;
		drawPoint(positionX, positionY);
	}

	private void drawRight() {
		System.out.println(" ### Drawing Line RIGHT ###");
		drawPoint(positionX, positionY);
		positionX++;
		drawPoint(positionX, positionY);
	}

	private void drawUp() {
		System.out.println(" ### Drawing Line UP ###");
		drawPoint(positionX, positionY);
		positionY--;
		drawPoint(positionX, positionY);
	}

	private void drawDown() {
		System.out.println(" ### Drawing Line DOWN ###");
		drawPoint(positionX, positionY);
		positionY++;
		drawPoint(positionX, positionY);
	}

	public void drawPoint(int x, int y) {
		this.setBackground(Color.WHITE);
		
		Graphics g = this.getGraphics();
		
		if (g == null) // Don't bother if we've got no Grahphics to work with
			return; 
		roboter.setLocation(x-16, y-32);

		//roboter.validate();
		//roboter.setBounds(x,y,50,50);
		g.fillOval(x, y, 5, 5);

	}

	@Override
	public void run() {
		while (true) {
			try {
				if(con.getDraw()){
				draw(con.getCurMove());
				}
				Thread.sleep(500);
				//con.setDraw(false);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}

		}
	}
}
