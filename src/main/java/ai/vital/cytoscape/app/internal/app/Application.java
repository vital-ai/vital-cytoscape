package ai.vital.cytoscape.app.internal.app;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import ai.vital.cytoscape.app.internal.queries.Queries;
import ai.vital.cytoscape.app.internal.tabs.PathsTab.ExpansionDirection;
import ai.vital.domain.Datascript;
import ai.vital.domain.Job;
import ai.vital.prime.service.VitalServicePrime;
import ai.vital.vitalservice.EndpointType;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.VitalStatus;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalGraphQuery;
import ai.vital.vitalservice.query.VitalGraphQueryPropertyCriterion.Comparator;
import ai.vital.vitalservice.query.VitalGraphQueryTypeCriterion;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.classes.ClassMetadata;
import ai.vital.vitalsigns.conf.VitalSignsConfig;
import ai.vital.vitalsigns.conf.VitalSignsConfig.DomainsStrategy;
import ai.vital.vitalsigns.conf.VitalSignsConfig.DomainsSyncMode;
import ai.vital.vitalsigns.meta.PathElement;
import ai.vital.vitalsigns.model.Edge_hasChildCategory;
import ai.vital.vitalsigns.model.GraphMatch;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;
import ai.vital.vitalsigns.model.VitalApp;
import ai.vital.vitalsigns.model.VitalSegment;
import ai.vital.vitalsigns.model.property.IProperty;
import ai.vital.vitalsigns.model.property.URIProperty;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;
import ai.vital.vitalsigns.uri.URIGenerator;

public class Application {

	private static Application singleton;
	
	private List<LoginListener> loginListeners = new ArrayList<LoginListener>();

	private final static Logger log = LoggerFactory.getLogger(Application.class);

	private static final int HARD_LIMIT = 10000;

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
	
