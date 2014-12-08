package ai.vital.cytoscape.app.internal.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.PropertyInterface;
import ai.vital.vitalsigns.model.URIPropertyValue;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;

public class Utils {

	private final static Logger log = LoggerFactory.getLogger(Utils.class);
	
	private static final String CELLULAR_COMPONENT = "cellular_component";
	private static final String BIOLOGICAL_PROCESS = "biological_process";
	private static final String MOLECULAR_FUNCTION = "molecular_function";

	
	private static HashSet<String> excludedGOAttributes = new HashSet<String>();
	static {
		excludedGOAttributes.add(BIOLOGICAL_PROCESS);
		excludedGOAttributes.add(CELLULAR_COMPONENT);
		excludedGOAttributes.add(MOLECULAR_FUNCTION);
	}
	
	
	
	
	public static String virtualConnector_NodeTypeID = "memomics.hypernode.virtual-connector";
	
	
	public static final String vitalVisualStyleName = "vitalVisualStyle";
	
//	public static final CyLayoutAlgorithm forceDirectedLayout = CyLayouts.getLayout("force-directed");
	
//	public static final CyLayoutAlgorithm jgraph_radial_tree = CyLayouts.getLayout("jgraph-radial-tree");
//	public static GridNodeLayout defaultLayout = new GridNodeLayout();
	
//	private static CyLayoutAlgorithm jgraph_spring = null; // CyLayouts.getLayout("jgraph-spring");

//	private static CyLayoutAlgorithm kamada_kawai = null; //CyLayouts.getLayout("kamada-kawai");
	
//	private static CyLayoutAlgorithm force_directed = null; //CyLayouts.getLayout("force-directed");
	
	private static boolean initialized = false;

	/*
	public static CyLayoutAlgorithm getKamadaKawaiLayoutAlgorithm() {
		initLayouts();
		if(kamada_kawai != null) {
			return kamada_kawai;
		} 
		
		if(force_directed != null) {
			return force_directed;
		}
		
		if(jgraph_spring != null) {
			return jgraph_spring;
		}
		
		return defaultLayout;
		
	}
	

	public static CyLayoutAlgorithm getForceDirectedLayout() {
		initLayouts();
		if(force_directed != null) {
			return force_directed;
		}
		
		if(jgraph_spring != null) {
			return jgraph_spring;
		} 
		
		if(kamada_kawai != null) {
			return kamada_kawai;
		}
		
		return defaultLayout;
	}
	

	
    private static synchronized void initLayouts() {
    	
    	if(initialized) {
    		return;
    	}
    	
    	jgraph_spring = CyLayouts.getLayout("jgraph-spring");

    	kamada_kawai = CyLayouts.getLayout("kamada-kawai");
    	
    	if(kamada_kawai == null) {
    		kamada_kawai = CyLayouts.getLayout("Kamada-Kawai");
    	}
    	
    	force_directed = CyLayouts.getLayout("force-directed");
    	
    	initialized = true;
		
	}

	*/


	public static final double CONNECTOR_NODE_SIZE = 10.0;
	
	private static CyLayoutAlgorithm grid = null;	
	
	private static CyLayoutAlgorithm kamadaKawai = null;
	
	public static void placeNodesInTheNetwork(CyNetwork cyNetwork, List<GraphObject> entities) {
		placeNodesInTheNetwork(cyNetwork, entities.toArray(new GraphObject[]{}));
	}
	
	@SuppressWarnings("unchecked")
	public static void placeNodesInTheNetwork(CyNetwork cyNetwork,
			GraphObject[] entities) {

//		ConsolePanel.logLine("Importing "+entities.length+ " node(s) into the network '"+cyNetwork.getTitle()+"'");
		
		CyNetworkView networkView = VitalAICytoscapePlugin.getNetworkView(cyNetwork);
		
		LinkedList<CyNode> placedNodes = new LinkedList<CyNode>();
		
		HashSet<Long> ids = new HashSet<Long>();
		
		for (int i = 0; i < entities.length; i++) {
			GraphObject entity = (GraphObject) entities[i];

			//use uri as an identifier
			String uri = entity.getURI();
			
			
			if(nodeURIExistsInTheNetwork(cyNetwork, uri)) continue;

			CyNode createdNode = (CyNode) createNodeForURI(cyNetwork, uri);
			
			setNodeAttributes(cyNetwork, createdNode, (VITAL_Node) entity);
			
			placedNodes.add(createdNode);
			
			ids.add(createdNode.getSUID());

			
		}
		
		if(placedNodes.size() > 0) {

			VitalAICytoscapePlugin.getEventHelper().flushPayloadEvents();
			
			Set<View<CyNode>> nodeViews = new HashSet<View<CyNode>>();
			
			if(networkView != null) {
				
				for(CyNode n : placedNodes){
					
					View<CyNode> nodeView = networkView.getNodeView(n);
					
					if(nodeView != null) {
						nodeViews.add(nodeView);
					}
					
					
				}
				
				log.debug("Node views: " + nodeViews.size());
				
				TaskIterator createTaskIterator = getGridLayout().createTaskIterator(networkView, getGridLayout().getDefaultLayoutContext(), nodeViews, "");
				
				VitalAICytoscapePlugin.getDialogTaskManager().execute(createTaskIterator);
				
//				networkView.fitContent();
				
			}
			
		}
		
		
		
//		Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, cyNetwork);
		
	
//		
//		CyNetworkView networkView = Cytoscape.getNetworkView(cyNetwork.getIdentifier());
//		if(networkView!=null) {
//			
//			networkView.applyLayout(defaultLayout);
//			applyVisualStyle(cyNetwork);
//						
//		}
	        
		
        /*
        if (networkView != null) {
            CyLayoutAlgorithm layoutAlgorithm = graphReader.getLayoutAlgorithm();
            if (layoutAlgorithm != null) {
                layoutAlgorithm.doLayout(networkView);
            }
        }
         */
		
		
	}
	
