package ai.vital.cytoscape.app.internal.app;

import java.awt.Dimension;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Properties;

import javax.swing.JLabel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.CyActivator;
import ai.vital.cytoscape.app.internal.app.Application.LoginListener;
import ai.vital.cytoscape.app.internal.tabs.ConnectionTab;
import ai.vital.cytoscape.app.internal.tabs.DatascriptsTab;
import ai.vital.cytoscape.app.internal.tabs.MainTabsPanel;
import ai.vital.cytoscape.app.internal.tabs.PathsTab;
import ai.vital.cytoscape.app.internal.tabs.PathsTab.ExpansionDirection;
import ai.vital.cytoscape.app.internal.tabs.SearchTab;
import ai.vital.endpoint.EndpointType;
import ai.vital.vitalservice.segment.VitalSegment;

public class VitalAICytoscapePlugin extends Thread implements LoginListener, PropertyChangeListener {

	private final static Logger log = LoggerFactory.getLogger(VitalAICytoscapePlugin.class);
	
	private static VitalAICytoscapePlugin singleton;
	
	private MainTabsPanel tabPane;
	
	private ConnectionTab connectionTab;
	
	private SearchTab searchTab;
	
	private PathsTab pathsTab;
	
	private DatascriptsTab datascriptsTab;
	
	//XXX connect
//	public final static NodeMenuListener nodeMenuListener = new NodeMenuListener();
	
	//2 - search
	private final static int searchTabIndex = 1;
//	
//	private final static int pathsTabIndex = 2;
//	
//	private final static int datascriptsTabIndex = 3;
	
	private CyActivator activator;

	private CyApplicationManager cyApplicationManager;

	private CyNetworkViewManager cyNetworkViewManager;

	private CyNetworkManager cyNetworkManager;

	private CyNetworkFactory cyNetworkFactory;

	private CyNetworkViewFactory cyNetworkViewFactory;

	private CyEventHelper cyEventHelper;

	private CyLayoutAlgorithmManager cyLayoutAlgorithmManager;

	private DialogTaskManager dialogTaskManager;

	private JLabel initLabel = null;
	public VitalAICytoscapePlugin(CyActivator activator, CyApplicationManager cyApplicationManager, CyNetworkFactory nFactory, 
			CyNetworkViewFactory nvFactory, CyNetworkManager nManager, CyNetworkViewManager nvManager, CyEventHelper eHelper, 
			CyLayoutAlgorithmManager cyLayoutAlgorithmManager, DialogTaskManager dialogTaskManager, BundleContext context) {
	
		this.activator = activator;
		
		this.cyApplicationManager = cyApplicationManager;
		
		this.cyNetworkFactory = nFactory;
		
		this.cyNetworkManager = nManager;
		
		this.cyNetworkViewFactory = nvFactory;
		
		this.cyNetworkViewManager = nvManager;
		
		this.cyEventHelper = eHelper;
		
		this.cyLayoutAlgorithmManager = cyLayoutAlgorithmManager;
		
		this.dialogTaskManager = dialogTaskManager;

		//moved out to init it first
		tabPane = new MainTabsPanel();
		tabPane.setPreferredSize(new Dimension(400, 0));
		initLabel = new JLabel("VitalAI plugin is being initialized...");
		tabPane.add(initLabel);
		//   Register it as a service:
		activator.registerServiceX(tabPane, CytoPanelComponent.class, new Properties());
		
		
		//init it in separate thread??
		this.setDaemon(true);
		this.start();
		
	}
	
	
	
	@Override
	public void run() {
		
		Application.init();
		
		Application.get().addLoginListener(this);
		
		initTabPane();
		
		singleton = this;
		
	}



	private void initTabPane() {

		tabPane.remove(initLabel);
		
		initializeConnectionTab();

	}

	
	private void initializeConnectionTab() {
		Application application = Application.get();
	    connectionTab = new ConnectionTab();
	    tabPane.add("Connection", connectionTab);
	    application.addLoginListener(connectionTab);
	}
	
	
	@Override
	public void onLogin() {

		initializeSearchTab();
		
		initializePathsTab();
		
		initializeDatascriptsTab();
		
		tabPane.setSelectedIndex(searchTabIndex);
		
//		tabPane.setSelectedIndex(tabPane.indexOfTabComponent(searchTab));
		
		
		
	}

	@Override
	public void onLogout() {

		pathsTab.getSegmentsPanel().setSegmentsList(new ArrayList<VitalSegment>(0));
		searchTab.getSegmentsPanel().setSegmentsList(new ArrayList<VitalSegment>(0));
		
		if(datascriptsTab != null) {
			tabPane.remove(datascriptsTab);
			datascriptsTab = null;
		}
		tabPane.remove(pathsTab);
		pathsTab = null;
		
		tabPane.remove(searchTab);
		searchTab = null;
		
		
	}
		
