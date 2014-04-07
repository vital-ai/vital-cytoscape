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
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.border.TitledBorder;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.Application.LoginListener;



public class LoginPanel extends JPanel  implements LoginListener {

	private static final long serialVersionUID = 1L;
	
	private JLabel login = new JLabel("login:");
	private JLabel passwd = new JLabel("passwd:");
	
	private JTextField loginBox = new JTextField();
	
	private JPasswordField passwdBox = new JPasswordField();
	
	private JLabel loggedInInfo = new JLabel();
	
	private JLabel message = new JLabel("");
	
	private Timer t = new Timer();
	
	
	JPanel loginPanelRow;
	JPanel passwordPanelRow;
	JPanel loginButtonPanelRow;
	
	JPanel noticeLabelPanel;
	
	JPanel logoutButtonPanelRow;
	
	
	private JButton loginButton = new JButton("login");
	private JButton logoutButton = new JButton("logout");
	
	private JTextField URLField = new JTextField();
	
//	private JPanel notLoggedInPanel = new JPanel();
//	private JPanel loggedInPanel = new JPanel();
	
	public LoginPanel() {
		super();
				
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));

		JPanel URLPanel = new JPanel();
		URLPanel.setLayout(new BorderLayout(5,2));
		URLPanel.setBorder(new EmptyBorder(2,2,2,2));
		
		JLabel comp = new JLabel("URL:");
		comp.setHorizontalAlignment(SwingConstants.RIGHT);
		
		Dimension labelSize = new Dimension(40,25);
		
		comp.setPreferredSize(labelSize);
		
		URLPanel.add(comp, BorderLayout.WEST);
		
		URLField.setEditable(true);
		URLField.setText(Application.get().getInitialURL());
		
		URLPanel.add(URLField, BorderLayout.CENTER);
		
		add(URLPanel);
		
		this.setBorder(new TitledBorder("Login panel"));
		
//		notLoggedInPanel.setLayout(new GridLayout(3,2));
//		
//		notLoggedInPanel.add(login);
//		notLoggedInPanel.add(loginBox);
//		notLoggedInPanel.add(passwd);
//		notLoggedInPanel.add(passwdBox);
//		notLoggedInPanel.add(message);
//		notLoggedInPanel.add(loginButton);
//		
//		loggedInPanel.setLayout(new GridLayout(2,1));
//		loggedInPanel.add(loggedInInfo);
//		loggedInPanel.add(logoutButton);
		
//		notLoggedInPanel.setLayout(new GridLayout(3,2));
		
		
		loginPanelRow = new JPanel();
//		loginPanelRow.setLayout(new BoxLayout(loginPanelRow, BoxLayout.X_AXIS));
		loginPanelRow.setLayout(new BorderLayout(5,2));
		loginPanelRow.setBorder(new EmptyBorder(2,2,2,2));
		

		login.setPreferredSize(labelSize);
		login.setHorizontalAlignment(SwingConstants.RIGHT);
		
		loginPanelRow.add(login, BorderLayout.WEST);
		loginPanelRow.add(loginBox, BorderLayout.CENTER);
		
		
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
		loginButtonPanelRow.add(loginButton, BorderLayout.EAST);
		
		loginButton.setPreferredSize(new Dimension(70,25));
		
//		
//		notLoggedInPanel.add(message);
//		notLoggedInPanel.add(loginButton);
//		
//		loggedInPanel.setLayout(new GridLayout(2,1));
//		loggedInPanel.add(loggedInInfo);
//		loggedInPanel.add(logoutButton);
		
		
		loginButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				
				loginButton.setEnabled(false);
				message.setText("logging in ...");
				final String url = URLField.getText();
				System.out.println("Connecting to Vital Endpoint at: " + url);
				
				TimerTask task = new TimerTask(){

					@Override
					public void run() {
						
						try {
							Application.get().login(loginBox.getText(), String.valueOf(passwdBox.getPassword()),url);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							message.setText(e.getLocalizedMessage());
						}
						
						loginButton.setEnabled(true);
					}};

				t.schedule(task, 10);
				
			}});
		
		logoutButton.addActionListener(new ActionListener(){

			public void actionPerformed(ActionEvent e) {
				Application.get().logout();
			}});
		
		logoutButtonPanelRow = new JPanel();
		logoutButtonPanelRow.setLayout(new FlowLayout(FlowLayout.RIGHT));
		logoutButtonPanelRow.add(loggedInInfo);
		logoutButtonPanelRow.add(logoutButton);
		
		/*
		if(Configurator.getNoticeLabelVisible()) {
			
			noticeLabelPanel = new JPanel(new BorderLayout());
			JLabel noticeLabel = new JLabel("Login is not necessary to access Sage Network Data", JLabel.CENTER);
			noticeLabelPanel.add(noticeLabel, BorderLayout.CENTER);
//			noticeLabel.setBorder(new EmptyBorder(3,5,2,2));
		
		}
		
		*/
		
		add(loginPanelRow);
		add(passwordPanelRow);
		add(loginButtonPanelRow);
		
		if(noticeLabelPanel != null) {
			add(noticeLabelPanel);
		}
		
		Application.get().addLoginListener(this);
		
	}

	public void onLogin() {
//		remove(notLoggedInPanel);
//		add(loggedInPanel);
		
		if(noticeLabelPanel != null) {
			remove(noticeLabelPanel);
		}
		remove(loginButtonPanelRow);
		remove(passwordPanelRow);
		remove(loginPanelRow);
		
//		add(loggedInInfo);
		add(logoutButtonPanelRow);
		//loggedInInfo.setText("<html>You're logged in as <b>" + Application.get().getLogin() + "</b></html>");
		URLField.setEnabled(false);
	}

	public void onLogout() {
		URLField.setEnabled(true);
//		remove(loggedInPanel);
//		add(notLoggedInPanel);
		
		remove(logoutButtonPanelRow);
//		remove(loggedInInfo);

		add(loginPanelRow);
		add(passwordPanelRow);
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