	@SuppressWarnings("unchecked")
	public static void placeNodesEdgesInTheNetwork(CyNetwork cyNetwork,
			VITAL_Node[] nodes, VITAL_Edge[] edges) {

//		ConsolePanel.logLine("Importing "+entities.length+ " node(s) into the network '"+cyNetwork.getTitle()+"'");
		
		CyNetworkView networkView = VitalAICytoscapePlugin.getNetworkView(cyNetwork);
		
		LinkedList<CyNode> placedNodes = new LinkedList<CyNode>();
		
		HashSet<Long> ids = new HashSet<Long>();
		
		List<View<CyNode>> nodeViews = new ArrayList<View<CyNode>>();
		List<View<CyEdge>> edgeViews = new ArrayList<View<CyEdge>>();
		
		for (int i = 0; i < nodes.length; i++) {
			
			GraphObject entity = (GraphObject) nodes[i];

			//use uri as an identifier
			String uri = entity.getURI();
			
			
			if(nodeURIExistsInTheNetwork(cyNetwork, uri)) continue;

			CyNode createdNode = (CyNode) createNodeForURI(cyNetwork, uri);
			
			setNodeAttributes(cyNetwork, createdNode, (VITAL_Node) entity);
			
			placedNodes.add(createdNode);
			
			ids.add(createdNode.getSUID());

			if(networkView != null) {
				View<CyNode> nv = networkView.getNodeView(createdNode);
				if(nv!=null) nodeViews.add(nv);
			}
			
		}
		
		
		
		for(int i = 0; i < edges.length; i++) {
			
			VITAL_Edge edge = edges[i];
			
			String uri = edge.getURI();
			
			if(edgeURIExistsInTheNetwork(cyNetwork, uri)) continue;
			
			CyNode sourceNode = getCyNodeForURIInTheNetwork(cyNetwork, edge.getSourceURI());
			
			if(sourceNode == null) continue;
			
			CyNode destNode = getCyNodeForURIInTheNetwork(cyNetwork, edge.getDestinationURI());
			
			if(destNode == null) continue;
			
			CyEdge cyEdge = cyNetwork.addEdge(sourceNode, destNode, true); // directed

			log.debug("Edge ID: "+ cyEdge.getSUID());
			
			Utils.setEdgeAttributes(cyNetwork, cyEdge, edge);

			if(networkView != null) {
				View<CyEdge> ev = networkView.getEdgeView(cyEdge);
				if(ev!=null) edgeViews.add(ev);
			}
			
			
			
		}
		
		log.info("Import node views: " + nodes.length);
		log.info("Import edge views: " + edges.length);
		
		//grid layout for node views only
		if(nodeViews.size() > 0 && edgeViews.size() == 0) {

			VitalAICytoscapePlugin.getEventHelper().flushPayloadEvents();
			
			if(networkView != null) {
				
				for(CyNode n : placedNodes){
					
					View<CyNode> nodeView = networkView.getNodeView(n);
					
					if(nodeView != null) {
						nodeViews.add(nodeView);
					}
					
					
				}
				
				log.debug("Node views: " + nodeViews.size());
				
				TaskIterator createTaskIterator = getGridLayout().createTaskIterator(networkView, getGridLayout().getDefaultLayoutContext(), new HashSet<View<CyNode>>(nodeViews), "");
				
				VitalAICytoscapePlugin.getDialogTaskManager().execute(createTaskIterator);
				
//				networkView.fitContent();
				
			}
			
		}
		
		if(nodeViews.size() > 0 && edgeViews.size() > 0) {
			
			VitalAICytoscapePlugin.getEventHelper().flushPayloadEvents();
			
			TaskIterator ti = Utils.getKamadaKawaiLayout().createTaskIterator(networkView, Utils.getKamadaKawaiLayout().getDefaultLayoutContext(), new HashSet<View<CyNode>>(nodeViews), "");
			
			VitalAICytoscapePlugin.getDialogTaskManager().execute(ti);
			
		}
		
		
		
		
		
//		Cytoscape.firePropertyChange(Cytoscape.NETWORK_MODIFIED, null, cyNetwork);
		
	
//		
//		CyNetworkView networkView = Cytoscape.getNetworkView(cyNetwork.getIdentifier());
//		if(networkView!=null) {
//			
//			networkView.applyLayout(defaultLayout);
//			applyVisualStyle(cyNetwork);
//						
//		}
	        
		
        /*
        if (networkView != null) {
            CyLayoutAlgorithm layoutAlgorithm = graphReader.getLayoutAlgorithm();
            if (layoutAlgorithm != null) {
                layoutAlgorithm.doLayout(networkView);
            }
        }
         */
		
		
	}
	
