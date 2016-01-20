package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.Application.HierarchyNode;
import ai.vital.cytoscape.app.internal.panels.JCheckBoxTree;
import ai.vital.cytoscape.app.internal.panels.SegmentsPanel;
import ai.vital.vitalservice.EndpointType;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;

public class PathsTab extends JPanel {

	private static final long serialVersionUID = 7556218037060359816L;

	private SegmentsPanel segmentsPanel;
	
	private JComboBox<ExpansionDirection> directionBox;
	
	private JComboBox<Integer> depthBox;
	
	private JCheckBoxTree nodesTree = new JCheckBoxTree();
	private JCheckBoxTree edgesTree = new JCheckBoxTree();
	
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
//		if( true ) {
			
			boolean local = EndpointType.VITALPRIME != Application.get().getEndpointType();
			
			HierarchyNode nodesHierarchy = null;
			
			try {
				nodesHierarchy = Application.get().getClassHierarchy(VITAL_Node.class, local);
			} catch (Exception e) {
				nodesHierarchy = new HierarchyNode();
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Nodes hierarchy error", JOptionPane.ERROR_MESSAGE);
			}
			
			HierarchyNode edgesHierarchy = null;
			try {
				edgesHierarchy = Application.get().getClassHierarchy(VITAL_Edge.class, local);
			} catch (Exception e) {
				edgesHierarchy = new HierarchyNode();
				JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Edges hierarchy error", JOptionPane.ERROR_MESSAGE);
			}
				
			nodesTree.setMinimumSize(new Dimension(200, 0));
			nodesTree.setRootVisible(true);
			DefaultMutableTreeNode nodesRoot = new DefaultMutableTreeNode(nodesHierarchy, true);
			//expand
			initTree(nodesRoot);
			DefaultTreeModel nodesModel = new DefaultTreeModel(nodesRoot, false);
			nodesTree.setModel(nodesModel);
			
			nodesModel.nodeStructureChanged(nodesRoot);
			nodesTree.selectRootNode();
			
//		JPanel nodesTreeWrapper = new JPanel(new BorderLayout());
			JScrollPane sp = new JScrollPane(nodesTree);
			sp.setPreferredSize(new Dimension(0, 200));
//		nodesTreeWrapper.add(sp);
//		northPanel.add(nodesTreeWrapper);
			northPanel.add(sp);
			
			
			
//		nodesTree.setMinimumSize(new Dimension(200, 0));
			edgesTree.setRootVisible(true);
			DefaultMutableTreeNode edgesRoot = new DefaultMutableTreeNode(edgesHierarchy, true);
			initTree(edgesRoot);
			DefaultTreeModel edgesModel = new DefaultTreeModel(edgesRoot, false);
			edgesTree.setModel(edgesModel);
			
			edgesModel.nodeStructureChanged(edgesRoot);
			edgesTree.selectRootNode();
			
			JScrollPane sp2 = new JScrollPane(edgesTree);
			sp2.setPreferredSize(new Dimension(0, 200));
//		nodesTreeWrapper.add(sp);
//		northPanel.add(nodesTreeWrapper);
			northPanel.add(sp2);
			
			//for(OntClass .listClasses()
			
			//
			
//		} else {
//			
//			northPanel.add(new JLabel("   nodes and edges filter available only with vital prime endpoint"));
//			
//		}
		
		add(northPanel, BorderLayout.NORTH);
		
		
	}

	private void initTree(DefaultMutableTreeNode nodesRoot) {

		HierarchyNode w = (HierarchyNode) nodesRoot.getUserObject();

		Collections.sort(w.children, new Comparator<HierarchyNode>() {
			@Override
			public int compare(HierarchyNode o1, HierarchyNode o2) {
				return o1.cls.getSimpleName().compareTo(o2.cls.getSimpleName());
			}
		});
		
		for(HierarchyNode c : w.children) {
			
			DefaultMutableTreeNode n = new DefaultMutableTreeNode(c, true);
			
			nodesRoot.add(n);
			
			initTree(n);
			
		}
		
	}

	public SegmentsPanel getSegmentsPanel() {
		return segmentsPanel;
	}
	
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

	@SuppressWarnings("unchecked")
	public List<Class<? extends VITAL_Edge>> getSelectedEdgeTypes() {
		List<Class<? extends VITAL_Edge>> l = new ArrayList<Class<? extends VITAL_Edge>>();
		for( TreePath path : edgesTree.getCheckedPaths() ) {
			Object[] path2 = path.getPath();
			for(Object obj : path2) {
				DefaultMutableTreeNode tn = (DefaultMutableTreeNode) obj;
				HierarchyNode hn = (HierarchyNode) tn.getUserObject();
				if(l.contains(hn.cls)) continue;
				l.add((Class<? extends VITAL_Edge>) hn.cls);
			}
		}
		return l;
	}

	@SuppressWarnings("unchecked")
	public List<Class<? extends VITAL_Node>> getSelectedNodeTypes() {
		List<Class<? extends VITAL_Node>> l = new ArrayList<Class<? extends VITAL_Node>>();
		for( TreePath path : nodesTree.getCheckedPaths() ) {
			Object[] path2 = path.getPath();
			for(Object obj : path2) {
				DefaultMutableTreeNode tn = (DefaultMutableTreeNode) obj;
				HierarchyNode hn = (HierarchyNode) tn.getUserObject();
				if(l.contains(hn.cls)) continue;
				l.add((Class<? extends VITAL_Node>) hn.cls);
			}
		}
		return l;
	}
}
