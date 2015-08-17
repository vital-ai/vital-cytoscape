package ai.vital.cytoscape.app.internal.tasks;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JOptionPane;

import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyEdge.Type;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.model.Attributes;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;

public class ExpandNodesTask implements Task {

	private final static Logger log = LoggerFactory.getLogger(ExpandNodesTask.class);
	
	private View<CyNode> initialNode = null;

	private int mode = NORMAL;
	
	private TaskMonitor taskMonitor;

	//XXX
//	private boolean isInterrupted;
	
	private ExpansionThread thread;

	private String title = "";
	
	public final static int NORMAL = 0;
	
	public final static int NETWORK_ONLY = 1;
	
	public final static int MARKED_ONLY = 2;
	
	private LinkedHashSet<Long> selectedIndices = new LinkedHashSet<Long>();
	
	public ExpandNodesTask() {

		this(null,NORMAL);

	}

	/**
	 * 
	 * @param obj
	 * @param mode - one of constants, NORMAL if unrecognized
	 */
	public ExpandNodesTask(View<CyNode> obj, int mode) {
		initialNode = obj;
		if(mode >2 || mode < 0) {
			this.mode = NORMAL;
		} else {
			this.mode = mode;
		}
		
		if(mode == NORMAL) {
			title = "Expanding nodes...";
		} else if(mode == NETWORK_ONLY) {
			title = "Expanding to network nodes...";
		} else {
			title = "Expanding to selected nodes... ";
		}
		
		obj.getModel().getNetworkPointer();
		
	}

	public String getTitle() {
		return title;
	}

	@SuppressWarnings("deprecation")
	public void halt() {
//		isInterrupted = true;
		thread.stop();
	}

	public void run() {
		this.thread = new ExpansionThread();
		
		thread.start();
		
		
		while(thread.isAlive()) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		VisualStyleUtils.applyVisualStyle(VitalAICytoscapePlugin.getCurrentNetworkView());
		
	}

	public void setTaskMonitor(TaskMonitor arg0)
			throws IllegalThreadStateException {
		this.taskMonitor = arg0;
		
	}
	
	public List<View<CyNode>> processNode(View<CyNode> nv, CyNetwork cyNet, CyNetworkView myView, HashSet<Long> createdIds, List<GraphObject> objects, boolean centerNotFitContent) {

		List<CyNode> createdNodes = new ArrayList<CyNode>();
		
		List<View<CyNode>> nodeViews = new ArrayList<View<CyNode>>();
		
		nodeViews.add(nv);
		
		double x_position = nv.getVisualProperty(BasicVisualLexicon.NODE_X_LOCATION);
		double y_position = nv.getVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION);

		CyNode cn = (CyNode) nv.getModel();

		CyRow nodeAttributes = cyNet.getRow(cn);
		
		String uri_str = nodeAttributes.get(Attributes.uri, String.class);

