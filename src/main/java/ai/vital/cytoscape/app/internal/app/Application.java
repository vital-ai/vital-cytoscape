package ai.vital.cytoscape.app.internal.app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ai.vital.cytoscape.app.internal.tabs.PathsTab.ExpansionDirection;
import ai.vital.domain.Datascript;
import ai.vital.domain.Job;
import ai.vital.endpoint.EndpointType;
import ai.vital.lucene.model.LuceneSegment;
import ai.vital.prime.service.VitalServicePrime;
import ai.vital.prime.service.config.VitalServicePrimeConfig;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.VitalStatus;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.factory.Factory;
import ai.vital.vitalservice.model.App;
import ai.vital.vitalservice.model.Customer;
import ai.vital.vitalservice.query.QueryPathElement;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalPropertyConstraint;
import ai.vital.vitalservice.query.VitalPropertyConstraint.Comparator;
import ai.vital.vitalservice.query.VitalQueryContainer;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.datatype.VitalURI;
import ai.vital.vitalsigns.global.GlobalHashTable;
import ai.vital.vitalsigns.meta.GraphSetsRegistry;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.PathElement;
import ai.vital.vitalsigns.model.URIPropertyValue;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;

public class Application {

	private static Application singleton;
	
	private List<LoginListener> loginListeners = new ArrayList<LoginListener>();

	private final static Logger log = LoggerFactory.getLogger(Application.class);

	//managed by connection tab
	private VitalService vitalService = null;

	private Config serviceConfig = null;
	
	private EndpointType endpointType = null;
	
	private String primeURL = null;
	
	public boolean addLoginListener(LoginListener l) {
		if(!loginListeners.contains(l)) {
			loginListeners.add(l);
			return true;
		}
		return false;
	}
	
	public boolean removeLoginListener(LoginListener l) {
		if(loginListeners.contains(l)) {
			loginListeners.remove(l);
			return true;
		}
		return false;
	}
	
	private Application() {
		
	}
	
	public static Application get() {
		if(singleton == null) throw new RuntimeException("Vital AI application not ready.");
		return singleton;
	}
	
