package ai.vital.cytoscape.app.internal.tabs;

import java.awt.Component;
import java.awt.Dimension;

import javax.swing.Icon;
import javax.swing.JTabbedPane;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class MainTabsPanel extends JTabbedPane implements CytoPanelComponent {

	private static final long serialVersionUID = 2673342686154396174L;

	public MainTabsPanel() {
	
		setMinimumSize(new Dimension(200, -1));
		
	}
	
	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.WEST;
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public String getTitle() {
		return "Vital AI";
	}

}