	private static CyLayoutAlgorithm getGridLayout() {
		if(grid == null) {
			grid = VitalAICytoscapePlugin.getLayoutAlgorithmManager().getLayout("grid");
			if(grid == null) throw new RuntimeException("No 'grid' layout found - cannot proceed!");
		}
		return grid;
	}

	private static void setNodesState(CyNetwork cyNetwork,
			LinkedList<CyNode> placedNodes, boolean b) {
		// TODO Auto-generated method stub
		
	}

	public static CyNode createNodeAndSetAttributes(CyNetwork cyNetwork, VITAL_Node nodeBase) {
		CyNode createdNode = createNodeForURI(cyNetwork, nodeBase.getURI());
		setNodeAttributes(cyNetwork, createdNode, nodeBase);
		return createdNode;
	}
	
	public static CyNode createNodeForURI(CyNetwork cyNetwork, String uri) {

		CyNode cyNode = cyNetwork.addNode();
		
//		CyNetworkView networkView = VitalAICytoscapePlugin.getNetworkView(cyNetwork);
//		networkView.
		
		
		return cyNode;
		
	}
	

//	public static void placeNodesInTheNetwork(CyNetwork cyNetwork, List<Entity> entities) {
//		placeNodesInTheNetwork(cyNetwork, entities.toArray(new Entity[]{}));
//	}
	
	/*
	public static void applyVisualStyle(CyNetwork network) {
		applyVisualStyle(network, vitalVisualStyleName);
	}
	*/
	
	/*XXX implement me
	public static void applyVisualStyle(CyNetwork network, String styleName) {
	
		VitalAICytoscapePlugin.get
		
		
			// get the network and view
//			CyNetwork network = Cytoscape.getCurrentNetwork();
//			CyNetworkView networkView = Cytoscape.getCurrentNetworkView();

			CyNetworkView networkView = Cytoscape.getNetworkView(network.getIdentifier());
		
			String currentStyleName = networkView.getVisualStyle().getName();
			log.debug("Current style name : " + currentStyleName);
//			if(currentStyleName.equals(memomicsVisualStyleName)) {
//				log.debug("No need to change style.");
//				defaultLayout.doLayout(networkView);
//				return;
//			}
			// get the VisualMappingManager and CalculatorCatalog
			VisualMappingManager manager = Cytoscape.getVisualMappingManager();
			CalculatorCatalog catalog = manager.getCalculatorCatalog();

			// check to see if a visual style with this name already exists
			VisualStyle vs = catalog.getVisualStyle(styleName);
			if (vs == null) {
				log.debug("Created visual style: " + styleName);
				// if not, create it and add it to the catalog
				vs = createVisualStyle(network);
				catalog.addVisualStyle(vs);
			}

			
			networkView.setVisualStyle(vs.getName()); // not strictly necessary

			// actually apply the visual style
			manager.setVisualStyle(vs);
			Cytoscape.getVisualMappingManager().applyAppearances();
			networkView.redrawGraph(true,true);
	}
	 */
	
	
	
