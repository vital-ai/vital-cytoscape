package ai.vital.cytoscape.app.internal.app;

import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;

public class NetworkListener implements NetworkAddedListener, NetworkDestroyedListener {

	private final static Logger log = LoggerFactory.getLogger(NetworkListener.class);
	
	@Override
	public void handleEvent(NetworkAddedEvent event) {

		log.info("Network added: " + event.getNetwork().getSUID());
		
		NetworkListPanel.notifyNetworkListPanels(null);
		
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent event) {
		
		log.info("Network destroyed...");
		
		NetworkListPanel.notifyNetworkListPanels(null);
		
	}

}
