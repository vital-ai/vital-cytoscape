package ai.vital.cytoscape.app.internal;

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
import org.cytoscape.view.vizmap.VisualMappingFunctionFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyleFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

import ai.vital.cytoscape.app.internal.app.NetworkListener;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.cytoscape.app.internal.tasks.ExpandNodesViewTaskFactory;

public class CyActivator extends AbstractCyActivator {

	private BundleContext context = null;
	
	@Override
	public void start(BundleContext context) throws Exception {
		
		this.context = context;
		
		CyApplicationManager cyApplicationManager = getService(context, CyApplicationManager.class);
		
		CyNetworkFactory nFactory = getService(context, CyNetworkFactory.class);
		
		CyNetworkViewFactory nvFactory = getService(context, CyNetworkViewFactory.class);
		
		CyNetworkManager nManager = getService(context, CyNetworkManager.class); 
		
		CyNetworkViewManager nvManager = getService(context, CyNetworkViewManager.class);
		
		MenuAction action = new MenuAction(cyApplicationManager, "Hello World App");
		
		Properties properties = new Properties();
		
		registerAllServices(context, action, properties);
		
		System.out.println("Starting plugin...");
		
		NetworkListener nl = new NetworkListener();
		
		registerService(context, nl, NetworkAddedListener.class, new Properties());
		registerService(context, nl, NetworkDestroyedListener.class, new Properties());
		
		
		CyEventHelper helper = getService(context, CyEventHelper.class);
		
		// Register myNodeViewTaskFactory as a service in CyActivator
		Properties myNodeViewTaskFactoryProps = new Properties();
		myNodeViewTaskFactoryProps.setProperty("title","Expand Nodes with Vital AI Service");
		registerService(context, new ExpandNodesViewTaskFactory(), NodeViewTaskFactory.class, myNodeViewTaskFactoryProps);
		
		CyLayoutAlgorithmManager cyLayoutAlgorithmManager = getService(context, CyLayoutAlgorithmManager.class);
		
		for(CyLayoutAlgorithm l : cyLayoutAlgorithmManager.getAllLayouts()) {
			System.out.println(l.getName() + " " + l.getSupportsSelectedOnly());
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