	/*
	public static VisualStyle createVisualStyle(CyNetwork network) {
			
			NodeAppearanceCalculator nodeAppCalc = new NodeAppearanceCalculator();
			EdgeAppearanceCalculator edgeAppCalc = new EdgeAppearanceCalculator();
			GlobalAppearanceCalculator globalAppCalc = new GlobalAppearanceCalculator(); 

			//default NODE appearance
			NodeAppearance defaultNodeAppearance = nodeAppCalc.getDefaultAppearance();
//			defaultAppearance.set(VisualPropertyType.NODE_FILL_COLOR,Color.blue);
			NodeShape defaultShape = NodeShape.ELLIPSE;
			defaultNodeAppearance.set(VisualPropertyType.NODE_SHAPE,defaultShape);
			defaultNodeAppearance.set(VisualPropertyType.NODE_LINE_WIDTH, 1);
			defaultNodeAppearance.set(VisualPropertyType.NODE_BORDER_COLOR, Color.black);
			
			
			// Passthrough Mapping - set node label 
//			PassThroughMapping pm = new PassThroughMapping(new String(), "attr2");
			PassThroughMapping pm = new PassThroughMapping(new String(), nodeLabelAttributeName);
			Calculator nlc = new BasicCalculator("Example Node Label Calculator", 
			                                     pm, VisualPropertyType.NODE_LABEL);
			nodeAppCalc.setCalculator(nlc);

			//default green color
			Color defColor = new Color(195,255,220);
			
			
			DiscreteMapping disMapping = Application.get().getCustomNodeColorMapping(network);
			
			if(disMapping == null) {
			// Discrete Mapping - set node fill color
			disMapping = new DiscreteMapping(defColor,
					ObjectMapping.NODE_MAPPING);
			}
			
			// Discrete Mapping - set node shape
			DiscreteMapping disNodeShapeMapping = new DiscreteMapping(defaultShape,
					ObjectMapping.NODE_MAPPING);
			disNodeShapeMapping.setControllingAttributeName(Attributes.nodeTypeURI, network, false);
			
//			disNodeShapeMapping.putMapValue(ASAPI_HyperNodeTypes.RELATION, NodeShape.TRIANGLE);
//			disNodeShapeMapping.putMapValue(MemomicsNodeTypes.ORGANIZATION, NodeShape.ROUND_RECT);
//			disNodeShapeMapping.putMapValue(MemomicsNodeTypes.DRUG, NodeShape.HEXAGON);
////			disNodeShapeMapping.putMapValue(virtualSummarization_fact_NodeTypeID, NodeShape.TRIANGLE);
////			disNodeShapeMapping.putMapValue(virtualSummarization_relation_NodeTypeID, NodeShape.TRIANGLE);
//			disNodeShapeMapping.putMapValue(MemomicsNodeTypes.JOURNAL_ARTICLE, NodeShape.RECT);
//			disNodeShapeMapping.putMapValue(MemomicsNodeTypes.PUBMED, NodeShape.RECT);
			
//			disNodeShapeMapping.putMapValue(MemomicsNodeTypes.DOCUMENT, NodeShape.RECT);
//			disNodeShapeMapping.putMapValue(NodeTypeIDs.BIO_CHEM + ":" + BioChemCategoryIDs.PROCESS, NodeShape.OCTAGON);
//			disNodeShapeMapping.putMapValue(NodeTypeIDs.BIO_CHEM + ":" + BioChemCategoryIDs.PROCESS, NodeShape.OCTAGON);
//			disMapping.putMapValue(NodeTypeIDs.BIO_CHEM, new Color(212,254,212));
			
			//TODO
//			disNodeShapeMapping.putMapValue(Categories.CLINICAL_TRIAL, NodeShape.RECT_3D);
//			disNodeShapeMapping.putMapValue(Categories.COMPANY, NodeShape.HEXAGON);
//			
//			disNodeShapeMapping.putMapValue(Categories.DRUG, NodeShape.OCTAGON);
//			disNodeShapeMapping.putMapValue(Categories.DISEASE, NodeShape.PARALLELOGRAM);
//			
//			disNodeShapeMapping.putMapValue(Categories.GENE, NodeShape.DIAMOND);
//			
//			disNodeShapeMapping.putMapValue(Categories.PATENT, NodeShape.RECT);
//			disNodeShapeMapping.putMapValue(Categories.PROCESS, NodeShape.ROUND_RECT);
//			disNodeShapeMapping.putMapValue(Categories.PROTEIN, NodeShape.TRAPEZOID);
//			
//			disNodeShapeMapping.putMapValue(Categories.SUBSTANCE, NodeShape.TRAPEZOID_2);
//			disNodeShapeMapping.putMapValue(Categories.SYMBOL, NodeShape.TRIANGLE);
//			disNodeShapeMapping.putMapValue(Categories.SYNSET, defaultShape);
//			
//			disNodeShapeMapping.putMapValue(Categories.TESTNODE, defaultShape);
	


			Calculator nodeShapeCalculator = new BasicCalculator("Node Shape Calculator",
					disNodeShapeMapping,
					VisualPropertyType.NODE_SHAPE);
			nodeAppCalc.setCalculator(nodeShapeCalculator);
			
			
			// Discrete Mapping - set node line width
			DiscreteMapping discreteBorderLineWidthMapping = new DiscreteMapping(3,
					ObjectMapping.NODE_MAPPING);
			discreteBorderLineWidthMapping.setControllingAttributeName(Attributes.marked, network, false);
			discreteBorderLineWidthMapping.putMapValue(new Boolean(true), 5);
			
			Calculator nodeBorderLineWidthCalculator = new BasicCalculator("Node border opaque calculator",
					discreteBorderLineWidthMapping,
					VisualPropertyType.NODE_LINE_WIDTH);
			nodeAppCalc.setCalculator(nodeBorderLineWidthCalculator);

			// Discrete Mapping - set node opaque
			Color defaultBorder = Color.black;
			DiscreteMapping discreteBorderColorMapping = new DiscreteMapping(defaultBorder,
					ObjectMapping.NODE_MAPPING);
			discreteBorderColorMapping.setControllingAttributeName(Attributes.marked, network, false);
			discreteBorderColorMapping.putMapValue(new Boolean(true), Color.red);
			
			Calculator nodeBorderColorCalculator = new BasicCalculator("Node border color calculator",
					discreteBorderColorMapping,
					VisualPropertyType.NODE_BORDER_COLOR);
			nodeAppCalc.setCalculator(nodeBorderColorCalculator);
			
			
			
			//default EDGE appearance
			
			Color defaultEdgeColor = new Color(0,0,100);
			
			Color directRelationColor = new Color(25,164,25);
			
			Color inDirectRelationColor = new Color(0,255,0);
			
			EdgeAppearance defaultEdgeAppearance = edgeAppCalc.getDefaultAppearance();
			defaultEdgeAppearance.set(VisualPropertyType.EDGE_LINE_WIDTH,2);
			defaultEdgeAppearance.set(VisualPropertyType.EDGE_COLOR, defaultEdgeColor);
			defaultEdgeAppearance.set(VisualPropertyType.EDGE_TGTARROW_COLOR, defaultEdgeColor);
			defaultEdgeAppearance.set(VisualPropertyType.EDGE_TGTARROW_SHAPE, ArrowShape.ARROW);

			// Discrete Mapping - set edge color 
			DiscreteMapping edgeColorMapping = new DiscreteMapping(defaultEdgeColor,
					ObjectMapping.EDGE_MAPPING);
			edgeColorMapping.setControllingAttributeName(Attributes.edgeTypeURI, network, false);
			
			//edgeColorMapping.putMapValue(ASAPI_HyperEdgeTypes.SIMPLE_RELATION_PART, directRelationColor);
			
			
//			edgeColorMapping.putMapValue(EdgeTypeIDs.RELATION_BIND, directRelationColor);
//			edgeColorMapping.putMapValue(virtualSummarization_fact_EdgeTypeID, directRelationColor);
			
//			edgeColorMapping.putMapValue(EdgeTypeIDs.NORMALIZATION_SUMMARIZATION, inDirectRelationColor);
//			edgeColorMapping.putMapValue(virtualSummarization_relation_EdgeTypeID, inDirectRelationColor);
			
//			edgeColorMapping.putMapValue("DEFINITION", Color.blue);
//			edgeColorMapping.putMapValue("SYNONYM", Color.blue);
//			edgeColorMapping.putMapValue("edge_bind", Color.LIGHT_GRAY);
			
			
			
			
			Calculator edgeColorCalculator = new BasicCalculator("Edge color calculator",
					edgeColorMapping,
					VisualPropertyType.EDGE_COLOR);
			edgeAppCalc.setCalculator(edgeColorCalculator);
			
			// Discrete Mapping - set edge target arrow head color 
			DiscreteMapping edgeTargetArrowHeadMapping = new DiscreteMapping(defaultEdgeColor,
					ObjectMapping.EDGE_MAPPING);
			edgeTargetArrowHeadMapping.setControllingAttributeName(Attributes.edgeTypeURI, network, false);
			
//			edgeTargetArrowHeadMapping.putMapValue(EdgeTypeIDs.RELATION_GENE_2_GO, directRelationColor);
//			edgeTargetArrowHeadMapping.putMapValue(EdgeTypeIDs.RELATION_BIND, directRelationColor);
//			edgeTargetArrowHeadMapping.putMapValue(ASAPI_HyperEdgeTypes.SIMPLE_RELATION_PART, directRelationColor);
//			edgeTargetArrowHeadMapping.putMapValue(virtualSummarization_fact_EdgeTypeID, directRelationColor);
			
//			edgeTargetArrowHeadMapping.putMapValue(EdgeTypeIDs.NORMALIZATION_SUMMARIZATION, inDirectRelationColor);
//			edgeTargetArrowHeadMapping.putMapValue(virtualSummarization_relation_EdgeTypeID, inDirectRelationColor);
//			edgeTargetArrowHeadMapping.putMapValue("edge_isa", Color.green);
//			edgeTargetArrowHeadMapping.putMapValue("DEFINITION", Color.blue);
//			edgeTargetArrowHeadMapping.putMapValue("SYNONYM", Color.blue);
//			edgeTargetArrowHeadMapping.putMapValue("edge_bind", Color.yellow);

//			for(int i = 0 ; i < array.length ; i ++) {
//				edgeTargetArrowHeadMapping.putMapValue(array[i],Color.yellow);
//			}
			
			Calculator edgeTargetArrowHeadCalculator = new BasicCalculator("Edge arrow head color calculator",
					edgeTargetArrowHeadMapping,
					VisualPropertyType.EDGE_TGTARROW_COLOR);
			edgeAppCalc.setCalculator(edgeTargetArrowHeadCalculator);
			
			
			//target arrow shape mapping
	        
	        
	        LineStyle defaultLineStyle = LineStyle.SOLID;
			DiscreteMapping edgeLineMapping = new DiscreteMapping(defaultLineStyle, ObjectMapping.EDGE_MAPPING);
			edgeLineMapping.setControllingAttributeName(Attributes.edgeTypeURI, network, false);
			
//			edgeLineMapping.putMapValue(virtualSummarization_fact_EdgeTypeID, LineStyle.LONG_DASH);
//			edgeLineMapping.putMapValue(ASAPI_HyperEdgeTypes.SIMPLE_RELATION_PART, LineStyle.LONG_DASH);
//			edgeLineMapping.putMapValue(ASAPI_HyperEdgeTypes.HAS_RELATION_PART, LineStyle.LONG_DASH);
//			edgeLineMapping.putMapValue(virtualSummarization_relation_EdgeTypeID, LineStyle.LONG_DASH);
			
			Calculator edgeLineCalculator = new BasicCalculator("Edge line type calculator", edgeLineMapping, VisualPropertyType.EDGE_LINE_STYLE);
			edgeAppCalc.setCalculator(edgeLineCalculator);
	        

			
			// Continuous Mapping - set node color 
			ContinuousMapping continuousMapping = new ContinuousMapping(2, 
	                                                            ObjectMapping.EDGE_MAPPING);
			continuousMapping.setControllingAttributeName(Attributes.relationsCount, network, false);

	        Interpolator numToColor = new LinearNumberToNumberInterpolator();
	        continuousMapping.setInterpolator(numToColor);

//			Color underColor = Color.GRAY;
//			Color minColor = Color.RED;
//			Color midColor = Color.WHITE;
//			Color maxColor = Color.GREEN;
//			Color overColor = Color.BLUE;

			// Create boundary conditions                  less than,   equals,  greater than
			BoundaryRangeValues bv0 = new BoundaryRangeValues(1, 1, 1);
			BoundaryRangeValues bv1 = new BoundaryRangeValues(10, 10, 10);
			BoundaryRangeValues bv2 = new BoundaryRangeValues(100, 100, 100);

	        // Set the attribute point values associated with the boundary values 
			continuousMapping.addPoint(1.0, bv0);
			continuousMapping.addPoint(10.0, bv1);
			continuousMapping.addPoint(100.0, bv2);
			
			Calculator edgeWidthCalculator = new BasicCalculator("Edge width calculator", 
			                                                continuousMapping, 
														 VisualPropertyType.EDGE_LINE_WIDTH);
			edgeAppCalc.setCalculator(edgeWidthCalculator);


			
			
			// Create the visual style
			globalAppCalc.setDefaultBackgroundColor(new Color(240,255,240));
			
			VisualStyle visualStyle = new VisualStyle(memomicsVisualStyleName, nodeAppCalc, edgeAppCalc, globalAppCalc);
			
			return visualStyle;
	}
	*/
	
