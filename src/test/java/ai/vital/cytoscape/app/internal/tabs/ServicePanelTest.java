package ai.vital.cytoscape.app.internal.tabs;

import javax.swing.JFrame;

import junit.framework.TestCase;

public class ServicePanelTest extends TestCase {

	public void test() {
		
	}
	
	public static void main(String[] args) throws InterruptedException {
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		ServicePanel panel = new ServicePanel();
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		
		Thread.sleep(3000);
		
	}
	
}
