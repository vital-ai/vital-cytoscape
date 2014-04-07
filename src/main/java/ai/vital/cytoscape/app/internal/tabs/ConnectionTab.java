package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JPanel;

import ai.vital.cytoscape.app.internal.app.Application.LoginListener;
import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;


public class ConnectionTab extends JPanel implements LoginListener {

	private static final long serialVersionUID = 1L;

	private LoginPanel loginPanel = new LoginPanel();
	
	private JPanel centerPanel = new JPanel();
	
	public ConnectionTab() {
		super();
		setLayout(new BorderLayout());
		
		add(loginPanel,BorderLayout.NORTH);
		
		centerPanel.setLayout(new GridLayout(3,1,2,2));
		
		add(centerPanel, BorderLayout.CENTER);
		
	}

	public void onLogin() {
		
		System.out.println("On login received!");
	
		updateUI();
		
	}

	public void onLogout() {
	
		System.out.println("On logout received!");
		
		NetworkListPanel.clearListeners();
		
		updateUI();
		
	}
	
	
	public void setURL(String initialURL) {
		loginPanel.setConnectionURL(initialURL);
	}

}