	public static void setNodeAttributes(CyNetwork network, CyNode node, VITAL_Node entity) {

		CyRow nodeAttributes = network.getRow(node);
		
		createIfNotExists(Attributes.uri, String.class, nodeAttributes);
		nodeAttributes.set(Attributes.uri, entity.getURI());
		
		createIfNotExists(Attributes.canonicalName, String.class, nodeAttributes);
		nodeAttributes.set(Attributes.canonicalName, PropertyUtils.resolveName(entity));
//		nodeAttributes.setAttribute(id, Attributes.category, entity.getCategoryID());
//		nodeAttributes.setAttribute(id, Attributes.scope, entity.getScopeType().toString());
//		nodeAttributes.setAttribute(id, Attributes.segment, entity.getSegmentName());
//		nodeAttributes.setAttribute(id, Attributes.numInteractionEdges, entity.getNumInteractionEdges());
//		nodeAttributes.setAttribute(id, Attributes.numSubsumptionEdges, entity.getNumSubsumptionEdges());
		if(entity.getProperty("timestamp") != null ) {
			createIfNotExists("timestamp", String.class, nodeAttributes);
			nodeAttributes.set(Attributes.creationDate, TimeUtils.convertDate((Long)entity.getProperty("timestamp")));
		}
//		nodeAttributes.setAttribute(id, Attributes.knownDate, TimeUtils.convertDate(entity.getKnownDate()));
//		nodeAttributes.setAttribute(id, Attributes.modificationDate, TimeUtils.convertDate(entity.getModificationDate()));
		
		
//		String typeID = null;
//		String categoryID = null;
		
		
		HashMap<String, HashSet<String>> go_cats = new HashMap<String, HashSet<String>>();
		go_cats.put(Attributes.GO_BIOLOGICAL_PROCESS, new HashSet<String>());
		go_cats.put(Attributes.GO_CELLULAR_COMPONENT, new HashSet<String>());
		go_cats.put(Attributes.GO_MOLECULAR_FUNCTION, new HashSet<String>());
		
		
		for( Iterator<Entry<String, PropertyInterface>> iterator = entity.getProperties().entrySet().iterator(); iterator.hasNext(); ) {
			
			Entry<String, PropertyInterface> next = iterator.next();
			
			String propName = next.getKey();
			
			setProperty(nodeAttributes, next.getValue());
		}
		
//		if(typeID != null && categoryID != null) {
//			nodeAttributes.setAttribute(id, Attributes.type_id, typeID + ":" + categoryID);
//		} else if(typeID != null) {
//			nodeAttributes.setAttribute(id, Attributes.type_id, typeID);
//		}
		
		createIfNotExists(Attributes.nodeTypeURI, String.class, nodeAttributes);
		nodeAttributes.set(Attributes.nodeTypeURI, VitalSigns.get().getRDFClass(entity.getClass()));
		
		
		/*XXX restore?
		for( Entry<String, HashSet<String>> entry : go_cats.entrySet() ) {
			
			String key = entry.getKey();
			ArrayList<String> values = new ArrayList<String>(entry.getValue());
			
//			List listAttribute = nodeAttributes.getListAttribute(id, key);
//			if(listAttribute != null) {
//				
//				listAttribute.clear();
//				listAttribute.addAll(values);
//				
//			} else {
			
				List listAttribute_ = nodeAttributes.getListAttribute(long1, key);
			
				Collections.sort(values);
				
//				//XXX
//				//extend by 2
//				int stop = 0;
//				if(listAttribute_ != null && listAttribute_.size() > 0) {
//					stop = listAttribute_.size();
//				}
//				
//				for(int i = 0 ; i < stop + 2; i++) {
//					values.add("TEST" + ( i + 1 ));
//				}

				if(values == null || values.size() == 0) {

					boolean exists = false;
					
					String[] attributeNames = nodeAttributes.getAttributeNames();
					for(String s : attributeNames ) {
						if(s.equals(key)) {
							exists = true;
							break;
						}
					}
					
					//conditional
					if(exists) {
						nodeAttributes.deleteAttribute(long1, key);
					}
					
				} else {
					
					nodeAttributes.setListAttribute(long1, key, values);
					
				}
				
				
				
				
				List listAttribute = nodeAttributes.getListAttribute(long1, key);
				
				
				
				
				boolean userEditable = nodeAttributes.getUserEditable(key);
				boolean userVisible = nodeAttributes.getUserVisible(key);
				
				log.debug();
				
//			}
			
			
		}
		*/
		
	}
	
