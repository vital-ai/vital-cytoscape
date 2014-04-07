package ai.vital.cytoscape.app.internal.app;

import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedEvent;
import org.cytoscape.model.events.NetworkDestroyedListener;

import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;

public class NetworkListener implements NetworkAddedListener, NetworkDestroyedListener {

	@Override
	public void handleEvent(NetworkAddedEvent event) {

		System.out.println("Network added: " + event.getNetwork().getSUID());
		
		NetworkListPanel.notifyNetworkListPanels(null);
		
	}

	@Override
	public void handleEvent(NetworkDestroyedEvent event) {
		
		System.out.println("Network destroyed...");
		
		NetworkListPanel.notifyNetworkListPanels(null);
		
	}

}