	public static void init() {
		if(singleton != null) return;
		singleton = new Application();
		
		String vitalHome = System.getenv("VITAL_HOME");
		log.info("Checking vital singleton...");
		VitalSigns vs = VitalSigns.get();
		log.info("$VITAL_HOME: " + vitalHome);
//		o("Singleton obtained, registering vital domain ontology...");
		File domainJarsDir = new File(vitalHome, "domain-jar");
		log.info("Domain jars path: " + domainJarsDir.getAbsolutePath() + " dir ? " + domainJarsDir.isDirectory());
		//vs.registerOntology(new VitalOntology());
		
		if(domainJarsDir.isDirectory()) {
			
			log.info("Domain files count: " +domainJarsDir.listFiles().length);
			for(File f : domainJarsDir.listFiles()) {
				
				if(!f.getName().endsWith(".jar")) {
					continue;
				}
				
				log.info("Registering domain ontology: " + f.getName());
				try {
					vs.registerOntology(f.toURI().toURL());
				} catch (MalformedURLException e) {}
				
			}
		} else {
			log.warn("WARN - $VITAL_HOME/domain-jar/ directory does not exist");
		}
	
		
		File serviceCfgFile = new File(vitalHome, "vital-config/vitalservice/vitalservice.config");
		
		if(!serviceCfgFile.exists()) {
			log.error("Service config file not found: " + serviceCfgFile.getAbsolutePath());
			singleton.serviceConfig = ConfigFactory.empty();
		} else {
			singleton.serviceConfig = ConfigFactory.parseFile(serviceCfgFile);
		}

		singleton.endpointType = EndpointType.LUCENEMEMORY;
		
		try {
			singleton.endpointType = EndpointType.fromString(singleton.serviceConfig.getString("type"));
		} catch(Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		
		if(singleton.endpointType == EndpointType.VITALPRIME) {
			
			String primeURL = "http://127.0.0.1:9080/java";
			
			//set initial url
			try {
				primeURL = singleton.serviceConfig.getString("VitalPrime.endpointURL");
			} catch(Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
			
			singleton.primeURL = primeURL;
			

			
		} else {
			
			//just create the service
//			service = Factory.getVitalService();
			
		}
		
	}

	public void login(String username, String password, String url) throws Exception {

		log.debug("Logging in...");
		
		
		if(endpointType == EndpointType.VITALPRIME) {
			
			String customerID = serviceConfig.getString("customerID");
			String appID = serviceConfig.getString("appID");
			
			App app = new App();
			app.setID(appID);
			app.setCustomerID(customerID);
			
			Customer customer = new Customer();
			customer.setID(customerID);
			
			new URL(url);
			
//			VitalServicePrimeConfigCreator creator = new VitalServicePrimeConfigCreator();
			VitalServicePrimeConfig cfg = new VitalServicePrimeConfig();
			
			cfg.setApp(app);
			cfg.setCustomer(customer);
			cfg.endpointURL = url;
			
			VitalService _service = Factory.createVitalService(cfg);
			
			VitalStatus status = _service.ping();
			
			if(status.getStatus() != VitalStatus.Status.ok) {
				throw new Exception("Ping failed: " + status.getMessage());
			}
			
			vitalService = _service;
			
//			creator.setCustomConfigProperties(cfg, serviceConfig);
			
			
		} else {
			
			//just create the service with factory
			
			vitalService = Factory.getVitalService();
			
		}
		
		/*
		VitalService.setEndpoint(url);
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("accountType", "local");
		params.put("segment", "modernist");
		params.put("username", username);
		params.put("password", password);
		
		ResultSet rs = VitalService.getInstance().callFunction("AuthenticateUser.groovy", params);
		if(!rs.isOk()) throw new Exception(rs.getErrorMessage());
		*/
		//successfully authenticated?
		
		/*
		o("Testing expansion...");
		
		ResultList expanded = getConnections("http://uri.vital.ai/wordnet/NounSynsetNode_1396611318625_1016512");
		
		for(ResultElement el : expanded.getResults()) {
			log.debug(el.getGraphObject());
		}
		*/
		
		notifyListenersOfLoginEvent();
		
	}

	public static interface LoginListener {
		
		public void onLogin();
		
		public void onLogout();
		
	}

	
	private void notifyListenersOfLoginEvent() {
		for( Iterator<LoginListener> iterator = loginListeners.iterator(); iterator.hasNext(); ) {
			iterator.next().onLogin();
		}
		
	}
	private void notifyListenersOfLogoutEvent() {
		for( Iterator<LoginListener> iterator = loginListeners.iterator(); iterator.hasNext(); ) {
			iterator.next().onLogout();
		}
		
	}
	
	public void logout() {

		this.vitalService = null;
		notifyListenersOfLogoutEvent();
		
	}

	public ResultList search(VitalSelectQuery sq) throws Exception {
		return vitalService.selectQuery(sq);
	}

	public boolean isExpandUsingSynonyms() {
		return false;
	}

	public ResultList getConnections(String uri_str, String typeURI) {

		//expansion
		/*
		GraphObject graphObject = null;
		try {
			graphObject = VitalService.getInstance().getGraphObject(uri_str);
		} catch (VitalServiceException e) {
			e.printStackTrace();
		}
		if(graphObject == null) return new ResultSet();
		*/
		
		ExpansionDirection direction = VitalAICytoscapePlugin.getExpansionDirection();
		
		
		ResultList rs = new ResultList();
		List<ResultElement> li = new ArrayList<ResultElement>();
		rs.setResults(li);
		List<VitalSegment> serviceSegments = new ArrayList<VitalSegment>();
		try {
			serviceSegments = getServiceSegments();
		} catch (Exception e1) {
		}
		
		
		for(Entry<String, LuceneSegment> en : VitalSigns.get().getNs2Segment().entrySet()) {
			
			String domainSegment = en.getKey();
			
			LuceneSegment value = en.getValue();
			
			//select all edges
			VitalSelectQuery sq = new VitalSelectQuery();
			sq.setLimit(1000);
			sq.setOffset(0);
			sq.setType(VitalQueryContainer.Type.or);
			
			if(direction == ExpansionDirection.Both || direction == ExpansionDirection.Outgoing) {
				sq.getComponents().add(new VitalPropertyConstraint(VitalCoreOntology.hasEdgeSource.getURI(), new URIPropertyValue(uri_str), Comparator.EQ));
			}
			if(direction == ExpansionDirection.Both || direction == ExpansionDirection.Incoming) {
				sq.getComponents().add(new VitalPropertyConstraint(VitalCoreOntology.hasEdgeDestination.getURI(), new URIPropertyValue(uri_str), Comparator.EQ));
			}
			
			ResultList rl = VitalSigns.get().doSelectQuery(domainSegment, sq);
			for(ResultElement r : rl.getResults()) {
				GraphObject _e= r.getGraphObject();
				if(!(_e instanceof VITAL_Edge)) continue;
				VITAL_Edge e = (VITAL_Edge) _e;
				GraphObject otherEndpoint = null;
				if(!e.getSourceURI().equals(uri_str)) {
					otherEndpoint = value.get(e.getSourceURI());
				} 
				if(!e.getDestinationURI().equals(uri_str)) {
					otherEndpoint = value.get(e.getDestinationURI());
				}
				
				if(otherEndpoint != null) {
					li.add(new ResultElement(e, 1d));
					li.add(new ResultElement(otherEndpoint, 1d));
				}
			}
			
		}
		
		if(serviceSegments.size() > 0){
		
		try {
			
			Class<? extends GraphObject> gClass = VitalSigns.get().getGroovyClass(typeURI);
			
			if(gClass == null) {
				log.warn("No groovy class for URI: " + typeURI);
				return rs;
			}
			
			//paths list 
			List<List<PathElement>> vpaths = GraphSetsRegistry.get().getPaths(gClass);
//			vpaths.addAll(GraphSetsRegistry.get().getPaths(gClass, false));
			
			if(vpaths.size() < 1) {
				log.warn("Default taxonomy paths list for class: " + gClass.getCanonicalName() + " is empty - cannot expand the object");
				return rs;
			}
				
			List<List<QueryPathElement>> paths = new ArrayList<List<QueryPathElement>>(vpaths.size());
			
			//convert paths into query path elements
			for(List<PathElement> path : vpaths) {
				
				List<QueryPathElement> qpath = new ArrayList<QueryPathElement>(path.size());
				
				for(PathElement pe : path) {
					Class<? extends VITAL_Edge> edgeClass = VitalSigns.get().getGroovyClass(pe.getEdgeTypeURI());
					QueryPathElement qpe = new QueryPathElement(edgeClass, null, pe.isReversed() ? QueryPathElement.Direction.reverse : QueryPathElement.Direction.forward, QueryPathElement.CollectEdges.yes, QueryPathElement.CollectDestObjects.yes);
					qpath.add(qpe);
				}
				
				paths.add(qpath);
				
			}
			
			
			GraphObject graphObjectExpanded = vitalService.getExpandedInSegmentsWithPaths(VitalURI.withString(uri_str), serviceSegments, paths);
//			o("Expanded: " + graphObjectExpanded);
			if(graphObjectExpanded instanceof VITAL_Node) {
				VITAL_Node n = (VITAL_Node) graphObjectExpanded;
				List<VITAL_Edge> outgoingEdges = n.getOutgoingEdges();
//				o("outgoing edges: " + outgoingEdges.size());
				List<GraphObject> l = new ArrayList<GraphObject>();
				l.addAll(outgoingEdges);
				
				for(VITAL_Edge e : outgoingEdges) {
					GraphObject o = GlobalHashTable.get().get(e.getDestinationURI());
					if(o != null) {
						l.add(o);
					}
				}
				
				List<VITAL_Edge> incomingE = ((VITAL_Node) graphObjectExpanded).getIncomingEdges();
//				o("incoming edges: " + outgoingEdges.size());
				l.addAll(incomingE);
				
				for(VITAL_Edge e : incomingE) {
					GraphObject o = GlobalHashTable.get().get(e.getSourceURI());
					if(o != null) {
						l.add(o);
					}
				}
				
//				rs.putGraphObjects(l);
				for(GraphObject g : l) {
					li.add(new ResultElement(g, 1d));
				}
				
			}
//			o("Results: " + li.size());
		} catch (Exception e) {
			log.error(e.getLocalizedMessage(), e);
		}
		}
		
		return rs;
	}

	public VitalSegment getWordnetSegment() {
		VitalSegment s1 = new VitalSegment();
		s1.setId("wordnet");
		return s1;
	}

	public List<VitalSegment> getServiceSegments() throws VitalServiceException, VitalServiceUnimplementedException {
		return vitalService.listSegments();
	}

	public VitalService getVitalService() {
		return vitalService;
	}

	public void setVitalService(VitalService vitalService) {
		this.vitalService = vitalService;
	}

	public EndpointType getEndpointType() {
		return endpointType;
	}

	public String getPrimeURL() {
		return primeURL;
	}

	public List<Datascript> getDatascripts() throws Exception {
		if(endpointType == EndpointType.VITALPRIME) {
			VitalServicePrime primeService = (VitalServicePrime) vitalService;
			List<GraphObject> scripts = new ArrayList<GraphObject>();
			List<GraphObject> commonScripts = primeService.listDatascripts("commons/scripts/*", false);
			scripts.addAll(commonScripts);
			List<GraphObject> appScripts = primeService.listDatascripts("*", false);
			scripts.addAll(appScripts);
			
			List<Datascript> scriptsRes = new ArrayList<Datascript>();
			//skip non-callable jobs
			for( Iterator<GraphObject> iterator = scripts.iterator(); iterator.hasNext(); ) {
				
				GraphObject g = iterator.next();
				if(g instanceof Job) {
					Boolean callable = (Boolean) g.getProperty("callable");
					if(callable == null || callable.equals(Boolean.FALSE)) {
						iterator.remove();
					}
				}
				
				if(g instanceof Datascript) {
					scriptsRes.add((Datascript) g);
				}
				
			}
			
			return scriptsRes;
		} else {
			throw new Exception("Expected vitalprime endpoint type");
		}
	}

	public static void initForTests() {

		singleton = new Application();
		singleton.vitalService = Factory.getVitalService();
		
		String vitalHome = System.getenv("VITAL_HOME");
		log.info("$VITAL_HOME: " + vitalHome);
	
		File serviceCfgFile = new File(vitalHome, "vital-config/vitalservice/vitalservice.config");
		
		if(!serviceCfgFile.exists()) {
			log.error("Service config file not found: " + serviceCfgFile.getAbsolutePath());
			singleton.serviceConfig = ConfigFactory.empty();
		} else {
			singleton.serviceConfig = ConfigFactory.parseFile(serviceCfgFile);
		}
		
		singleton.endpointType = singleton.vitalService.getEndpointType();
		
		if(singleton.vitalService instanceof VitalServicePrime) {
			
			String primeURL = "http://127.0.0.1:9080/java";
			
			//set initial url
			try {
				primeURL = singleton.serviceConfig.getString("VitalPrime.endpointURL");
			} catch(Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
			
			singleton.primeURL = primeURL;
			
		}
		

		
	}

	public ResultList executeDatascript(String path,
			Map<String, Object> runParamsF) throws Exception {

		return vitalService.callFunction(path, runParamsF);
		
	}

	public static class HierarchyNode {
		
		public Class<? extends GraphObject> cls;
		
		public String URI;
		
		public List<HierarchyNode> children = new ArrayList<HierarchyNode>();
		
		@Override
		public String toString() {
			return cls != null ? cls.getSimpleName() : "(error)";
		}
		
	}
	
	
	HierarchyNode root = new HierarchyNode();
	
	public HierarchyNode getClassHierarchy(Class<? extends GraphObject> rootClass) throws Exception {

		//prefetch the entire tree

		if(root.URI == null) {
			
			synchronized(root) {
				
				if(root.URI == null) {
					
					initializeHierarchy();
					
				}
				
			}
			
		}
		
		//seek
		HierarchyNode m = findRoot(root, rootClass);
		
		if(m == null) throw new Exception("Class not found in hierarchy: " + rootClass.getCanonicalName());
		
		return m;
		
	}

	private HierarchyNode findRoot(HierarchyNode root2,
			Class<? extends GraphObject> toFind) {
		
		if(root2.cls.equals(toFind)) {
			return root2;
		}
		
		for(HierarchyNode c : root2.children) {
			
			HierarchyNode hit = findRoot(c, toFind);
			if(hit != null) return hit;
			
		}
		
		return null;
	}

	private void initializeHierarchy() throws Exception {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("class", GraphObject.class.getCanonicalName());
		ResultList rl = vitalService.callFunction("commons/scripts/GetClassHierarchy", params);
		
		if(rl.getStatus().getStatus() != VitalStatus.Status.ok) throw new Exception("Vital service error: " + rl.getStatus().getMessage());
		
		Map<String, VITAL_Node> nodes = new HashMap<String, VITAL_Node>();
		
		Map<String, List<String>> src2Dest= new HashMap<String, List<String>>();
		
		for(ResultElement r : rl.getResults()) {
			
			GraphObject g = r.getGraphObject();
			
			if(g instanceof VITAL_Node) {
				
				nodes.put(g.getURI(), (VITAL_Node) g);
				
			} else if(g instanceof VITAL_Edge) {
				
				VITAL_Edge e = (VITAL_Edge) g;
				
				List<String> l = src2Dest.get(e.getSourceURI());
				
				if(l == null) {
					l = new ArrayList<String>();
					src2Dest.put(e.getSourceURI(), l);
				}
				
				l.add(e.getDestinationURI());
				
				
			}
			
		}
		
		VITAL_Node rootNode = nodes.get(VitalCoreOntology.NS + GraphObject.class.getSimpleName());
		
		if(rootNode == null) throw new Exception("No hierarchy root node found in results list");
		
		root.cls = GraphObject.class;
		root.URI = rootNode.getURI();
		
		processHierarchy(root, nodes, src2Dest);
		
	}

	@SuppressWarnings("unchecked")
	private void processHierarchy(HierarchyNode parent,
			Map<String, VITAL_Node> nodes, Map<String, List<String>> src2Dest) throws Exception {
		
		List<String> uris = src2Dest.get(parent.URI);
		
		if(uris == null) return;
		
		for(String u : uris) {
			
			VITAL_Node n = nodes.get(u);
			
			if(n == null) throw new Exception("No class node with URI: " + u);
			
			HierarchyNode child = new HierarchyNode();
			child.URI = u;
			
			String gname = (String) n.getProperty("name");
			
			Class<? extends GraphObject> cls = (Class<? extends GraphObject>) VitalSigns.get().getGroovyClass(u);//Class.forName((String) n.getProperty("name"));
			if(cls == null) throw new Exception("Class not found: " + gname + " uri: " + u);
			child.cls = cls;
			
			parent.children.add(child);
			
			processHierarchy(child, nodes, src2Dest);
			
		}
		
	}

	

	/*
	public DiscreteMapping getCustomNodeColorMapping(CyNetwork network) {

		Color defColor = new Color(195,255,220);
		
		DiscreteMapping disMapping = new DiscreteMapping(defColor,
				ObjectMapping.NODE_MAPPING);
		
		disMapping.setControllingAttributeName(Attributes.nodeTypeURI, network, false);
		
		disMapping.putMapValue(VitalSigns.get().getRDFClass(AdjectiveSynsetNode.class), new Color(212,254,212));
		
		disMapping.putMapValue(VitalSigns.get().getRDFClass(AdverbSynsetNode.class), new Color(255,255,153));
		
		disMapping.putMapValue(VitalSigns.get().getRDFClass(NounSynsetNode.class), new Color(255,197,197));
		
		disMapping.putMapValue(VitalSigns.get().getRDFClass(VerbSynsetNode.class), new Color(255,168,0));
		
		return disMapping;
	
		
	}
	*/

}