	private static void createIfNotExists(String c, Class t, CyRow nodeAttributes) {

		CyTable table = nodeAttributes.getTable();
		CyColumn column = table.getColumn(c);
		if(column == null) {
			table.createColumn(c, t, false);
		}
		
	}

	private static boolean isGOAnnotation(String propName) {
		return 	propName.equals(BIOLOGICAL_PROCESS) ||
			propName.equals(CELLULAR_COMPONENT) ||
			propName.equals(MOLECULAR_FUNCTION);
	}

	private static void setProperty(CyRow attributes, PropertyInterface propertyO) {
		
		if(propertyO.getValue() == null) {
			log.debug( "Property \"" + propertyO.getShortName() + "\" value is null" );
			return;
		}
		
		Object property = propertyO.getValue();
		
		String n = propertyO.getShortName();
		
		if(property instanceof Boolean) {
			createIfNotExists(n, Boolean.class, attributes);
			attributes.set(n, (Boolean)property);
		} else if(property instanceof Date) {
			createIfNotExists(n, String.class, attributes);
			attributes.set(n, TimeUtils.convertDate((Date)property));
		} else if(property instanceof Double) {
			createIfNotExists(n, Double.class, attributes);
			attributes.set(n, (Double)property);
		} else if(property instanceof Integer) {
			createIfNotExists(n, Integer.class, attributes);
			attributes.set(n, (Integer)property);
		} else if(property instanceof String) {
			createIfNotExists(n, String.class, attributes);
			attributes.set(n, (String)property);
		} else if(property instanceof URIPropertyValue) {
			createIfNotExists(n, String.class, attributes);
			attributes.set(n, ((URIPropertyValue)property).getURI());
		} 
	}
	
	
	public static boolean nodeURIExistsInTheNetwork(CyNetwork cyNetwork, String uri) {
		return getCyNodeForURIInTheNetwork(cyNetwork, uri) != null;
	}
	