		if (uri_str != null && !uri_str.equals("")) {

			
			//entity URI 2 attributesURIs
			HashMap<Long, GraphObject> nodeID2Entity = new HashMap<Long, GraphObject>(); 
			
			for (int k = 0; k < objects.size(); k++) {
				
				GraphObject entity = (GraphObject) objects.get(k);

				
				if (entity instanceof VITAL_Node) {

//					String nodeName = entity.getName();

					String node_uri = entity.getURI();

					if (node_uri.equals(uri_str)) {

						// skip center

//						map.put(node_uri, node_id);

						continue;
					}

					// assume new node
					
					CyNode node = Utils.getCyNodeForURIInTheNetwork(cyNet, node_uri);
					
					if(node == null) {
						
						if(mode == NORMAL) {
							
							node = Utils.createNodeAndSetAttributes(cyNet, (VITAL_Node) entity);
							
							nodeID2Entity.put(node.getSUID(), entity);
							
							createdNodes.add(node);
							createdIds.add(node.getSUID());
							log.debug("Node with URI:" + node_uri+ " added.");
						}
					} else {
						
						View<CyNode> nodeView = myView.getNodeView(node);
						log.debug("Node with URI:" + node_uri+ " already exists in this network!");
						nodeViews.add(nodeView);
						
					}
					
				}
				
			}

			for (int m = 0; m < objects.size(); m++) {
				
				GraphObject entity = (GraphObject) objects.get(m);

				if (entity instanceof VITAL_Edge) {

					VITAL_Edge relation = (VITAL_Edge) entity;
					
					String destination_uri = relation.getDestinationURI();
					String source_uri = relation.getSourceURI();

					CyNode destination_node = Utils.getCyNodeForURIInTheNetwork(cyNet, destination_uri);
					CyNode source_node = Utils.getCyNodeForURIInTheNetwork(cyNet, source_uri);

					CyNode destNode = null;
					CyNode sourceNode = null;
					
					boolean D_M = false;
					boolean D_S = false;
					boolean S_M = false;
					boolean S_S = false;
					
					if(destination_node != null) {
						
						destNode = cyNet.getNode(destination_node.getSUID());
						
						if(mode == MARKED_ONLY && destNode != null) {
							if(selectedIndices.contains(destNode.getSUID())) {
								D_S = true;
							}
							Boolean marked = cyNet.getRow(destNode).get(Attributes.marked, Boolean.class);
							if(marked != null && marked.booleanValue() == true) {
								D_M = true;
							}
						}
					}
					
					if(source_node != null) {
						sourceNode = cyNet.getNode(source_node.getSUID());
						if(mode == MARKED_ONLY && sourceNode != null) {
							if(selectedIndices.contains(sourceNode.getSUID())) {
								S_S = true;
							}
							Boolean marked = cyNet.getRow(sourceNode).get(Attributes.marked, Boolean.class);
							if(marked != null && marked.booleanValue() == true) {
								S_M = true;
							}
						}
						
					}
					
					if(mode == MARKED_ONLY) {
						
						if( (S_S && D_M) || (D_S && S_M) ) {
							
						} else {
							destNode = null;
							sourceNode = null;
						}
						
					}
					
					if(destNode == null || sourceNode == null) {
						log.debug("Relation "+ relation.getSourceURI() + "::"+relation.getClass().getSimpleName()+"::"+relation.getDestinationURI()+" cannot be added"); 
						if(destination_node == null) {
							log.debug("Destination node is null! (URI:"+destination_uri+")");
						} 
						if(source_node == null) {
							log.debug("Soruce node is null! (URI:"+source_uri+")");
						}
						continue;
					}
					
					
					List<CyEdge> connectingEdgeList = cyNet.getConnectingEdgeList(source_node, destination_node, Type.DIRECTED);
					
					//check if an edge already exists between those two nodes
					
					CyEdge found = null;
					
					for(CyEdge e : connectingEdgeList) {
						
						String eURI = cyNet.getRow(e).get(Attributes.uri, String.class);
						if(relation.getURI().equals(eURI)) {
							found = e;
							break;
						}
						
					}

					if(found != null) {
						log.debug("Edge with URI:" + entity.getURI() + " already exists.");
						continue;
					}
					
					CyEdge edge = cyNet.addEdge(source_node,destination_node, true); // directed
	
					log.debug("Edge ID: "+ edge.getSUID());
					
					Utils.setEdgeAttributes(cyNet, edge, relation);
							
				}
				
			}
			
		}
		
		if(myView != null) {
	
			VitalAICytoscapePlugin.getEventHelper().flushPayloadEvents();
			
			for(CyNode n : createdNodes) {
				
				View<CyNode> nodeView = myView.getNodeView(n);
				if(nodeView != null) {
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_X_LOCATION, x_position);
					nodeView.setVisualProperty(BasicVisualLexicon.NODE_Y_LOCATION, y_position);
					
					nodeViews.add(nodeView);
					
				}
				
			}
			

			//disabled nodes expansion here, call it once after all nodes are epxanded
			//XXX
//			TaskIterator ti = Utils.getKamadaKawaiLayout().createTaskIterator(myView, Utils.getKamadaKawaiLayout().getDefaultLayoutContext(), new HashSet<View<CyNode>>(nodeViews), "");
//			VitalAICytoscapePlugin.getDialogTaskManager().execute(ti);
			
//				Utils.getKamadaKawaiLayoutAlgorithm().setSelectedOnly(true);
				
