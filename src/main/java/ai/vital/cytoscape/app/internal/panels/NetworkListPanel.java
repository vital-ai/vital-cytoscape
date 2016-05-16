package ai.vital.cytoscape.app.internal.panels;

import java.awt.BorderLayout;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Set;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;

public class NetworkListPanel extends JPanel implements ItemListener {

	private final static Logger log = LoggerFactory.getLogger(NetworkListPanel.class);
	
	private static final long serialVersionUID = 1L;
	
	private static int counter = 0;

	private static LinkedList<NetworkListPanel> createdPanels = new LinkedList<NetworkListPanel>();
	
	public static void notifyNetworkListPanels(String latestRemovedNetwordID) {

		Set<CyNetwork> networkSet = getNetworksSet();

		Iterator<NetworkListPanel> iterator = createdPanels.iterator();
		
		while(iterator.hasNext()) {
			iterator.next().updateNetworksList(networkSet,latestRemovedNetwordID);
		}
		
	}
	
    private static Set<CyNetwork> getNetworksSet() {

    	Set<CyNetwork> networkSet = null;
    	
		CyNetworkManager networkManager = VitalAICytoscapePlugin.getNetworkManager();
		if(networkManager != null) {
			networkSet = networkManager.getNetworkSet();
		} else {
			networkSet = new HashSet<CyNetwork>();
		}

		return networkSet;
	}

	private LinkedHashMap<String, Long> netTitle2ID = new LinkedHashMap<String, Long>();
	
    private DefaultComboBoxModel networksComboModel = new DefaultComboBoxModel();
    private JComboBox networksComboBox = new JComboBox(networksComboModel);
    
	public NetworkListPanel(boolean networksListListener) {
		super();
		networksComboBox.addItemListener(this);
		
		networksComboModel.addElement("<new network>");
		
        updateNetworksList(getNetworksSet(), "");
        
        setLayout(new BorderLayout());
        
        add(networksComboBox,BorderLayout.CENTER);
        
        if(networksListListener) {
        	createdPanels.add(this);
        }
	}
	
	public void itemStateChanged(ItemEvent e) {
		int selectedIndex = networksComboBox.getSelectedIndex();
		if(selectedIndex == 1) {
			networksComboBox.setSelectedIndex(0);
		}
	}
	
	public void updateNetworksList(Set<CyNetwork> networks, String latestRemovedNetwordID) {
		
		netTitle2ID.clear();
		
		log.debug("Updating networks list");
		
		int size = networksComboModel.getSize();
		
		while (size > 1) {
			networksComboModel.removeElementAt(1);
			size--;
		}
		
		CyNetwork[] networksArray = networks.toArray(new CyNetwork[0]);
		
		if(networksArray.length > 0 ) {
	        networksComboModel.addElement("-------------");
		}
		
		for(int i = 0 ; i < networksArray.length ; i ++) {
			
			CyNetwork network = (CyNetwork) networksArray[i];

			String title = network.getRow(network).get(CyNetwork.NAME, String.class);
			
			Long identifier = network.getSUID();
			log.debug("Network ID: " + identifier);
			
			if(latestRemovedNetwordID != null && identifier.equals(latestRemovedNetwordID)) {
				log.debug("Network withID: "+identifier +" was removed");	
			} else {
				networksComboModel.addElement(title);
				netTitle2ID.put(title, identifier);
			}
		}
	}
	
	private String getSelectedNetworkTitle() {
		if(networksComboBox.getSelectedIndex() == 0 ) {
			return null;
		}
		return (String) networksComboBox.getSelectedItem();
	}
	
	private Long getSelectedNetworkID() {
		String networkTitle = (String) networksComboBox.getSelectedItem();
		return netTitle2ID.get(networkTitle);
	}
    

	public CyNetwork getSelectedNetwork() {
	
		Long networkID = null;
		
		String selectedNetworkTitle = getSelectedNetworkTitle();
		
		if(selectedNetworkTitle==null) {
			
			CyNetwork createNetwork = VitalAICytoscapePlugin.getNetworkFactory().createNetwork();
			
			createNetwork.getRow(createNetwork).set(CyNetwork.NAME, "NewNetwork_"+counter++);
			
			networkID = createNetwork.getSUID();
			
			
			VitalAICytoscapePlugin.getNetworkManager().addNetwork(createNetwork);
			
			VitalAICytoscapePlugin.getApplicationManager().setCurrentNetwork(createNetwork);
			
			
			CyNetworkView createNetworkView = VitalAICytoscapePlugin.getNetworkViewFactory().createNetworkView(createNetwork);
			
			VitalAICytoscapePlugin.getNetworkViewManager().addNetworkView(createNetworkView);
			
			VitalAICytoscapePlugin.getApplicationManager().setCurrentNetworkView(createNetworkView);
			
			
//			CyNetworkView networkView = Cytoscape.getNetworkView(networkID);
//			
//			if(networkView != null) {
//				
//				Component component2 = networkView.getComponent();
//				
//				if(component2 instanceof InnerCanvas) {
//					
//					InnerCanvas ic = (InnerCanvas) component2;
//					
//					Container parent2 = ic.getParent();
//					
//					JLayeredPane layeredPane = (JLayeredPane) parent2;
//					
//					Container parent3 = layeredPane.getParent();
//					
//				
//					JInternalFrame jif = (JInternalFrame) layeredPane.getParent();
//					
//					log.debug();
//					
//				}
//				
//			}
			
		} else {
			
			networkID = getSelectedNetworkID();
	
			log.debug("Existing network title:" + selectedNetworkTitle + "\tid:"  + networkID);
			
		}
		
		CyNetwork cyNetwork = VitalAICytoscapePlugin.getNetworkManager().getNetwork(networkID);
		return cyNetwork;
		
	}

	public static void clearListeners() {
		createdPanels.clear();
	}
}
