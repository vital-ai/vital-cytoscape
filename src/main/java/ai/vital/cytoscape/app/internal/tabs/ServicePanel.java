package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
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

import com.typesafe.config.Config;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.Application.LoginListener;
import ai.vital.vitalservice.EndpointType;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.auth.VitalAuthKeyValidation;
import ai.vital.vitalservice.auth.VitalAuthKeyValidation.VitalAuthKeyValidationException;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalsigns.model.VitalApp;
import ai.vital.vitalsigns.model.VitalServiceKey;
import ai.vital.vitalsigns.model.properties.Property_hasKey;


public class ServicePanel extends JPanel implements LoginListener, ActionListener {

	private static final long serialVersionUID = 1L;

	private final static Logger log = LoggerFactory.getLogger(ServicePanel.class);
	
	private JLabel login = new JLabel("login:");
	private JLabel passwd = new JLabel("passwd:");
	
	private JComboBox<String> serviceProfiles = new JComboBox<String>();
	
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
	private JLabel endpointLabel = new JLabel("type:");
	private JLabel endpointTypeL = new JLabel();
	
	private JLabel endpointURLLabel = new JLabel(" ");
	private JLabel endpointURLValue = new JLabel(" ");
	
//	private JPanel notLoggedInPanel = new JPanel();
//	private JPanel loggedInPanel = new JPanel();
	
	
	private JLabel URLLabel = new JLabel("URL:");
	private JTextField URLField = new JTextField();
	
	private JLabel keyLabel = new JLabel("Service Key:");
	private JTextField keyField = new JTextField();
	
	private EndpointType endpointType;

	private Config servicesConfig;

	private String selectedProfile; 
	
	private JPanel keyPanel = new JPanel();
	
