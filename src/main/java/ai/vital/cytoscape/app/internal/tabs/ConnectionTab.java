package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.GridLayout;

import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.Application.LoginListener;
import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;


public class ConnectionTab extends JPanel implements LoginListener {

	private static final long serialVersionUID = 1L;

//	private LoginPanel loginPanel = new LoginPanel();
	private ServicePanel servicePanel = new ServicePanel();
	
	private JPanel centerPanel = new JPanel();
	
	private final static Logger log = LoggerFactory.getLogger(ConnectionTab.class);
	
	public ConnectionTab() {
		super();
		setLayout(new BorderLayout());
		
		add(servicePanel,BorderLayout.NORTH);
		
		centerPanel.setLayout(new GridLayout(3,1,2,2));
		
		add(centerPanel, BorderLayout.CENTER);
		
	}

	public void onLogin() {
		
		log.debug("On login received!");
	
		updateUI();
		
	}

	public void onLogout() {
	
		log.debug("On logout received!");
		
		NetworkListPanel.clearListeners();
		
		updateUI();
		
	}
	
	
	public void setURL(String initialURL) {
		servicePanel.setConnectionURL(initialURL);
	}

}