	public static boolean edgeURIExistsInTheNetwork(CyNetwork cyNetwork, String uri) {
		return getCyEdgeForURIInTheNetwork(cyNetwork, uri) != null;
	}
	
	public static CyEdge getCyEdgeForURIInTheNetwork(CyNetwork cyNetwork, String edgeURI) {
		for(CyEdge edge : cyNetwork.getEdgeList()) {
			CyRow row = cyNetwork.getRow(edge);
			String s = row.get(Attributes.uri, String.class);

			if(s == null || s.isEmpty()) continue;
			
			if(edgeURI.equals(s)) {
				return edge;
			}
			
		}
		
		return null;
			
	}
	
	public static CyNode getCyNodeForURIInTheNetwork(CyNetwork cyNetwork, String uri) {
		
		for(CyNode node : cyNetwork.getNodeList()) {
			
			CyRow row = cyNetwork.getRow(node);
			
			String s = row.get(Attributes.uri, String.class);

			if(s == null || s.isEmpty()) continue;
			
			if(uri.equals(s)) {
				return node;
			}
			
		}
		
		return null;
	}
	
	
	public static CyNode getCyNodeForIdentifier(CyNetwork cyNetwork, Long identifier) {

		return cyNetwork.getNode(identifier);
		
	}

	public static void setEdgeAttributes(CyNetwork cyNet, CyEdge edge,
			VITAL_Edge relation) {

		CyRow r = cyNet.getRow(edge);
		
		createIfNotExists(Attributes.uri, String.class, r);
		r.set(Attributes.uri, relation.getURI());
		
		createIfNotExists(Attributes.edgeTypeURI, String.class, r);
		r.set(Attributes.edgeTypeURI, VitalSigns.get().getRDFClass(relation.getClass()));
		
	}

