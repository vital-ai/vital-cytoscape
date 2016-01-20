package ai.vital.cytoscape.app.internal.app;

import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedEvent;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;

public class NetworkListener implements NetworkAddedListener, NetworkDestroyedListener, NetworkViewAddedListener, NetworkViewDestroyedListener {

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

	@Override
	public void handleEvent(NetworkViewDestroyedEvent event) {
		log.info("Network view destroyed...");
	}

	@Override
	public void handleEvent(NetworkViewAddedEvent event) {
		
		CyNetworkView networkView = event.getNetworkView();
		
		log.info("Network view added: " + networkView.getSUID());
		
		VisualStyleUtils.applyVisualStyle(networkView);
		
	}

}