	//			Utils.kamada_kawai.doLayout(myView);
//				
//				myView.applyLayout(Utils.getKamadaKawaiLayoutAlgorithm());
//				
//				if(!centerNotFitContent) {
//					myView.fitContent();
//				} else {
//					DingNetworkView dingNetworkView = ((DingNetworkView) myView);
//					dingNetworkView.setCenter(nv.getXPosition(), nv.getYPosition());
//				}
				
	
//			unselectAllNodes(cyNet);
			
		}
			
		return nodeViews;
	}
	
	
	@SuppressWarnings("unused")
	private void unselectAllNodes(CyNetwork cyNet) {
		for(CyNode n : cyNet.getNodeList()) {
			cyNet.getRow(n).set(CyNetwork.SELECTED, false);
		}		
	}


	private class ExpansionThread extends Thread {

		@Override
		public void run() {

	        taskMonitor.setProgress(-1D);
	        taskMonitor.setStatusMessage("Checking selected nodes...");
	        
			CyNetworkView myView = VitalAICytoscapePlugin.getCurrentNetworkView();
			CyNetwork cyNet = myView.getModel();
			
			List<View<CyEdge>> edgeViews = new ArrayList<View<CyEdge>>();
			
			for(View<CyEdge> edgeView : myView.getEdgeViews()) {
				if(Boolean.TRUE.equals(cyNet.getRow(edgeView.getModel()).get(CyNetwork.SELECTED, Boolean.class))) {
					edgeViews.add(edgeView);
				}
			}
				
			List<View<CyNode>> nodeViews = new ArrayList<View<CyNode>>();
			
			for(View<CyNode> nodeView : myView.getNodeViews()) {
				if(Boolean.TRUE.equals(cyNet.getRow(nodeView.getModel()).get(CyNetwork.SELECTED, Boolean.class))) {
					nodeViews.add(nodeView);
				}
			}
			
			View<CyNode> nv;
			
			HashSet<Long> createdIds = new HashSet<Long>();

			if(initialNode != null) {
				
				if( ! nodeViews.contains(initialNode)) {
					
					nodeViews.add(initialNode);
					
				}
				
			}
			
//			Utils.getKamadaKawaiLayoutAlgorithm().setSelectedOnly(true);
			
//			lock all existing nodes
//			Iterator nodeViewsIterator = myView.getNodeViewsIterator();
//			while(nodeViewsIterator.hasNext()) {
//				Utils.getKamadaKawaiLayoutAlgorithm().lockNode((NodeView) nodeViewsIterator.next());
//			}
			
//			Utils.kamada_kawai.lockNodes(nodeViews.toArray(new NodeView[]{}));
			

			Iterator<View<CyNode>> i = nodeViews.iterator();
			
			if(mode == MARKED_ONLY) {
				while(i.hasNext()) {
					View<CyNode> next = i.next();
					selectedIndices.add(next.getSUID());
				}
				
				i = nodeViews.iterator();
				
			}

//			LinkedList<CyNode> createdNodes = new LinkedList<CyNode>();
			
			double percentage = 0;
			
			int processed_nodes = 1;
			
			boolean centerNotFitContent = true;
			
			int size = nodeViews.size(); 
			
			if(size > 1) {
				centerNotFitContent = false;
			}
			
			double step = (double)30/(double)size;
			
	        //  0% Complete
	        taskMonitor.setProgress(0D);
			
	        int left = 100;
	        
	        //30% is the synonyms
	        /*
	        if(Application.get().isExpandUsingSynonyms() && mode == NORMAL) {
	        
	        	left = 70;
	        	
	        	
	            List<View<CyNode>> synonyms = new ArrayList<View<CyNode>>();
	            
	        	while (i.hasNext()) {

	        		taskMonitor.setStatusMessage("Searching for synonyms: node "+ processed_nodes + " of "+ size);
	        		
	        		View<CyNode> next = i.next();
	        		
	        		Long identifier = next.getSUID();
	        		
	        		String uri = cyNet.getRow(next).get(Attributes.uri, String.class);
	        		
	        		if(uri  != null && !uri.equals("")) {
	        			ASAPI_Response synonyms2 = Application.get().getSynonyms(uri);
	        			Iterator iterator = synonyms2.getObjects().iterator();
	        			LinkedList<ASAPI_Entity> objects = new LinkedList<ASAPI_Entity>();
	        			while(iterator.hasNext()) {
	        				objects.add((ASAPI_Entity) iterator.next());
	        			}
	        			
	        			LinkedList<NodeView> nodesToAdd = processNode(next, cyNet, myView, createdIds, objects, centerNotFitContent);

	        			synonyms.addAll(nodesToAdd);
	        			
	        		}
	        	
	    			percentage = percentage + step;
	    			
	    	        taskMonitor.setPercentCompleted((double) percentage);
	        		
	    	        processed_nodes ++;
	        		
	        	}
	        	
	        	nodeViews.addAll(synonyms);
	        	
	        	i = nodeViews.iterator();
	        	
	        }
	        */
	        
			processed_nodes = 0;
			size = nodeViews.size();
			step = (double)left/(double)size;
	        
			int nouris = 0;
			
			Set<View<CyNode>> allNodeViews = new HashSet<View<CyNode>>();
			
	        
			while (i.hasNext()) {
				
				taskMonitor.setStatusMessage("Expanding node " + (processed_nodes+1) + " of " + size);
				
				log.debug("Expanding node " + (processed_nodes+1) + " of " + size);
				
				nv = i.next();
				
				if(nv == null) {
					
					log.warn("Null node view");
					
				} else {
					
//					String node_id = nv.getNode().getIdentifier();
					
					//XXX
//					CyNode cn = Cytoscape.getCyNode(node_id, false);
					CyNode cn = (CyNode) nv.getModel();
					
					String uri_str = cyNet.getRow(cn).get(Attributes.uri, String.class);
					
					String typeURI = cyNet.getRow(cn).get(Attributes.nodeTypeURI, String.class);
					
					if(uri_str == null || uri_str.trim().isEmpty()) {
						
						log.warn("Node without URI attribute (" + nv.getSUID() + ") - skipping...");
						
						nouris++;
						
					} else if(typeURI == null || typeURI.trim().isEmpty()) {
						
						log.warn("Node type URI is null or empty (" + nv.getSUID() + ") - skipping...");
						
						nouris++;
						
					} else {
						
						List<GraphObject> objects = new ArrayList<GraphObject>();
						
						//get filters and direction from path tab
						long s = System.currentTimeMillis();
						ResultList rs_connections = Application.get().getConnections(uri_str, typeURI);
						for(ResultElement g : rs_connections.getResults()) {
							objects.add(g.getGraphObject());
						}
						
						log.info("Results fetch time: {}ms", System.currentTimeMillis() - s );
//						objects = filterNodesAndSegments(rs_relations);
						
						s = System.currentTimeMillis();
						List<View<CyNode>> processed = processNode(nv, cyNet, myView, createdIds, objects, centerNotFitContent);
						log.info("Results processing time: {}ms", System.currentTimeMillis() - s);
						
						allNodeViews.addAll(processed);
						
						
					}
					
					
				}
				
				
				
				
				
				
				percentage = percentage + step;
				
		        taskMonitor.setProgress(percentage / 100D);
		        
		        processed_nodes++;
				
			}
			
			taskMonitor.setStatusMessage("Done!");
			
			
//			Utils.applyVisualStyle(cyNet);
			VisualStyleUtils.applyVisualStyle(myView);
//			Utils.getKamadaKawaiLayoutAlgorithm().unlockAllNodes();
			
			
			//final select
			Iterator<View<CyNode>> iterator = nodeViews.iterator();
			while(iterator.hasNext()) {
				View<CyNode> nv_ = iterator.next();
				cyNet.getRow(nv_.getModel()).set(CyNetwork.SELECTED, true);
			}
			
			
			if(nouris > 0 && processed_nodes == nouris) {
				JOptionPane.showMessageDialog(null, "No nodes with URI property, at least one required", "Expansion error", JOptionPane.ERROR_MESSAGE);
			}
			
			if(allNodeViews.size() > 0) {
				
				TaskIterator ti = Utils.getKamadaKawaiLayout().createTaskIterator(myView, Utils.getKamadaKawaiLayout().getDefaultLayoutContext(), new HashSet<View<CyNode>>(allNodeViews), "");
				DialogTaskManager dialogTaskManager = VitalAICytoscapePlugin.getDialogTaskManager();
				dialogTaskManager.execute(ti);
				
			}

			taskMonitor.setProgress(1D);
			
			
		}
		
	}


	@Override
	public void cancel() {
		halt();
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {

		setTaskMonitor(tm);
		
		run();
		
	}

}
