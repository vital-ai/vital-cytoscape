package ai.vital.cytoscape.app.internal.app;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collection;
import java.util.Properties;

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

import ai.vital.cytoscape.app.internal.CyActivator;
import ai.vital.cytoscape.app.internal.app.Application.LoginListener;
import ai.vital.cytoscape.app.internal.tabs.ConnectionTab;
import ai.vital.cytoscape.app.internal.tabs.MainTabsPanel;
import ai.vital.cytoscape.app.internal.tabs.SearchTab;

public class VitalAICytoscapePlugin implements LoginListener, PropertyChangeListener {

	private static VitalAICytoscapePlugin singleton;
	
	private MainTabsPanel tabPane;
	
	private ConnectionTab connectionTab;
	
	private SearchTab searchTab;
	
	//XXX connect
//	public final static NodeMenuListener nodeMenuListener = new NodeMenuListener();
	
	//2 - search
	private final static int searchTabIndex = 1;
	
	private CyActivator activator;

	private CyApplicationManager cyApplicationManager;

	private CyNetworkViewManager cyNetworkViewManager;

	private CyNetworkManager cyNetworkManager;

	private CyNetworkFactory cyNetworkFactory;

	private CyNetworkViewFactory cyNetworkViewFactory;

	private CyEventHelper cyEventHelper;

	private CyLayoutAlgorithmManager cyLayoutAlgorithmManager;

	private DialogTaskManager dialogTaskManager;

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
		
		Application.init();
		
		Application.get().addLoginListener(this);
		
		initTabPane();
		
		singleton = this;
		
	}
	
	private void initTabPane() {

		tabPane = new MainTabsPanel();
		
		initializeConnectionTab();

		//   Register it as a service:
		activator.registerServiceX(tabPane, CytoPanelComponent.class, new Properties());
	}

	
	private void initializeConnectionTab() {
		Application application = Application.get();
	    connectionTab = new ConnectionTab();
	    tabPane.add("Connection", connectionTab);
	    application.addLoginListener(connectionTab);
	}
	
	
	static void o(String m) { System.out.println(m); }

	@Override
	public void onLogin() {

		initializeSearchTab();
		
	}

	@Override
	public void onLogout() {

		tabPane.remove(searchTab);
		
	}
		
	private void initializeSearchTab() {
		
		searchTab = new SearchTab();
	    tabPane.insertTab("Search", null, searchTab, null, searchTabIndex);
	    
	}
	
//	@SuppressWarnings("unchecked")
//	@Override
//	public void propertyChange(PropertyChangeEvent event) {
//		
//		System.out.println("PROPERTY CHANGED EVENT :" + event.getPropertyName());
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
//			System.out.println("INSIDE:" + event.getPropertyName() 
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
//						System.out.println(next.getName());
//						System.out.println("supports selected only? " + next.supportsSelectedOnly());
//						System.out.println("Initial attributes");
//						System.out.println(next.getInitialAttributeList());
//						System.out.println("Settings:");
//						if(next.getSettings() != null) {
//							HashMap settings = next.getSettings().getProperties();
//							Iterator iterator2 = settings.keySet().iterator();
//							while(iterator2.hasNext()) {
//								Object key = iterator2.next();
//								System.out.println("\t" +key + "::" + settings.get(key) );
//							}
//						}
//						if(next.getSettingsPanel()!=null) {
//							next.getSettingsPanel().setVisible(true);
//						}
//						System.out.println();
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
//		System.out.println("Previous transfer handler: " + internalFrameComponent.getTransferHandler());
		
//		internalFrameComponent.setTransferHandler(new MemomicsTransferHandler());		
//		System.out.println("New transfer handler: " + internalFrameComponent.getTransferHandler());
		
		System.out.println("DropHandler created for : " + networkView.getTitle());
		


	}
	
	*/

	@Override
	public void propertyChange(PropertyChangeEvent evt) {

		o("PROPERTY CHANGED: " + evt);
		
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
		return singleton.dialogTaskManager;
	}
}