	public static void init() throws Throwable {
		if(singleton != null) return;
		singleton = new Application();
		
		String vitalHome = System.getenv("VITAL_HOME");
		log.info("Checking vital singleton...");
		log.info("$VITAL_HOME: " + vitalHome);
		
		VitalSigns vs = null;
		
		try {
			
			if(vitalHome == null || vitalHome.isEmpty()) {
				throw new Exception("VITAL_HOME environment variable not set");
			}
			
			File vh = new File(vitalHome);
			if(!vh.exists()) {
				throw new Exception("VITAL_HOME location does not exist: " + vh.getAbsolutePath());
			}
			
			if(!vh.isDirectory()) {
				throw new Exception("VITAL_HOME location is not a directory: " + vh.getAbsolutePath());
			}
			
			File configFile = VitalSigns.getConfigFile(vh);
			if(!configFile.exists()) {
				throw new Exception("VitalSigns config file does not exist, path: " + configFile.getAbsolutePath());
			}
			
			try {
				VitalSignsConfig.fromTypesafeConfig(configFile);
			} catch(Exception e) {
				throw new Exception("VitalSigns config file is invalid: " + e.getLocalizedMessage());
			}
			
			File coreModelFile = new File(vitalHome, "vital-ontology/" + VitalCoreOntology.FILE_NAME);
//			File coreOwvitalHome
			
			if(!coreModelFile.exists()) {
			    throw new Exception("Vital core ontology file not found: " + coreModelFile.getAbsolutePath());
			}
			
		
			File licenseFile = new File(vh, "vital-license/vital-license.lic");
			if(!licenseFile.exists()) throw new Exception("Vital license file not found, path: " + licenseFile.getAbsolutePath());
			
			Map<String, Object> cfg = new HashMap<String, Object>();
			cfg.put("domainsStrategy", DomainsStrategy.dynamic.name());
			cfg.put("autoLoad", true);
			cfg.put("loadDeployedJars", false);
			//use whatever value is set in prime
//			cfg.put("domainsSyncMode", DomainsSyncMode.pull.name());
			
			log.info("Overridden vitalsigns config params: {}", cfg);
			
			VitalSigns.mergeConfig = ConfigFactory.parseMap(cfg);
			
			vs = VitalSigns.get();
			
		} catch(Throwable e) {
			log.error(e.getLocalizedMessage());
			throw e;
//			JOptionPane.showMessageDialog(null, e.getMessage(), "Vital AI initialization error", JOptionPane.ERROR_MESSAGE);
//			throw new RuntimeException(e);
		}
		
		
	}

//	public void login(String username, String password, String url) throws Exception {
//
//		log.debug("Logging in...");
//		
//		for(VitalService service : VitalServiceFactory.listOpenServices()) {
//			service.close();
//		}
//		
//		if(endpointType == EndpointType.VITALPRIME) {
//			
//			
//			String customerID = serviceConfig.getString("customerID");
//			String appID = serviceConfig.getString("appID");
//			
//			App app = new App();
//			app.setID(appID);
//			app.setOrganizationID(customerID);
//			
//			Organization organization = new Organization();
//			organization.setID(customerID);
//			
//			new URL(url);
//			
////			VitalServicePrimeConfigCreator creator = new VitalServicePrimeConfigCreator();
//			VitalServicePrimeConfig cfg = new VitalServicePrimeConfig();
//			
//			cfg.setApp(app);
//			cfg.setOrganization(organization);
//			cfg.endpointURL = url;
//			
//			VitalService _service = VitalServiceFactory.createVitalService(cfg);
//			
//			VitalStatus status = _service.ping();
//			
//			if(status.getStatus() != VitalStatus.Status.ok) {
//				throw new Exception("Ping failed: " + status.getMessage());
//			}
//			
//			vitalService = _service;
//			
////			creator.setCustomConfigProperties(cfg, serviceConfig);
//			
//			
//		} else {
//			
//			//just create the service with factory
//			
//			vitalService = VitalServiceFactory.getVitalService();
//			
//		}
//		
//		/*
//		VitalService.setEndpoint(url);
//		
//		Map<String, Object> params = new HashMap<String, Object>();
//		params.put("accountType", "local");
//		params.put("segment", "modernist");
//		params.put("username", username);
//		params.put("password", password);
//		
//		ResultSet rs = VitalService.getInstance().callFunction("AuthenticateUser.groovy", params);
//		if(!rs.isOk()) throw new Exception(rs.getErrorMessage());
//		*/
//		//successfully authenticated?
//		
//		/*
//		o("Testing expansion...");
//		
//		ResultList expanded = getConnections("http://uri.vital.ai/wordnet/NounSynsetNode_1396611318625_1016512");
//		
//		for(ResultElement el : expanded.getResults()) {
//			log.debug(el.getGraphObject());
//		}
//		*/
//		
//		notifyListenersOfLoginEvent();
//		
//	}


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
		//reset hierarchy root
		this.root = new HierarchyNode();
		notifyListenersOfLogoutEvent();
		
	}

	public ResultList search(VitalSelectQuery sq) throws Exception {
		return vitalService.query(sq);
	}

	public boolean isExpandUsingSynonyms() {
		return false;
	}

	public ResultList getConnections(String uri_str, String typeURI, int offset, int limit) {

		log.info("Getting connections, URI: {}, typeURI: {}, offset: {}, limit: {}", new Object[]{uri_str, typeURI, offset, limit});
		
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
		
		List<VitalSegment> selectedSegments = VitalAICytoscapePlugin.getPathSegments();
		
		Integer depth = VitalAICytoscapePlugin.getDepth();
		if(depth == null) depth = 1;
		
		ResultList rs = new ResultList();
//		List<ResultElement> li = new ArrayList<ResultElement>();
//		rs.setResults(li);

		Map<String, GraphObject> resultsMap = new HashMap<String, GraphObject>();
		
		
		List<Class<? extends VITAL_Edge>> fClasses = new ArrayList<Class<? extends VITAL_Edge>>();
		List<Class<? extends VITAL_Edge>> rClasses = new ArrayList<Class<? extends VITAL_Edge>>();
		
		
		/* taxonomy based approach */
		/*
		List<List<PathElement>> fPaths = null;
		List<List<PathElement>> rPaths = null;
		try {
			ClassMetadata cm = VitalSigns.get().getClassesRegistry().getClass(typeURI) ;
			if(cm == null) throw new Exception("Class not found: " + typeURI);
			Class<? extends GraphObject> gClass = cm.getClazz();
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
			rs.setStatus(VitalStatus.withError(e2.getLocalizedMessage()));
			return rs;
		}
		
		for(List<PathElement> p : fPaths) {
			PathElement pe = p.get(0);
			if(!pe.isHyperedge()) fClasses.add((Class<? extends VITAL_Edge>) pe.getEdgeClass());
		}
		
		for(List<PathElement> p : rPaths) {
			PathElement pe = p.get(0);
			if(!pe.isHyperedge()) rClasses.add((Class<? extends VITAL_Edge>) pe.getEdgeClass());
		}
		*/

		List<Class<? extends VITAL_Node>> allNodeTypes = VitalAICytoscapePlugin.getAllNodeTypes(); 
		List<Class<? extends VITAL_Node>> nodeTypes = VitalAICytoscapePlugin.getSelectedNodeTypes();
//		log.info("Selected nodeTypes [{}]: {}", nodeTypes.size(), nodeTypes.toString());
		if(nodeTypes.isEmpty()) {
			log.error("No node types selected");
			rs.setLimit(-1);
			return rs;
		}
		
		List<Class<? extends VITAL_Edge>> allEdgeTypes = VitalAICytoscapePlugin.getAllEdgeTypes();
		List<Class<? extends VITAL_Edge>> edgeTypes = VitalAICytoscapePlugin.getSelectedEdgeTypes();
//		log.info("Selected edgeTypes [{}]: {}: ", edgeTypes.size(), edgeTypes.toString());
		if(edgeTypes.isEmpty()) {
			log.error("No edge types selected");
			rs.setLimit(-1);
			return rs;
		}
		
		
		boolean forward = direction == ExpansionDirection.Both || direction == ExpansionDirection.Outgoing;
		
		boolean reverse = direction == ExpansionDirection.Both || direction == ExpansionDirection.Incoming;
		
		
		if(allEdgeTypes.size() == edgeTypes.size()) {
			//empty constraints
			log.info("all edge types optimization enabled");
		} else {
			//check direction and append type constraints
			if(forward) {
				fClasses = edgeTypes;
			}
			if(reverse) {
				rClasses = edgeTypes;
			}
			
		}
		
		if(allNodeTypes.size() == nodeTypes.size()) {
			//empty constraints
			log.info("all node types optimization enabled");
			nodeTypes = new ArrayList<Class<? extends VITAL_Node>>();
			
		} else {

			
		}
		
		List<VitalGraphQueryTypeCriterion> exclusiveEdgeTypes = new ArrayList<VitalGraphQueryTypeCriterion>();
		List<VitalGraphQueryTypeCriterion> exclusiveNodeTypes = new ArrayList<VitalGraphQueryTypeCriterion>();
		
		if(EndpointType.ALLEGROGRAPH == Application.get().getVitalService().getEndpointType() ) {
			
			if(allEdgeTypes.size() != edgeTypes.size() && edgeTypes.size() > allEdgeTypes.size()  / 2) {
				
				log.info("Sparql endpoint exlusive edges queries optimization");
				
				for(Class<? extends VITAL_Edge> ec : allEdgeTypes) {
					if(!edgeTypes.contains(ec)) {
						VitalGraphQueryTypeCriterion vitalGraphQueryTypeCriterion = new VitalGraphQueryTypeCriterion(ec);
						vitalGraphQueryTypeCriterion.setNegative(true);
						exclusiveEdgeTypes.add(vitalGraphQueryTypeCriterion);
					}
				}
				
				fClasses = Collections.emptyList();
				rClasses = Collections.emptyList();
				
			}
			
			if(allNodeTypes.size() != nodeTypes.size() && nodeTypes.size() > allNodeTypes.size() / 2) {
				
				log.info("Sparql endpoint exlusive nodes queries optimization");
				
				for(Class<? extends VITAL_Node> nc : allNodeTypes) {
					if(!nodeTypes.contains(nc)) {
						VitalGraphQueryTypeCriterion vitalGraphQueryTypeCriterion = new VitalGraphQueryTypeCriterion(nc);
						vitalGraphQueryTypeCriterion.setNegative(true);
						exclusiveNodeTypes.add(vitalGraphQueryTypeCriterion);
					}
					
				}
				
				nodeTypes = Collections.emptyList();
				
			}
			
		}
		
		
		
//		List<VitalSegment> serviceSegments = new ArrayList<VitalSegment>();
//		try {
//			serviceSegments = getServiceSegments();
//		} catch (Exception e1) {
//		}
		
		
		
//		//XXX temporarily override f and r classes
//		if(direction == ExpansionDirection.Both || direction == ExpansionDirection.Outgoing) {
//			fClasses.add(Edge_hasChildCategory.class);
//		} else {
//			fClasses.clear();
//		}
//		
//		//XXX temporarily override f and r classes
//		if(direction == ExpansionDirection.Both || direction == ExpansionDirection.Incoming) {
//			rClasses.add(Edge_hasChildCategory.class);
//		} else {
//			rClasses.clear();
//		}
		
		
		VitalGraphQuery vgq = Queries.connectionsQueyGraph(new ArrayList<VitalSegment>(), uri_str, depth, offset, limit, forward, reverse, fClasses, rClasses, nodeTypes, exclusiveEdgeTypes, exclusiveNodeTypes);
		
		
		//XXX path query
//		VitalPathQuery vpq = Queries.connectionsQuery(new ArrayList<VitalSegment>(), uri_str, depth, fClasses, rClasses);
//		rs.setLimit(-1);

		List<String> nsList = new ArrayList<String>(VitalSigns.get().getOntologyURI2ImportsTree().keySet());
		
		for(String domainSegment : nsList) {
			
			try {
				
				//XXX path query
//				vpq.setSegments(Arrays.asList(VitalSegment.withId(domainSegment)));
//				
//				ResultList rlx = VitalSigns.get().query(vpq, nsList);
//				
//				filterGraphMatch(rlx, resultsMap);
				
				
				vgq.setSegments(Arrays.asList(VitalSegment.withId(domainSegment)));
				
				ResultList rlx = VitalSigns.get().query(vgq, nsList);
					
				if(rlx.getResults().size() < limit) {
						
					offset = -1;
						
				} else if(offset + limit >= HARD_LIMIT) {
						
					log.info("Local Query HARD LIMIT hit: " + HARD_LIMIT + " node " + uri_str + " expansion stopped");
						
					offset = -1;
						
				} else {
						
					offset += limit;
					
				}
					
				filterGraphMatch(rlx, resultsMap);
					
			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
			
		}
		
			
			
//		}
		
		log.info("Selected segments count: " + selectedSegments.size());
		
		if(selectedSegments.size() > 0){
		
			try {

//				XXX path query
//				vpq.setSegments(serviceSegments);
//				ResultList rlx = vitalService.query(vpq);
//				filterGraphMatch(rlx, resultsMap);
				
				vgq.setSegments(selectedSegments);
				
				ResultList rlx = vitalService.query(vgq);
				
				log.info("GraphQuery status: {}", rlx.getStatus().toString());
				
				if(rlx.getResults().size() < limit) {
						
					offset = -1;
					rs.setLimit(-1);
						
				} else if(offset + limit >= HARD_LIMIT) {
						
					log.info("Service query HARD LIMIT hit: " + HARD_LIMIT + " node " + uri_str + " expansion stopped");
						
					offset = -1;
					
					rs.setLimit(-1);
						
				} else {
						
					offset += limit;
						
					rs.setLimit(offset);
					
				}
					
				filterGraphMatch(rlx, resultsMap);

			} catch (Exception e) {
				log.error(e.getLocalizedMessage(), e);
			}
			
		} else {
			
			rs.setLimit(offset);
			
		}
		

		for(Entry<String, GraphObject> entry : resultsMap.entrySet()) {
			
			rs.getResults().add(new ResultElement(entry.getValue(), 1D));
			
		}
		
		
		return rs;
		
	}

	public static void filterGraphMatch(ResultList rlx,
			Map<String, GraphObject> resultsMap) {

		
		Set<String> uriProps = new HashSet<String>();
		
		for(GraphObject g : rlx) {
			
			if(g instanceof GraphMatch) {

				uriProps.clear();
				
				for(IProperty p : g.getPropertiesMap().values()) {
					if(!(p instanceof URIProperty)) continue;
					URIProperty u = (URIProperty) p;
					
					String objURI = u.get();
					if(resultsMap.containsKey(objURI)) {
						
						continue;
						
					}
					
					uriProps.add(objURI);
				}
				
				for(String uriProp : uriProps) {
					
					Object v = g.getProperty(uriProp);
					if(v == null || !(v instanceof GraphObject)) continue;
					
					GraphObject gx = (GraphObject) v;
					
					resultsMap.put(gx.getURI(), gx);
					
				}
				
			} else {
				
				if(resultsMap.containsKey(g.getURI())) continue;
				
				resultsMap.put(g.getURI(), g);
				
			}
			
			
		}
		
		
	}

//	public VitalSegment getWordnetSegment() {
//		VitalSegment s1 = new VitalSegment();
//		s1.setID("wordnet");
//		return s1;
//	}

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
					IProperty callable = (IProperty) g.getProperty("callable");
					if(callable == null || callable.rawValue().equals(Boolean.FALSE)) {
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
	
	public HierarchyNode getClassHierarchy(Class<? extends GraphObject> rootClass, boolean local) throws Exception {

		//prefetch the entire tree

		if(root.URI == null) {
			
			synchronized(root) {
				
				if(root.URI == null) {
					
					initializeHierarchy(local);
					
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

	private void initializeHierarchy(boolean local) throws Exception {
		
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("class", GraphObject.class.getCanonicalName());
		
		ResultList rl = null;
		
		if(local) {
			
			rl = getLocalHierarchy();
			
		} else {
			
			rl = vitalService.callFunction("commons/scripts/GetClassHierarchy", params);
			
		}
		
				
		
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

	//adapted from commons.scripts.GetClassHierarchy datascript
	private ResultList getLocalHierarchy() throws Exception {
		
		ResultList r = new ResultList();
		
		OntModel ontModel = VitalSigns.get().getOntologyModel();
		
		List<GraphObject> target = new ArrayList<GraphObject>();

		collectSubclasses(null, GraphObject.class, ontModel, target);
		
		for(GraphObject g : target) {
			r.getResults().add(new ResultElement(g, 1D));
		}
					
		r.setTotalResults(target.size());
		
		return r;
	}
	
	private void collectSubclasses(VITAL_Node parentClsNode, Class cls, OntModel ontModel, List<GraphObject> target) throws Exception {
		
		//add to target
		VITAL_Node clsNode = new VITAL_Node();
		clsNode.setProperty("name", cls.getCanonicalName());
		
		target.add(clsNode);
		
		
		List<OntClass> subclasses = null;
		
		if(GraphObject.class.equals(cls)) {
		
			subclasses = new ArrayList<OntClass>();
			
			subclasses.add(ontModel.getOntClass(VitalCoreOntology.VITAL_Edge.getURI()));
			subclasses.add(ontModel.getOntClass(VitalCoreOntology.VITAL_HyperEdge.getURI()));
			subclasses.add(ontModel.getOntClass(VitalCoreOntology.VITAL_HyperNode.getURI()));
			subclasses.add(ontModel.getOntClass(VitalCoreOntology.VITAL_Node.getURI()));
			
			//virtual node!
			clsNode.setURI(VitalCoreOntology.NS + "GraphObject");
			
		} else {
		
			String clsURI = VitalSigns.get().getClassesRegistry().getClassURI(cls);
			if(clsURI == null || clsURI.isEmpty()) throw new Exception("No URI of class: " + cls.getCanonicalName() + " found");
			
			OntClass ontCls = ontModel.getOntClass(clsURI);
			
			if(ontCls == null) throw new Exception("Ont class not found: " + clsURI + ", " + cls.getCanonicalName()); 
		
			subclasses = ontCls.listSubClasses(true).toList();
			
			clsNode.setURI(ontCls.getURI());			
				
		}
		
		if(parentClsNode != null) {
			VITAL_Edge e = new VITAL_Edge().addSource(parentClsNode).addDestination(clsNode);
			e.generateURI((VitalApp)null);
			target.add(e);
			
		}
		
		for(OntClass c : subclasses) {
		
			ClassMetadata sc = VitalSigns.get().getClassesRegistry().getClass(c.getURI());
			
			if(sc == null) throw new Exception("No groovy class found for URI: " + c.getURI());
			
			collectSubclasses(clsNode, sc.getClazz(), ontModel, target);	
		
		}
		
		
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
			
			ClassMetadata cm = VitalSigns.get().getClassesRegistry().getClass(u);
			
			if(cm == null) {
				log.warn("Class not found: " + gname + " uri: " + u);
				continue;
//				throw new Exception("Class not found: " + gname + " uri: " + u);
			}
			
			Class<? extends GraphObject> cls = cm.getClazz();
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
