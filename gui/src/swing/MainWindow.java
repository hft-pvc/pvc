package swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Graphics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class MainWindow {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainWindow window = new MainWindow();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public MainWindow() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setBounds(0, 0, screenSize.width, screenSize.height);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		JPanel map = new JPanel() {
			@Override
			public void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setColor(Color.BLACK);
				g.drawLine(0, 0, 100, 100);
			}
		};
		map.setForeground(Color.ORANGE);
		map.setBackground(Color.WHITE);
		frame.getContentPane().add(map, BorderLayout.CENTER);
		
		JPanel info_panel = new JPanel();
		info_panel.setSize(100, 150);
		frame.getContentPane().add(info_panel, BorderLayout.SOUTH);
		info_panel.setLayout(new BorderLayout(0, 0));
		
		JPanel console = new JPanel();
		info_panel.add(console, BorderLayout.WEST);
		console.setBackground(Color.BLACK);
		console.setLayout(new BorderLayout(0, 0));
		
		JTextArea textArea = new JTextArea();
		textArea.setBackground(Color.BLACK);
		textArea.setForeground(Color.WHITE);
		textArea.setPreferredSize(new Dimension(screenSize.width / 2, screenSize.height / 4));
		textArea.setEditable(false);
		textArea.setText("adsldjfladf\n pjdlfjsadfl");
		console.add(textArea);
		
		
		
		
		JPanel control = new JPanel();
		info_panel.add(control, BorderLayout.EAST);
		
		JButton left = new JButton("LEFT");
		
		JButton right = new JButton("RIGHT");
		right.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		
		JButton up = new JButton("UP");
		
		JButton down = new JButton("DOWN");

		
	}

}
