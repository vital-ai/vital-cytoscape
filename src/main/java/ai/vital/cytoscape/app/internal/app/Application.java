package ai.vital.cytoscape.app.internal.app;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.queries.Queries;
import ai.vital.cytoscape.app.internal.tabs.PathsTab.ExpansionDirection;
import ai.vital.domain.Datascript;
import ai.vital.domain.Job;
import ai.vital.endpoint.EndpointType;
import ai.vital.lucene.model.LuceneSegment;
import ai.vital.prime.service.VitalServicePrime;
import ai.vital.prime.service.config.VitalServicePrimeConfig;
import ai.vital.property.URIProperty;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.VitalStatus;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalservice.model.App;
import ai.vital.vitalservice.model.Organization;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.Connector;
import ai.vital.vitalservice.query.Destination;
import ai.vital.vitalservice.query.GraphElement;
import ai.vital.vitalservice.query.QueryContainerType;
import ai.vital.vitalservice.query.Source;
import ai.vital.vitalservice.query.VitalGraphArcContainer;
import ai.vital.vitalservice.query.VitalGraphArcElement;
import ai.vital.vitalservice.query.VitalGraphCriteriaContainer;
import ai.vital.vitalservice.query.VitalGraphQueryTypeCriterion;
import ai.vital.vitalservice.query.VitalPathQuery;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.meta.PathElement;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Edge_PropertiesHelper;
import ai.vital.vitalsigns.model.VITAL_Node;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

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
		VitalSigns vs = null;
		try {
			
			if(vitalHome == null || vitalHome.isEmpty()) {
				throw new Exception("VITAL_HOME environment variable not set");
			}
		
			File licenseFile = new File(vitalHome, "vital-license/vital-license.lic");
			if(!licenseFile.exists()) throw new Exception("Vital license file not found, path: " + licenseFile.getAbsolutePath());
			
			vs = VitalSigns.get();
			
		} catch(Throwable e) {
			JOptionPane.showMessageDialog(null, e.getMessage(), "Vital AI initialization error", JOptionPane.ERROR_MESSAGE);
			throw new RuntimeException(e);
		}
		
		log.info("$VITAL_HOME: " + vitalHome);
