package swing;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ScrollPaneConstants;

import rxtx.Connection;
import net.miginfocom.swing.MigLayout;
/**
*
* 
* @author Tobias Fleischer
* 
*/
public class MainWindow {

	private JFrame frame;

	/**
	 * Launch the application.
	 */
	Dimension screen ;
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
	 * @throws IOException 
	 */
	public MainWindow() throws IOException {
		initialize();
	}

	/**
	 * Initialize the contents of the frame
	 * @throws IOException 
	 */
	private void initialize() throws IOException {
		Dimension min = new Dimension(500, 970);
		frame = new JFrame();
		frame.getContentPane().setBackground(Color.BLACK);
		frame.setMinimumSize(min);
		frame.getContentPane().setEnabled(false);
		frame.setBounds(100, 100, 450, 300);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setTitle("PVC Projekt: SLAM by Juhulian Rilli, Tobias Fleischer, Jan Hoeppner, Felix Van Gunsteren");
		frame.getContentPane().setLayout(new MigLayout("", "[grow][grow][]", "[grow][grow]"));
		
		/**
		 * MapArea
		 */
		Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
		Dimension sizeConsol = new Dimension(screen.width,screen.height);
		Draw drawMap = new Draw();
		drawMap.setBackground(Color.WHITE);
		//"cell 0 0 2 1,grow"
		JScrollPane scrollPane = new JScrollPane(drawMap);
		scrollPane.setEnabled(false);
		frame.getContentPane().add(scrollPane, " hmin 750px,cell 0 0 3 1,push,grow");
		frame.setVisible(true);
		
		
		/**
		 * ConsolArea
		 */
		JTextArea consolTextArea = new JTextArea();
		PrintStream standardOut = System.out;
		PrintStream errOut = System.err;
		PrintStream printStream = new PrintStream(new CustomOutputStream(consolTextArea));
		standardOut = System.out;
		System.setOut(printStream);
		System.setErr(printStream);
		Runnable runnable = new Connection(printStream);
		
		//consolTextArea.setBackground(Color.BLACK);
		JScrollPane scrollPaneConsol = new JScrollPane (consolTextArea, 
	            ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
	            ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		frame.getContentPane().add(scrollPaneConsol, "hmax 300px ,cell 0 1 2 1, push ,grow");
		Dimension size = new Dimension(10,10);
		
			
			
		
		/**
		 * ControlArea
		 */
		Control control = new Control();
		control.setBackground(Color.BLACK);
		//control.setBackground(UIManager.getColor("Button.background"));
		frame.getContentPane().add(control, "wmax 130px,hmax 200px,  cell 2 1");
		
		
		
		/**
		 * MenuBar
		 */
		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);
		
		JMenu mnNewMenu = new JMenu("Connect");
		menuBar.add(mnNewMenu);
		
		JMenuItem mntmNew = new JMenuItem("New Connection");
		mnNewMenu.add(mntmNew);
		
		JMenuItem mntmClos = new JMenuItem("Close Connection");
		mnNewMenu.add(mntmClos);
	}

}