	public static CyLayoutAlgorithm getKamadaKawaiLayout() {

		if(kamadaKawai == null) {
			
			kamadaKawai = VitalAICytoscapePlugin.getLayoutAlgorithmManager().getLayout("kamada-kawai");
			if(kamadaKawai == null) throw new RuntimeException("No 'kamada-kawai' layout found - cannot proceed!");
			
		}
		
		return kamadaKawai;
	}


	/*
	public static VisualStyle createScoredVisualStyle(CyNetwork network) {
		
		VisualStyle baseStyle = createVisualStyle(null);
		
		baseStyle.setName(memomicsScoredVisualStyleName);

		
		// Continuous Mapping - set node color 
		ContinuousMapping continuousMapping = new ContinuousMapping(-1, 
                                                            ObjectMapping.NODE_MAPPING);
		continuousMapping.setControllingAttributeName(Attributes.score, network, false);

        Interpolator numToColor = new LinearNumberToNumberInterpolator();
        continuousMapping.setInterpolator(numToColor);

//		Color underColor = Color.GRAY;
//		Color minColor = Color.RED;
//		Color midColor = Color.WHITE;
//		Color maxColor = Color.GREEN;
//		Color overColor = Color.BLUE;

		// Create boundary conditions                  less than,   equals,  greater than
		BoundaryRangeValues bv0 = new BoundaryRangeValues(1, 1, 1);
		BoundaryRangeValues bv1 = new BoundaryRangeValues(15, 15, 15);
//		BoundaryRangeValues bv2 = new BoundaryRangeValues(100, 100, 100);

        // Set the attribute point values associated with the boundary values 
		continuousMapping.addPoint(1.0, bv0);
		continuousMapping.addPoint(15.0, bv1);
//		continuousMapping.addPoint(100.0, bv2);
		
		
		
		Calculator nodeSizeCalculator = new BasicCalculator("Node size calculator", 
		                                                continuousMapping, 
													 VisualPropertyType.NODE_SIZE);
		
		baseStyle.getNodeAppearanceCalculator().setCalculator(nodeSizeCalculator);
		
		return baseStyle;
		
		
	}
	
	*/



	
}