//		o("Singleton obtained, registering vital domain ontology...");
		File domainJarsDir = new File(vitalHome, "domain-groovy-jar");
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
				} catch (Exception e) {
					log.error(e.getLocalizedMessage());
				}
				
			}
		} else {
			log.warn("WARN - $VITAL_HOME/domain-jar/ directory does not exist");
		}
	
		
		/*
		File serviceCfgFile = VitalServiceFactory.getConfigFile();
		
		if(!serviceCfgFile.exists()) {
			log.error("Service config file not found: " + serviceCfgFile.getAbsolutePath());
			singleton.serviceConfig = ConfigFactory.empty();
		} else {
			singleton.serviceConfig = ConfigFactory.parseFile(serviceCfgFile);
		}
		
		List<String> profiles = VitalServiceFactory.getAvailableProfiles();

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
		*/
		
	}

	public void login(String username, String password, String url) throws Exception {

		log.debug("Logging in...");
		
		VitalServiceFactory.closeVitalService();
		
		if(endpointType == EndpointType.VITALPRIME) {
			
			
			String customerID = serviceConfig.getString("customerID");
			String appID = serviceConfig.getString("appID");
			
			App app = new App();
			app.setID(appID);
			app.setOrganizationID(customerID);
			
			Organization organization = new Organization();
			organization.setID(customerID);
			
			new URL(url);
			
//			VitalServicePrimeConfigCreator creator = new VitalServicePrimeConfigCreator();
			VitalServicePrimeConfig cfg = new VitalServicePrimeConfig();
			
			cfg.setApp(app);
			cfg.setOrganization(organization);
			cfg.endpointURL = url;
			
			VitalService _service = VitalServiceFactory.createVitalService(cfg);
			
			VitalStatus status = _service.ping();
			
			if(status.getStatus() != VitalStatus.Status.ok) {
				throw new Exception("Ping failed: " + status.getMessage());
			}
			
			vitalService = _service;
			
//			creator.setCustomConfigProperties(cfg, serviceConfig);
			
			
		} else {
			
			//just create the service with factory
			
			vitalService = VitalServiceFactory.getVitalService();
			
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
		return vitalService.query(sq);
	}

	public boolean isExpandUsingSynonyms() {
		return false;
	}

	@SuppressWarnings("unchecked")
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
		
		if(direction == null) direction = ExpansionDirection.Outgoing;
		
		Integer depth = VitalAICytoscapePlugin.getDepth();
		if(depth == null) depth = 1;
		
		ResultList rs = new ResultList();
		List<ResultElement> li = new ArrayList<ResultElement>();
		rs.setResults(li);

		Class<? extends GraphObject> gClass = VitalSigns.get().getGroovyClass(typeURI);
		
		
		List<List<PathElement>> fPaths = null;
		List<List<PathElement>> rPaths = null;
		try {
			if(direction == ExpansionDirection.Both || direction == ExpansionDirection.Outgoing) {
				fPaths = VitalSigns.get().getClassesRegistry().getPaths(gClass, true);
			} else {
				fPaths = new ArrayList<List<PathElement>>();
			}
			
			if(direction == ExpansionDirection.Both || direction == ExpansionDirection.Incoming) {
				rPaths = VitalSigns.get().getClassesRegistry().getPaths(gClass, false);
			} else {
				rPaths = new ArrayList<List<PathElement>>();
			}
		} catch (Exception e2) {
			log.error(e2.getLocalizedMessage(), e2);
			return rs;
		}
		
		List<Class<? extends VITAL_Edge>> fClasses = new ArrayList<Class<? extends VITAL_Edge>>();
		List<Class<? extends VITAL_Edge>> rClasses = new ArrayList<Class<? extends VITAL_Edge>>();
		
		for(List<PathElement> p : fPaths) {
			PathElement pe = p.get(0);
			if(!pe.isHyperedge()) fClasses.add((Class<? extends VITAL_Edge>) pe.getEdgeClass());
		}
		
		for(List<PathElement> p : rPaths) {
			PathElement pe = p.get(0);
			if(!pe.isHyperedge()) rClasses.add((Class<? extends VITAL_Edge>) pe.getEdgeClass());
		}
		
		if(fClasses.size() == 0 && rClasses.size() == 0) {
			return rs;
		}

		List<VitalSegment> serviceSegments = new ArrayList<VitalSegment>();
		try {
			serviceSegments = getServiceSegments();
		} catch (Exception e1) {
		}
		
		VitalPathQuery vpq = Queries.connectionsQuery(new ArrayList<VitalSegment>(), uri_str, depth, fClasses, rClasses);
		
		for(Entry<String, LuceneSegment> en : VitalSigns.get().getOntologyURI2Segment().entrySet()) {
			
			String domainSegment = en.getKey();

			vpq.setSegments(Arrays.asList(VitalSegment.withId(domainSegment)));
			
			ResultList rl = VitalSigns.get().query(vpq, Arrays.asList(domainSegment));
			
			li.addAll(rl.getResults());
			
		}
		
		if(serviceSegments.size() > 0){
		
			try {
	
				vpq.setSegments(serviceSegments);
				ResultList rl = vitalService.query(vpq);
				
				li.addAll(rl.getResults());
				
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
		}
		
		
		//filter out duplicate URIs objects now
		Set<String> uris = new HashSet<String>(); 
		for( Iterator<ResultElement> iterator = rs.getResults().iterator(); iterator.hasNext(); ) {
			ResultElement next = iterator.next();
			if(!uris.add(next.getGraphObject().getURI())) {
				iterator.remove();
			}
		}
		
		return rs;
		
	}

	public VitalSegment getWordnetSegment() {
		VitalSegment s1 = new VitalSegment();
		s1.setID("wordnet");
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

	public static void initForTests(VitalService service) {

		singleton = new Application();
		singleton.vitalService = service;
		
		singleton.endpointType = singleton.vitalService.getEndpointType();
		
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
			
			String gname = (String) n.getProperty("name").toString();
			
			Class<? extends GraphObject> cls = (Class<? extends GraphObject>) VitalSigns.get().getGroovyClass(u);//Class.forName((String) n.getProperty("name"));
			if(cls == null) throw new Exception("Class not found: " + gname + " uri: " + u);
			child.cls = cls;
			
			parent.children.add(child);
			
			processHierarchy(child, nodes, src2Dest);
			
		}
		
	}

	public void login(VitalService _service) throws Exception {
		
		VitalStatus status = _service.ping();
		
		if(status.getStatus() != VitalStatus.Status.ok) {
			throw new Exception("Ping failed: " + status.getMessage());
		}
		
		vitalService = _service;
		
		endpointType = vitalService.getEndpointType();
		
		notifyListenersOfLoginEvent();		
		
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
