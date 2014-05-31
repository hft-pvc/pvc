package swing;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
import rxtx.Connection;
import swing.Draw.Move;
/**
 * 
 * 
 * @author Tobias Fleischer
 * 
 */
public class Control extends JPanel implements MouseListener {
	Connection connect;
	JButton stop;

	Control() throws IOException {
		this.connect = Connection.getInstance();
		this.setLayout(new MigLayout("",
				"[44px][24px][][44px][][24px][][][][grow][]",
				"[44px][][][grow]"));

		JLabel up = new JLabel();
		up.setName("up");
		InputStream is = getClass().getResourceAsStream("images/up.png");
		Image image = ImageIO.read(is);
		up.setIcon(new ImageIcon(image));
		up.addMouseListener(this);
		this.add(up, "cell 2 0,alignx left,aligny top");

		JLabel left = new JLabel("");
		left.setName("left");
		is = getClass().getResourceAsStream("images/left.png");
		image = ImageIO.read(is);
		left.setIcon(new ImageIcon(image));
		left.addMouseListener(this);
		this.add(left, "cell 1 1,alignx left,aligny center");

		JLabel right = new JLabel("");
		right.setName("right");
		is = getClass().getResourceAsStream("images/right.png");
		image = ImageIO.read(is);
		right.setIcon(new ImageIcon(image));
		right.addMouseListener(this);
		this.add(right, "cell 3 1,alignx left,aligny center");

		JLabel down = new JLabel("");
		down.setName("down");
		is = getClass().getResourceAsStream("images/down.png");
		image = ImageIO.read(is);
		down.setIcon(new ImageIcon(image));
		down.addMouseListener(this);
		this.add(down, "cell 2 2,alignx left,aligny top");

		// Button start program
		JButton start = new JButton();
		start.setName("start");
		start.setText("start");
		start.addMouseListener(this);
		start.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("RP6 go!");
				Connection.writeData("0");
			}
		});
		this.add(start, "cell 0 0, hmax 25px, alignx left,aligny bottom");

		// Button stop
		JButton stop = new JButton();
		stop.setName("stop");
		stop.setText(" stop");
		stop.setPreferredSize(new Dimension(100, 100));
		stop.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("RP6 stop!");
				Connection.writeData("1");
			}
		});
		this.add(stop, "cell 0 1, hmax 25px,  alignx left,aligny bottom");
		
		// Button automatic mode
		JButton automatic = new JButton();
		automatic.setName("automatic");
		automatic.setText("auto");
		automatic.setPreferredSize(new Dimension(100, 100));
		automatic.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("RP6 automatic mode!");
				Connection.writeData("6");
			}
		});
		this.add(automatic, "cell 0 2, hmax 25px,  alignx left,aligny top");

	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseEntered(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mouseExited(MouseEvent e) {
		// TODO Auto-generated method stub

	}

	@Override
	public void mousePressed(MouseEvent e) {

		if (e.getComponent().getName().endsWith("up")) {
			System.err.println("RP6 drive forward!");
//			connect.writeData("2");
			Connection.getInstance().setCurMove(Move.FWD);
		} else if (e.getComponent().getName().endsWith("left")) {
			System.err.println("RP6 drive left!");
			Connection.getInstance().setCurMove(Move.LEFT);
//			connect.writeData("4");
		} else if (e.getComponent().getName().endsWith("right")) {
			System.err.println("RP6 drive right!");
			Connection.getInstance().setCurMove(Move.RIGHT);
//			connect.writeData("5");
		} else if (e.getComponent().getName().endsWith("down")) {
			System.err.println("RP6 drive backwards!");
//			connect.writeData("3");
			Connection.getInstance().setCurMove(Move.BWD);
		}

	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub

	}
}
