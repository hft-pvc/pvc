package swing;

import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
/**
*
* 
* @author Tobias Fleischer
* 
*/
public class CustomOutputStream extends OutputStream {
	private JTextArea textArea;
	
	public CustomOutputStream(JTextArea textArea) {
		this.textArea = textArea;
	}
	
	@Override
	public void write(int b) throws IOException {
		//textArea.setBackground(Color.BLACK);
		//textArea.setForeground(Color.WHITE);
		textArea.setEditable(false);
		// redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
	}
}