package ai.vital.cytoscape.app.internal;

import java.util.Collection;
import java.util.Properties;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.event.CyEventHelper;
import org.cytoscape.model.CyNetworkFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.model.events.NetworkDestroyedListener;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.view.layout.CyLayoutAlgorithm;
import org.cytoscape.view.layout.CyLayoutAlgorithmManager;
import org.cytoscape.view.model.CyNetworkViewFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.model.events.NetworkViewAddedEvent;
import org.cytoscape.view.model.events.NetworkViewAddedListener;
import org.cytoscape.view.model.events.NetworkViewDestroyedListener;
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.NetworkListener;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.cytoscape.app.internal.tasks.ExpandNodesViewTaskFactory;

public class CyActivator extends AbstractCyActivator {

	private BundleContext context = null;
	
	private final static Logger log = LoggerFactory.getLogger(CyActivator.class);
	
	@Override
	public void start(BundleContext context) throws Exception {
		
		this.context = context;
		
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		
		CyNetworkFactory nFactory = getService(context, CyNetworkFactory.class);
		
		CyNetworkViewFactory nvFactory = getService(context, CyNetworkViewFactory.class);
		
		CyNetworkManager nManager = getService(context, CyNetworkManager.class); 
		
		CyNetworkViewManager nvManager = getService(context, CyNetworkViewManager.class);
		
		MenuAction action = new MenuAction(cyApplicationManager, "Vital AI Graph Visualization");
		
		Properties properties = new Properties();
		
		registerAllServices(context, action, properties);
		
		log.info("Starting Vital AI plugin...");
		
		NetworkListener nl = new NetworkListener();
		
		registerService(context, nl, NetworkAddedListener.class, new Properties());
		registerService(context, nl, NetworkDestroyedListener.class, new Properties());
		
		registerService(context, nl, NetworkViewAddedListener.class, new Properties());
		registerService(context, nl, NetworkViewDestroyedListener.class, new Properties());
		
		
		CyEventHelper helper = getService(context, CyEventHelper.class);
		
		// Register myNodeViewTaskFactory as a service in CyActivator
		Properties myNodeViewTaskFactoryProps = new Properties();
		myNodeViewTaskFactoryProps.setProperty("title","Expand Nodes"); // with Vital AI Service
		myNodeViewTaskFactoryProps.put("preferredMenu", "Vital AI");
		registerService(context, new ExpandNodesViewTaskFactory(), NodeViewTaskFactory.class, myNodeViewTaskFactoryProps);
		
		CyLayoutAlgorithmManager cyLayoutAlgorithmManager = getService(context, CyLayoutAlgorithmManager.class);
		
		Collection<CyLayoutAlgorithm> allLayouts = cyLayoutAlgorithmManager.getAllLayouts();
		log.info("Supported layouts [" + allLayouts.size() + "]");
		for(CyLayoutAlgorithm l : allLayouts) {
			log.info(l.getName() + " " + l.getSupportsSelectedOnly());
		}
		
		DialogTaskManager dialogTaskManager = getService(context, DialogTaskManager.class);

		
		VisualMappingManager vmmServiceRef = getService(context, VisualMappingManager.class);

		VisualStyleFactory visualStyleFactoryServiceRef = getService(context, VisualStyleFactory.class);
		                 
		VisualMappingFunctionFactory vmfFactoryC = getService(context, VisualMappingFunctionFactory.class, "(mapping.type=continuous)");
		VisualMappingFunctionFactory vmfFactoryD = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=discrete)");
		VisualMappingFunctionFactory vmfFactoryP = getService(context,VisualMappingFunctionFactory.class, "(mapping.type=passthrough)");
		
		VisualStyleUtils.init(vmmServiceRef, visualStyleFactoryServiceRef, vmfFactoryC, vmfFactoryD, vmfFactoryP);
		
		
		new VitalAICytoscapePlugin(this, cyApplicationManager, nFactory, nvFactory, nManager, nvManager, helper, cyLayoutAlgorithmManager, dialogTaskManager, context);

	}
	
	public void registerServiceX(Object service, Class<?> serviceClass, Properties props) {
		registerService(context, service, serviceClass, props);
	}
	
}
