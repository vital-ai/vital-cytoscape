package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.Application.HierarchyNode;
import ai.vital.cytoscape.app.internal.panels.JCheckBoxTree;
import ai.vital.cytoscape.app.internal.panels.SegmentsPanel;
import ai.vital.domain.ontology.VitalOntology;
import ai.vital.vitalservice.EndpointType;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;

public class PathsTab extends JPanel {

	private static final long serialVersionUID = 7556218037060359816L;

	private SegmentsPanel segmentsPanel;
	
	private JComboBox<ExpansionDirection> directionBox;
	
	private JComboBox<Integer> depthBox;
	
	public PathsTab() {
		
		setLayout(new BorderLayout());
		
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		
		segmentsPanel = new SegmentsPanel();
		segmentsPanel.setPreferredSize(new Dimension(0, 100));
		
		northPanel.add(segmentsPanel);
		
		northPanel.add(Box.createRigidArea(new Dimension(1, 10)));
		
		directionBox = new JComboBox<PathsTab.ExpansionDirection>();
		directionBox.addItem(ExpansionDirection.Both);
		directionBox.addItem(ExpansionDirection.Outgoing);
		directionBox.addItem(ExpansionDirection.Incoming);
		
		JPanel directionPanel = new JPanel();
		directionPanel.setLayout(new BoxLayout(directionPanel, BoxLayout.X_AXIS));
		directionPanel.add(Box.createRigidArea(new Dimension(10, 1)));
		directionPanel.add(new JLabel("Direction:"));
		directionPanel.add(Box.createRigidArea(new Dimension(10, 1)));
		directionBox.setPreferredSize(new Dimension(120, 30));
		directionPanel.add(directionBox);
		northPanel.add(directionPanel);
		
		northPanel.add(Box.createRigidArea(new Dimension(1, 10)));

		
		
		depthBox = new JComboBox<Integer>();
		depthBox.addItem(1);
		depthBox.addItem(2);
		
		JPanel depthPanel = new JPanel();
		depthPanel.setLayout(new BoxLayout(depthPanel, BoxLayout.X_AXIS));
		depthPanel.add(Box.createRigidArea(new Dimension(10, 1)));
		depthPanel.add(new JLabel("Expansion Depth:"));
		depthPanel.add(Box.createRigidArea(new Dimension(10, 1)));
		depthBox.setPreferredSize(new Dimension(90, 30));
		depthPanel.add(depthBox);
		northPanel.add(depthPanel);
		
		northPanel.add(Box.createRigidArea(new Dimension(1, 10)));
		
		
		//filters available only in prime endpoint
		if( EndpointType.VITALPRIME == Application.get().getEndpointType()) {
			
			HierarchyNode nodesHierarchy = null;
			try {
				nodesHierarchy = Application.get().getClassHierarchy(VITAL_Node.class);
			} catch (Exception e) {
				nodesHierarchy = new HierarchyNode();
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Nodes hierarchy error", JOptionPane.ERROR_MESSAGE);
			}
			
			HierarchyNode edgesHierarchy = null;
			try {
				edgesHierarchy = Application.get().getClassHierarchy(VITAL_Edge.class);
			} catch (Exception e) {
				edgesHierarchy = new HierarchyNode();
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Edges hierarchy error", JOptionPane.ERROR_MESSAGE);
			}		
			
			
			
			JCheckBoxTree nodesTree = new JCheckBoxTree();
			nodesTree.setMinimumSize(new Dimension(200, 0));
			nodesTree.setRootVisible(true);
			DefaultMutableTreeNode nodesRoot = new DefaultMutableTreeNode(nodesHierarchy, true);
			//expand
			initTree(nodesRoot);
			DefaultTreeModel nodesModel = new DefaultTreeModel(nodesRoot, false);
			nodesTree.setModel(nodesModel);
			
			nodesModel.nodeStructureChanged(nodesRoot);
			
			
//		JPanel nodesTreeWrapper = new JPanel(new BorderLayout());
			JScrollPane sp = new JScrollPane(nodesTree);
			sp.setPreferredSize(new Dimension(0, 200));
//		nodesTreeWrapper.add(sp);
//		northPanel.add(nodesTreeWrapper);
			northPanel.add(sp);
			
			
			
			JCheckBoxTree edgesTree = new JCheckBoxTree();
//		nodesTree.setMinimumSize(new Dimension(200, 0));
			edgesTree.setRootVisible(true);
			DefaultMutableTreeNode edgesRoot = new DefaultMutableTreeNode(edgesHierarchy, true);
			initTree(edgesRoot);
			DefaultTreeModel edgesModel = new DefaultTreeModel(edgesRoot, false);
			edgesTree.setModel(edgesModel);
			
			edgesModel.nodeStructureChanged(edgesRoot);
			
			JScrollPane sp2 = new JScrollPane(edgesTree);
			sp2.setPreferredSize(new Dimension(0, 200));
//		nodesTreeWrapper.add(sp);
//		northPanel.add(nodesTreeWrapper);
			northPanel.add(sp2);
			
			//for(OntClass .listClasses()
			
			//
			
		} else {
			
			northPanel.add(new JLabel("   nodes and edges filter available only with vital prime endpoint"));
			
		}
		
		add(northPanel, BorderLayout.NORTH);
		
		
	}

	private void initTree(DefaultMutableTreeNode nodesRoot) {

		HierarchyNode w = (HierarchyNode) nodesRoot.getUserObject();

		for(HierarchyNode c : w.children) {
			
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(c, true);
			
			nodesRoot.add(n);
			
			initTree(n);
			
		}
		
	}

	public SegmentsPanel getSegmentsPanel() {
		return segmentsPanel;
	}
	
	/*
	public static void main(String[] args) throws VitalServiceException, VitalServiceUnimplementedException {
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		VitalServiceFactory.setServiceProfile("vitaldevelopmentprime");
		
		Application.initForTests(VitalServiceFactory.getVitalService());

		PathsTab panel = new PathsTab();

		panel.getSegmentsPanel().setSegmentsList(Application.get().getServiceSegments());
		
		panel.setSize(400, 400);

		frame.setMinimumSize(new Dimension(800, 600));
		frame.setSize(800, 600);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		

//		ResultList rl = Application.get().getConnections("xxx", VitalOntology.NS + "Entity");
//		System.out.println(rl.toString());
		
	}
	*/

	public static enum ExpansionDirection {
		Both,
		Outgoing,
		Incoming
	}

	public ExpansionDirection getExpansionDirection() {
		return (ExpansionDirection) directionBox.getSelectedItem();
	}
	
	public Integer getDepth() {
		return (Integer) depthBox.getSelectedItem();
	}
}
