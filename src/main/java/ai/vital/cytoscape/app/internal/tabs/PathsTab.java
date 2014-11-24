package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JPanel;

import ai.vital.cytoscape.app.internal.panels.SegmentsPanel;

public class PathsTab extends JPanel {

	private static final long serialVersionUID = 7556218037060359816L;

	private SegmentsPanel segmentsPanel;
	
	public PathsTab() {
		setLayout(new BorderLayout());
		
		segmentsPanel = new SegmentsPanel();
		segmentsPanel.setPreferredSize(new Dimension(0, 100));
		
		add(segmentsPanel, BorderLayout.NORTH);
		
	}

	public SegmentsPanel getSegmentsPanel() {
		return segmentsPanel;
	}
	
}