	private void initializeSearchTab() {
		
		searchTab = new SearchTab();
		tabPane.addTab("Search", null, searchTab, null);
//	    tabPane.insertTab("Search", null, searchTab, null, searchTabIndex);
	    
	    try {
			searchTab.getSegmentsPanel().setSegmentsList(Application.get().getServiceSegments());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
	    
	}
	
	private void initializePathsTab() {

		pathsTab = new PathsTab();
//		tabPane.insertTab("Paths", null, pathsTab, null, pathsTabIndex);
		tabPane.addTab("Paths", null, pathsTab, null);

	    try {
			pathsTab.getSegmentsPanel().setSegmentsList(Application.get().getServiceSegments());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		
	}
	
	private void initializeDatascriptsTab() {
		
		if(Application.get().getEndpointType() == EndpointType.VITALPRIME) {
			
			datascriptsTab = new DatascriptsTab();
//		tabPane.insertTab("Datascripts", null, datascriptsTab, null, datascriptsTabIndex);
			tabPane.addTab("Datascripts", null, datascriptsTab, null);
			
		}
		
		
	}


	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void propertyChange(PropertyChangeEvent event) {
//		
//		log.debug("PROPERTY CHANGED EVENT :" + event.getPropertyName());
//		String propertyName = event.getPropertyName();
//
//		
//		
//			
//			if(propertyName == Cytoscape.NETWORK_CREATED ||
//					propertyName == Cytoscape.NETWORK_DESTROYED ) {
//		
//				String latestRemoved = "";
//				
//				if(propertyName == Cytoscape.NETWORK_DESTROYED) {
//					
//					latestRemoved = (String) event.getNewValue();
//				}
//				
//				NetworkListPanel.notifyNetworkListPanels(latestRemoved);
//			}
//			
//			
//		
//		if(propertyName == CytoscapeDesktop.NETWORK_VIEW_CREATED ) {
//			
//			log.debug("INSIDE:" + event.getPropertyName() 
//					+" propVal: " + event.getNewValue() + " source: " + event.getSource() );
//			
//			CyNetworkView networkView = (CyNetworkView) event.getNewValue();
//			
//			/* XXX CONNECT!
//			networkView.addNodeContextMenuListener(nodeMenuListener);
//			
//			networkView.addEdgeContextMenuListener(edgeMenuListener);
//			*/
//			
//			registerDropHandling(networkView);
//
//			Utils.applyVisualStyle(networkView.getNetwork());
//			
//		}
//		
//		if(propertyName.equals(Cytoscape.NETWORK_MODIFIED)) {
//			CyNetwork network = (CyNetwork) event.getNewValue();
//			if(Cytoscape.getCurrentNetwork().getIdentifier().equals(network.getIdentifier())) {
//				
//				
//				if( event.getOldValue() != null && event.getOldValue().equals("GO Layout") ) {
///*					
//					//go layout change
//					
//					VisualStyle visualStyle = Cytoscape.getVisualMappingManager().getVisualStyle();
//					
//					NodeAppearanceCalculator nodeAppearanceCalculator = visualStyle.getNodeAppearanceCalculator();
//					
//			        nodeAppearanceCalculator.removeCalculator(VisualPropertyType.NODE_WIDTH);
//			        nodeAppearanceCalculator.removeCalculator(VisualPropertyType.NODE_HEIGHT);
//					
//					//node size
//			        // The first argument to the DiscreteMapping constructor seems
//			        // to no longer define the default. The default appearance
//			        // from the NodeAppearanceCalculator must be used instead:
//			        // a -1.0 means compute size in the standard way.
//			        DiscreteMapping nodeSizeMapping = new DiscreteMapping(new Double(-1.0),
//			                                                              Attributes.type_id,
//			                                                              ObjectMapping.NODE_MAPPING);
//
//			        // discreteMapping.putMapValue(HyperEdgeImpl.ENTITY_TYPE_REGULAR_NODE_VALUE,
//			        //                            new Double(-1.0)); // default
//			        nodeSizeMapping.putMapValue(Utils.virtualConnector_NodeTypeID,
//			                                    new Double(10));
//			        nodeSizeMapping.putMapValue(Utils.virtualSummarization_fact_NodeTypeID,
//			        		new Double(10));
//			        nodeSizeMapping.putMapValue(Utils.virtualSummarization_relation_NodeTypeID,
//			        		new Double(10));
//
//			        // MLC 06/30/07 BEGIN:
//			        //        Calculator nodeSizeCalculator = new GenericNodeUniformSizeCalculator("HyperEdge Node Uniform Size Calculator",
//			        //                                                                             discreteMapping);
//			        // &&&& IS NODE_SIZE CORRECT FOR uniform size?:
//			        Calculator nodeSizeCalculator = new BasicCalculator("HyperEdge Node Uniform Size Calculator",
//			                                                            nodeSizeMapping,
//			                                                            VisualPropertyType.NODE_SIZE);
//			        // MLC 06/30/07 END.
//			        //        Calculator nodeWidthCalculator  = new GenericNodeWidthCalculator("HyperEdge Node Type Width Calculator",
//			        //                                                                         discreteMapping);
//			        //        Calculator nodeHeightCalculator = new GenericNodeHeightCalculator("HyperEdge Node Type Height Calculator",
//			        //                                                                          discreteMapping);
//			        // MLC 01/15/07:
//			        // HEUtils.log("Set nodeSizeCalculator to " + nodeSizeCalculator);
//			        nodeAppearanceCalculator.setCalculator(nodeSizeCalculator);
//			        //        nac.setCalculator(nodeWidthCalculator);
//					
//
//			        visualStyle.setNodeAppearanceCalculator(nodeAppearanceCalculator);
//			        
//			        Cytoscape.getVisualMappingManager().setVisualStyle(visualStyle);
//					Cytoscape.getVisualMappingManager().applyAppearances();
//*/					
//				} else {
//					
//					Utils.applyVisualStyle(network);
//					Cytoscape.getVisualMappingManager().applyAppearances();
//					
//				}
//				
//			}
//			
//			/*
//			if( importanceTab != null) {
//				importanceTab.refreshNetworks(network.getIdentifier());
//			}
//			*/
//		}
//		
//		if(propertyName.equals(Cytoscape.CYTOSCAPE_INITIALIZED)) {
//			
////			initializeApplication();
//			connectionTab.setURL(Application.get().getInitialURL());
//			
//
//			//default visual style
//			VisualStyle createVisualStyle = Utils.createVisualStyle(null);
//			VisualMappingManager visualMappingManager = Cytoscape.getVisualMappingManager();
//			visualMappingManager.getCalculatorCatalog().addVisualStyle(createVisualStyle);
//			visualMappingManager.setVisualStyle(createVisualStyle);
//			
//			
//			
//			VisualStyle vs2 = Utils.createScoredVisualStyle(null);
//			visualMappingManager.getCalculatorCatalog().addVisualStyle(vs2);
//			
//			
//			final String testVal = "dummy";
//
//			LinkedHashMap<String, Class<?>> nodeAttributesMap = Attributes.getNodeAttributesMap();
//			Iterator<String> nodeIterator = nodeAttributesMap.keySet().iterator();
//			CyAttributes nodeAttributes = Cytoscape.getNodeAttributes();
//			while(nodeIterator.hasNext()) {
//				String attributeName = nodeIterator.next();
//				Class<?> class1 = nodeAttributesMap.get(attributeName);
//				
//				if (class1 == String.class) {
//					nodeAttributes.setAttribute(testVal, attributeName, new String());
//				} else if (class1 == Integer.class || class1 == Long.class) {
//					nodeAttributes.setAttribute(testVal, attributeName, new Integer(0));
//				} else if (class1 == Float.class || class1 == Double.class) {
//					nodeAttributes.setAttribute(testVal, attributeName, new Double(0));
//				} else if (class1 == Boolean.class) {
//					nodeAttributes.setAttribute(testVal, attributeName, new Boolean(false));
//				} else if(class1 == List.class) {
//					ArrayList<String> arrayList = new ArrayList<String>();
//					arrayList.add("test1");
//					arrayList.add("test2");
//					nodeAttributes.setListAttribute(testVal, attributeName, arrayList);
//				} else {
//					nodeAttributes.setAttribute(testVal, attributeName, new String());
//				}
//				
//				nodeAttributes.deleteAttribute(testVal, attributeName);
//				
//			}
//			LinkedHashMap<String, Class<?>> edgeAttributesMap = Attributes.getEdgeAttributesMap();
//			Iterator<String> edgeIterator = edgeAttributesMap.keySet().iterator();
//			CyAttributes edgeAttributes = Cytoscape.getEdgeAttributes();
//			while(edgeIterator.hasNext()) {
//				String attributeName = edgeIterator.next();
//				Class<?> class1 = edgeAttributesMap.get(attributeName);
//				
//				if (class1 == String.class) {
//					edgeAttributes.setAttribute(testVal, attributeName, new String());
//				} else if (class1 == Integer.class || class1 == Long.class) {
//					edgeAttributes.setAttribute(testVal, attributeName, new Integer(0));
//				} else if (class1 == Float.class || class1 == Double.class) {
//					edgeAttributes.setAttribute(testVal, attributeName, new Double(0));
//				} else if (class1 == Boolean.class) {
//					edgeAttributes.setAttribute(testVal, attributeName, new Boolean(false));
//				} else if(class1 == List.class) {
//					ArrayList<String> arrayList = new ArrayList<String>();
//					arrayList.add("test1");
//					arrayList.add("test2");
//					nodeAttributes.setListAttribute(testVal, attributeName, arrayList);
//				} else {
//					edgeAttributes.setAttribute(testVal, attributeName, new String());
//				}
//				
//				edgeAttributes.deleteAttribute(testVal, attributeName);
//				
//			}
//			
//			Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, Attributes.nodeTypeURI);
//			
//			TimerTask task = new TimerTask(){
//
//				@Override
//				public void run() {
//					Collection<CyLayoutAlgorithm> allLayouts = CyLayouts.getAllLayouts();
//					Iterator<CyLayoutAlgorithm> iterator = allLayouts.iterator();
//					while(iterator.hasNext()) {
//						CyLayoutAlgorithm next = iterator.next();
//						log.debug(next.getName());
//						log.debug("supports selected only? " + next.supportsSelectedOnly());
//						log.debug("Initial attributes");
//						log.debug(next.getInitialAttributeList());
//						log.debug("Settings:");
//						if(next.getSettings() != null) {
//							HashMap settings = next.getSettings().getProperties();
//							Iterator iterator2 = settings.keySet().iterator();
//							while(iterator2.hasNext()) {
//								Object key = iterator2.next();
//								log.debug("\t" +key + "::" + settings.get(key) );
//							}
//						}
//						if(next.getSettingsPanel()!=null) {
//							next.getSettingsPanel().setVisible(true);
//						}
//						log.debug();
//					}
//					
//				}};
//			
//		
//			
////			Timer t = new Timer(true);
////			t.schedule(task, 1000);
////			
////			yfiles.OrganicLayout oLayout = new yfiles.OrganicLayout();
//		}
//		 
//	}
//	
	
	/*
	public static void registerDropHandling(CyNetworkView networkView) {
		
		//given from Mr. Allan Kuchinsky

		VitalTransferHandler dh = new VitalTransferHandler (); // your TransferHandler class
		
        JDesktopPane dp = Cytoscape.getDesktop().getNetworkViewManager().getDesktopPane();
        dp.setTransferHandler(dh); 
		
//		TransferHandler transferHandler = internalFrameComponent.getTransferHandler();
//		log.debug("Previous transfer handler: " + internalFrameComponent.getTransferHandler());
		
//		internalFrameComponent.setTransferHandler(new MemomicsTransferHandler());		
//		log.debug("New transfer handler: " + internalFrameComponent.getTransferHandler());
		
		log.debug("DropHandler created for : " + networkView.getTitle());
		


	}
	
	*/

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		log.debug("PROPERTY CHANGED: " + evt);
		
	}

	public static CyNetwork getCurrentNetwork() {
		return singleton.cyApplicationManager.getCurrentNetwork();
	}

	public static CyNetworkView getNetworkView(CyNetwork cyNetwork) {
		Collection<CyNetworkView> networkViews = singleton.cyNetworkViewManager.getNetworkViews(cyNetwork);
		for(CyNetworkView nv : networkViews) {
			return nv;
		}
		return null;
	}
	
	public static CyNetworkManager getNetworkManager() {
		if(singleton == null) return null;
		return singleton.cyNetworkManager;
	}
	
	public static CyNetworkFactory getNetworkFactory() {
		return singleton.cyNetworkFactory;
	}
	
	public static CyNetworkViewFactory getNetworkViewFactory() {
		return singleton.cyNetworkViewFactory;
	}
	
	public static CyNetworkViewManager getNetworkViewManager() {
		return singleton.cyNetworkViewManager;
	}

	public static CyNetworkView getCurrentNetworkView() {
		return singleton.cyApplicationManager.getCurrentNetworkView();
	}

	public static CyEventHelper getEventHelper() {
		return singleton.cyEventHelper;
	}
	
	public static CyLayoutAlgorithmManager getLayoutAlgorithmManager() {
		return singleton.cyLayoutAlgorithmManager;
	}
	
	public static DialogTaskManager getDialogTaskManager() {
		if(singleton == null) return null;
		return singleton.dialogTaskManager;
	}



	public static ExpansionDirection getExpansionDirection() {
		if(singleton == null || singleton.pathsTab == null) return null;
		return singleton.pathsTab.getExpansionDirection();
	}
}