	public ServicePanel() {
		super();
				
		this.setLayout(new BoxLayout(this,BoxLayout.Y_AXIS));
		this.setBorder(new TitledBorder("Service"));

		
		List<String> profiles = VitalServiceFactory.getAvailableProfiles();
		Collections.sort(profiles);
		
		for(String profile : profiles ) {
			serviceProfiles.addItem(profile);
		}
		
		int ind = profiles.indexOf("default");
		if(ind >= 0) {
			serviceProfiles.setSelectedIndex(ind);
		}
		
		
		
		servicesConfig = VitalServiceFactory.getConfig();
		
		
		JPanel serviceProfilePanel = new JPanel();
		serviceProfilePanel.setLayout(new BorderLayout(5,2));
		serviceProfilePanel.setBorder(new EmptyBorder(2,2,2,2));
		
		serviceProfilePanel.add(new JLabel("profile:"), BorderLayout.WEST);
		
		serviceProfilePanel.add(serviceProfiles);
		
		serviceProfiles.addActionListener(this);
		
		add(serviceProfilePanel);
		
		JPanel endpointTypePanel = new JPanel();
		endpointTypePanel.setLayout(new BorderLayout(5,2));
		endpointTypePanel.setBorder(new EmptyBorder(2,2,2,2));
		
		
		endpointLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		
		Dimension labelSize = new Dimension(60,25);
		
//		endpointLabel.setPreferredSize(labelSize);
		
		endpointTypePanel.add(endpointLabel, BorderLayout.WEST);
		
//		endpointType = Application.get().getEndpointType();
		
		log.info("Endpoint type: {}", endpointType);

		
		endpointTypeL.setText(endpointType != null ? ( "" + endpointType.getName() ) : "");
		endpointTypePanel.add(endpointTypeL, BorderLayout.CENTER);
		
		add(endpointTypePanel);
		
		
		
		JPanel endpointURLPanel = new JPanel();
		endpointURLPanel.setLayout(new BorderLayout(5,2));
		endpointURLPanel.setBorder(new EmptyBorder(2,2,2,2));
		
		endpointURLPanel.add(endpointURLLabel, BorderLayout.WEST);
		endpointURLPanel.add(endpointURLValue, BorderLayout.CENTER);
		
		add(endpointURLPanel);

		keyPanel.setLayout(new BorderLayout(5,2));
		keyPanel.setBorder(new EmptyBorder(2,2,2,2));
		
		keyPanel.add(keyLabel, BorderLayout.WEST);
		keyPanel.add(keyField, BorderLayout.CENTER);
		
		add(keyPanel);
		
		

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
		
		JPanel connectButtonWrapper = new JPanel();
		connectButtonWrapper.setLayout(new BoxLayout(connectButtonWrapper, BoxLayout.X_AXIS));
		connectButtonWrapper.add(connectButton);
		connectButtonWrapper.add(Box.createRigidArea(new Dimension(15, 1)));
		
		loginButtonPanelRow.add(connectButtonWrapper, BorderLayout.EAST);
		
		//connectButton.setPreferredSize(new Dimension(70,25));
		
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
				serviceProfiles.setEnabled(false);
				keyField.setEnabled(false);
				message.setText("logging in ...");
				final String url = URLField.getText();
				String key = null;
				if(endpointType == EndpointType.VITALPRIME) {
					key = keyField.getText();
					log.info("Connecting to Vital Endpoint at: " + url + " key: " + key);
					try {
						VitalAuthKeyValidation.validateKey(key);
					} catch (VitalAuthKeyValidationException e1) {
						message.setText("");
						connectButton.setEnabled(true);
						serviceProfiles.setEnabled(true);
						keyField.setEnabled(true);
						JOptionPane.showMessageDialog(null, "Key validation error: " + e1.getLocalizedMessage(), "Input key validation error", JOptionPane.ERROR_MESSAGE);
						return;
					}
				} else {
					key = "aaaa-aaaa-aaaa";
					log.info("Connecting to Vital Endpoint type: " + endpointType.getName() + ", mock key: " + key);
				}
				
				final String finalKey = key;
				
				TimerTask task = new TimerTask(){

					@Override
					public void run() {
						
						try {
							
							if(selectedProfile == null) throw new RuntimeException("No profile selected!");
							
							for(VitalService service : VitalServiceFactory.listOpenServices()) {
								service.close();
							}
							
							//TODO
							VitalServiceKey keyObject = new VitalServiceKey();
							keyObject.generateURI((VitalApp)null);
							keyObject.set(Property_hasKey.class, finalKey);
							VitalService service = VitalServiceFactory.openService(keyObject, selectedProfile);
							
							Application.get().login(service);
							
						} catch (Exception e) {
							// TODO Auto-generated catch block
							log.error(e.getLocalizedMessage(), e);
							message.setText("");
							JOptionPane.showMessageDialog(null, "Connection error: " + e.getLocalizedMessage(), "Connection error", JOptionPane.ERROR_MESSAGE);
							connectButton.setEnabled(true);
							serviceProfiles.setEnabled(true);
							keyField.setEnabled(true);
							
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
		
		try {
		Application.get().addLoginListener(this);
		} catch(Exception e){
			e.printStackTrace();
		}
		
		
//		if(endpointType != EndpointType.VITALPRIME) {
//			
//			//autologin
//			connectButton.doClick();
//			
//		}
		
		this.actionPerformed(null);
		
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
		
		serviceProfiles.setEnabled(false);
		
		keyField.setEnabled(false);
		
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
		
		URLField.setEnabled(true);
		
		serviceProfiles.setEnabled(true);
		
		keyField.setEnabled(true);
		
		
		
	}

	public void setConnectionURL(String initialURL) {
		URLField.setText(initialURL);
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {

		try {
			
			this.selectedProfile = (String) serviceProfiles.getSelectedItem();
			
			Config profileConfig = servicesConfig.getConfig("profile." + selectedProfile);
			
			this.endpointType = EndpointType.fromString(profileConfig.getString("type"));
			
			
			endpointTypeL.setText( "" + this.endpointType.getName()) ;
			
			if(endpointType == EndpointType.VITALPRIME) {
				
				endpointURLLabel.setText("URL:");
				endpointURLValue.setText(profileConfig.getString("VitalPrime.endpointURL"));
				
				keyPanel.setVisible(true);
				keyField.setText("");
				
			} else {
				
				endpointURLLabel.setText(" ");
				endpointURLValue.setText(" ");
				
				keyPanel.setVisible(false);
				keyField.setText("");
			}
			
		} catch(Exception e) {
			
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "ERROR", JOptionPane.ERROR_MESSAGE);
			
		}
		
		
		
		
	}

}
