package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.Application.LoginListener;
import ai.vital.endpoint.EndpointType;


public class ServicePanel extends JPanel implements LoginListener {

	private static final long serialVersionUID = 1L;

	private final static Logger log = LoggerFactory.getLogger(ServicePanel.class);
	
	private JLabel login = new JLabel("login:");
	private JLabel passwd = new JLabel("passwd:");
	
	private JTextField loginBox = new JTextField();
	
	private JPasswordField passwdBox = new JPasswordField();
	
	private JLabel loggedInInfo = new JLabel();
	
	private JLabel message = new JLabel("");
	
	private Timer t = new Timer();
	
	
	private JPanel URLPanel;
	
	JPanel connectPanelRow;
	JPanel passwordPanelRow;
	JPanel loginButtonPanelRow;
	
	JPanel noticeLabelPanel;
	
	JPanel disconnectButtonPanelRow;
	
	
	private JButton connectButton = new JButton("Connect");
	private JButton disconnectButton = new JButton("Disconnect");
	
//	private JTextField URLField = new JTextField();
	private JLabel endpointLabel = new JLabel("endpoint:");
	private JLabel endpointTypeL = new JLabel();
	
//	private JPanel notLoggedInPanel = new JPanel();
//	private JPanel loggedInPanel = new JPanel();
	
	
	private JLabel URLLabel = new JLabel("URL:");
	private JTextField URLField = new JTextField();
	
	private EndpointType endpointType; 
	
	public ServicePanel() {
		super();
				
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.setBorder(new TitledBorder("Service"));

		
		
		JPanel endpointTypePanel = new JPanel();
		endpointTypePanel.setLayout(new BorderLayout(5,2));
		endpointTypePanel.setBorder(new EmptyBorder(2,2,2,2));
		
		
		endpointLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		Dimension labelSize = new Dimension(60,25);
		
		endpointLabel.setPreferredSize(labelSize);
		
		endpointTypePanel.add(endpointLabel, BorderLayout.WEST);
		
		endpointType = Application.get().getEndpointType();
		
		log.info("Endpoint type: {}", endpointType);

		
		endpointTypeL.setText("" + endpointType.getName());
		endpointTypePanel.add(endpointTypeL, BorderLayout.CENTER);
		
		add(endpointTypePanel);


		if(endpointType == EndpointType.VITALPRIME) {
			
			
			URLPanel = new JPanel();
			URLPanel.setLayout(new BorderLayout(5,2));
			URLPanel.setBorder(new EmptyBorder(2,2,2,2));
			
			URLLabel.setHorizontalAlignment(SwingConstants.RIGHT);
			URLLabel.setPreferredSize(labelSize);
			
			URLPanel.add(URLLabel, BorderLayout.WEST);
			
			URLField.setEditable(true);
			URLField.setText(Application.get().getPrimeURL());
			
			URLPanel.add(URLField, BorderLayout.CENTER);
			
			add(URLPanel);
			
		}
		

		
		
		connectPanelRow = new JPanel();
		connectPanelRow.setLayout(new BorderLayout(5,2));
		connectPanelRow.setBorder(new EmptyBorder(2,2,2,2));
		

		login.setPreferredSize(labelSize);
		login.setHorizontalAlignment(SwingConstants.RIGHT);
		
		connectPanelRow.add(login, BorderLayout.WEST);
		connectPanelRow.add(loginBox, BorderLayout.CENTER);
		
		
		passwordPanelRow = new JPanel();
		passwordPanelRow.setLayout(new BorderLayout(5,2));
		passwordPanelRow.setBorder(new EmptyBorder(2,2,2,2));
		passwd.setPreferredSize(labelSize);
		passwd.setHorizontalAlignment(SwingConstants.RIGHT);
		
		passwordPanelRow.add(passwd, BorderLayout.WEST);
		passwordPanelRow.add(passwdBox, BorderLayout.CENTER);
		
		loginButtonPanelRow = new JPanel();
//		loginButtonPanelRow.setLayout(new BoxLayout(loginButtonPanelRow, BoxLayout.LINE_AXIS));
		loginButtonPanelRow.setLayout(new BorderLayout(5,2));
		loginButtonPanelRow.setBorder(new EmptyBorder(2,2,2,2));
//		loginButtonPanelRow.setAlignmentX(RIGHT_ALIGNMENT);
		loginButtonPanelRow.add(message, BorderLayout.CENTER);
		loginButtonPanelRow.add(connectButton, BorderLayout.EAST);
		
		connectButton.setPreferredSize(new Dimension(70,25));
		
//		
//		notLoggedInPanel.add(message);
//		notLoggedInPanel.add(loginButton);
//		
//		loggedInPanel.setLayout(new GridLayout(2,1));
//		loggedInPanel.add(loggedInInfo);
//		loggedInPanel.add(logoutButton);
		
		
		connectButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				connectButton.setEnabled(false);
				message.setText("logging in ...");
				final String url = URLField.getText();
				if(endpointType == EndpointType.VITALPRIME) {
					log.info("Connecting to Vital Endpoint at: " + url);
				} else {
					log.info("Connecting to Vital Endpoint type: " + endpointType.getName());
				}
				
				TimerTask task = new TimerTask(){

					@Override
					public void run() {
						
						try {
							Application.get().login(loginBox.getText(), String.valueOf(passwdBox.getPassword()),url);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.error(e.getLocalizedMessage());
							message.setText("");
							JOptionPane.showMessageDialog(null, "Connection error: " + e.getLocalizedMessage(), "Connection error", JOptionPane.ERROR_MESSAGE);
							connectButton.setEnabled(true);
							
						}
						
						
					}};

				t.schedule(task, 10);
				
			}});
		
		disconnectButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Application.get().logout();
			}});
		
		disconnectButtonPanelRow = new JPanel();
		disconnectButtonPanelRow.setLayout(new FlowLayout(FlowLayout.RIGHT));
		disconnectButtonPanelRow.add(loggedInInfo);
		disconnectButtonPanelRow.add(disconnectButton);
		
		/*
		if(Configurator.getNoticeLabelVisible()) {
			
			noticeLabelPanel = new JPanel(new BorderLayout());
			JLabel noticeLabel = new JLabel("Login is not necessary to access Sage Network Data", JLabel.CENTER);
			noticeLabelPanel.add(noticeLabel, BorderLayout.CENTER);
//			noticeLabel.setBorder(new EmptyBorder(3,5,2,2));
		
		}
		
		*/

		onLogout();
		message.setText("");
		
		Application.get().addLoginListener(this);
		
		
		if(endpointType != EndpointType.VITALPRIME) {
			
			//autologin
			connectButton.doClick();
			
		}
		
		
	}

	public void onLogin() {
		
		if(noticeLabelPanel != null) {
			remove(noticeLabelPanel);
		}
		remove(loginButtonPanelRow);
//		remove(passwordPanelRow);
//		remove(connectPanelRow);
		
		add(disconnectButtonPanelRow);
		
		URLField.setEnabled(false);
		
	}

	public void onLogout() {
		
		connectButton.setEnabled(true);
		
		URLField.setEnabled(true);
//		remove(loggedInPanel);
//		add(notLoggedInPanel);
		
		remove(disconnectButtonPanelRow);
//		remove(loggedInInfo);

//		add(connectPanelRow);
//		add(passwordPanelRow);
		add(loginButtonPanelRow);
		if(noticeLabelPanel != null) {
			add(noticeLabelPanel);
		}
		
		message.setText("Logged out");
		
		
		
	}

	public void setConnectionURL(String initialURL) {
		URLField.setText(initialURL);
	}

}
