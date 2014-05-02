package swing;

import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.miginfocom.swing.MigLayout;
/**
*
* 
* @author Tobias Fleischer
* 
*/
public class Control extends JPanel implements MouseListener{
	Control() throws IOException{
	this.setLayout(new MigLayout("", "[44px][24px][][44px][][24px][][][][grow][]", "[44px][][][grow]"));
	
	JLabel up = new JLabel();
	up.setName("up");
	InputStream is = getClass().getResourceAsStream("images/up.png");
	Image image = ImageIO.read(is);
	up.setIcon(new ImageIcon(image));
	up.addMouseListener(this);
	this.add(up, "cell 1 0,alignx left,aligny top");
	
	JLabel left = new JLabel("");
	left.setName("left");
	is = getClass().getResourceAsStream("images/left.png");
	image = ImageIO.read(is);
	left.setIcon(new ImageIcon(image));
	left.addMouseListener(this);
	this.add(left, "cell 0 1,alignx left,aligny center");
	
	JLabel right = new JLabel("");
	right.setName("right");
	is = getClass().getResourceAsStream("images/right.png");
	image = ImageIO.read(is);
	right.setIcon(new ImageIcon(image));
	right.addMouseListener(this);
	this.add(right, "cell 2 1,alignx left,aligny center");
	
	JLabel down = new JLabel("");
	down.setName("down");
	is = getClass().getResourceAsStream("images/down.png");
	image = ImageIO.read(is);
	down.setIcon(new ImageIcon(image));
	down.addMouseListener(this);
	this.add(down, "cell 1 2,alignx left,aligny top");
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
		if(e.getComponent().getName().endsWith("up")){
			System.err.println("up");
		}else if(e.getComponent().getName().endsWith("left")){
			System.err.println("left");
		}else if(e.getComponent().getName().endsWith("right")){
			System.err.println("right");
		}else if(e.getComponent().getName().endsWith("down")){
			System.err.println("down");
		}
		
	}

	@Override
	public void mouseReleased(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}
}
