package swing;

import java.awt.Color;
import java.awt.Graphics;

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
		LEFT, RIGHT, UP, DOWN
	}
	
	private int positionX = 450;
	private int positionY = 350;
	private Direction dir = Direction.UP;
	private boolean drawNeverCalledBefore = true;
	private Connection con = Connection.getInstance();
	private Move lastMove = Move.FWD;
	private static final long serialVersionUID = 625962120099128913L;

	public void draw(Move curMove) throws InterruptedException {
		if (curMove == Move.LEFT) {
			if (dir == Direction.LEFT) {
				drawDown();
				dir = Direction.DOWN;
			} else if (dir == Direction.RIGHT) {
				drawUp();
				dir = Direction.UP;
			} else if (dir == Direction.UP) {
				drawLeft();
				dir = Direction.LEFT;
			} else {
				drawRight();
				dir = Direction.RIGHT;
			}
		} else if (curMove == Move.RIGHT) {
			if (dir == Direction.LEFT) {
				drawUp();
				dir = Direction.UP;
			} else if (dir == Direction.RIGHT) {
				drawDown();
				dir = Direction.DOWN;
			} else if (dir == Direction.UP) {
				drawRight();
				dir = Direction.RIGHT;
			} else {
				drawLeft();
				dir = Direction.LEFT;
			}
		} else if (curMove == Move.FWD) {
			if (drawNeverCalledBefore) {
				drawUp();
				dir = Direction.UP;
			} else if (lastMove == Move.BWD) {
				if (dir == Direction.LEFT) {
					drawRight();
					dir = Direction.RIGHT;
				} else if (dir == Direction.RIGHT) {
					drawLeft();
					dir = Direction.LEFT;
				} else if (dir == Direction.UP) {
					drawDown();
					dir = Direction.DOWN;
				} else {
					drawUp();
					dir = Direction.UP;
				}
			}
		} else if (curMove == Move.BWD) {
			if (drawNeverCalledBefore) {
				drawDown();
				dir = Direction.DOWN;
			} else if (lastMove == Move.FWD) {
				if (dir == Direction.LEFT) {
					drawRight();
					dir = Direction.RIGHT;
				} else if (dir == Direction.RIGHT) {
					drawLeft();
					dir = Direction.LEFT;
				} else if (dir == Direction.UP) {
					drawDown();
					dir = Direction.DOWN;
				} else {
					drawUp();
					dir = Direction.UP;
				}
			}
		} else if (curMove == Move.IDLE) {
		}
		drawNeverCalledBefore = false;
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
		positionY++;
		drawPoint(positionX, positionY);
	}

	private void drawDown() {
		System.out.println(" ### Drawing Line DOWN ###");
		drawPoint(positionX, positionY);
		positionY--;
		drawPoint(positionX, positionY);
	}

	public void drawPoint(int x, int y) {
		this.setBackground(Color.WHITE);
		Graphics g = this.getGraphics();
		
		if (g == null) // Don't bother if we've got no Grahphics to work with
			return; 
		
		g.fillOval(x, y, 5, 5);

	}

	@Override
	public void run() {
		while (true) {
			try {
				draw(con.getCurMove());
				Thread.sleep(200);
			} catch (InterruptedException e) {
				System.err.println(e.getMessage());
			}

		}
	}
}
