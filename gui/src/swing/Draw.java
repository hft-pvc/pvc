package swing;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.util.Vector;

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
	private Connection con = Connection.getInstance();
	private Move lastMove = Move.FWD;
	private static final long serialVersionUID = 625962120099128913L;
	private Vector<Point> points = new Vector<Point>();

	public Draw() {
		// Muss so sein sonst kann man keine Bild verschieben!!!
		this.setLayout(null);
		//
		roboter = new JLabel();
		robot = toolkit.getImage(pngPfad);
		roboter.setIcon(new ImageIcon(robot));
		this.add(roboter);
		roboter.setBounds(positionX - 16, positionY - 32, 37, 32);
	}

	public void draw(Move curMove) throws InterruptedException {
		con.setDraw(false);
		// if only at the first run true
		if (con.getDrawNeverCalledBefore()) {
			System.out.println("EIN MAL");
			con.setDrawNeverCalledBefore(false);
			if (curMove == Move.FWD) {
				drawUp();
				this.dir = Direction.UP;
			} else if (curMove == Move.BWD) {
				drawDown();
				this.dir = Direction.DOWN;
			} else if (curMove == Move.LEFT) {
				drawLeft();
				this.dir = Direction.LEFT;
			} else if (curMove == Move.RIGHT) {
				drawRight();
				this.dir = Direction.RIGHT;
			}
			// last direction UP or Down
		} else if (this.dir == Direction.UP || this.dir == Direction.DOWN) {
			if (curMove == Move.FWD) {
				drawUp();
			} else if (curMove == Move.BWD) {
				drawDown();
				this.dir = Direction.DOWN;
			} else if (curMove == Move.LEFT) {
				drawLeft();
				this.dir = Direction.LEFT;
			} else if (curMove == Move.RIGHT) {
				drawRight();
				this.dir = Direction.RIGHT;
			}
			// last direction left
		} else if (this.dir == Direction.LEFT) {
			if (curMove == Move.FWD) {
				drawLeft();
			} else if (curMove == Move.BWD) {
				drawRight();
				this.dir = Direction.LEFT;
			} else if (curMove == Move.LEFT) {
				drawDown();
				this.dir = Direction.DOWN;
			} else if (curMove == Move.RIGHT) {
				drawUp();
				this.dir = Direction.UP;
			}
			// last direction right
		} else if (this.dir == Direction.RIGHT) {
			if (curMove == Move.FWD) {
				drawRight();
				;
			} else if (curMove == Move.BWD) {
				drawLeft();
				this.dir = Direction.RIGHT;
			} else if (curMove == Move.LEFT) {
				drawUp();
				;
				this.dir = Direction.UP;
			} else if (curMove == Move.RIGHT) {
				drawDown();
				;
				this.dir = Direction.DOWN;
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
		
		Point curPoint = new Point();
		curPoint.setLocation(x, y);
		
		//move the robot
		roboter.setLocation(x - 16, y - 32);
		
		//draw the curPoint
		g.fillOval(curPoint.x, curPoint.y, 5, 5);
		
		//Draw the old points...
		for (int i = 0; i < points.size(); i++) {
			Point p = points.get(i);
			g.fillOval(p.x, p.y, 5, 5);
		}
		//Add the curPoint to the vector
		points.add(curPoint);
	}

	@Override
	public void run() {
		while (true) {
			try {
				if (con.getDraw()) {
					draw(con.getCurMove());
				}
				Thread.sleep(500);
				// con.setDraw(false);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}

		}
	}
}
