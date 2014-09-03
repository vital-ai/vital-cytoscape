package ai.vital.cytoscape.app.internal.app;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import ai.vital.lucene.model.LuceneSegment;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.factory.Factory;
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
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.URIPropertyValue;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;

public class Application {

	private static Application singleton;
	
	private List<LoginListener> loginListeners = new ArrayList<LoginListener>();
	
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
		o("$VITAL_HOME: " + vitalHome);
		o("Checking vital singleton...");
		VitalSigns vs = VitalSigns.get();
//		o("Singleton obtained, registering vital domain ontology...");
		File domainJarsDir = new File(vitalHome, "domain-jar");
		o("Domain jars path: " + domainJarsDir.getAbsolutePath() + " dir ? " + domainJarsDir.isDirectory());
		//vs.registerOntology(new VitalOntology());
		
		if(domainJarsDir.isDirectory()) {
			
			o("Domain files count: " +domainJarsDir.listFiles().length);
			for(File f : domainJarsDir.listFiles()) {
				
				if(!f.getName().endsWith(".jar")) {
					continue;
				}
				
				o("Registering domain ontology: " + f.getName());
				try {
					vs.registerOntology(f.toURI().toURL());
				} catch (MalformedURLException e) {}
				
			}
		} else {
			o("ERROR - $VITAL_HOME/domain-jar/ directory does not exist");
		}
		
	}

	public void login(String username, String password, String url) throws Exception {

		o("Logging in...");
		
		new URL(url);
		
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
		
		Factory.getVitalService();
		
		o("Endpoint changed to: " + url);
		
		/*
		o("Testing expansion...");
		
		ResultList expanded = getConnections("http://uri.vital.ai/wordnet/NounSynsetNode_1396611318625_1016512");
		
		for(ResultElement el : expanded.getResults()) {
			System.out.println(el.getGraphObject());
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
	
	static void o(String m){System.out.println(m);}

	public void logout() {

		notifyListenersOfLogoutEvent();
		
	}

	public String getInitialURL() {
		return "http://127.0.0.1:9080";
//		return "http://dataserver.moderni.st:80";
	}

	public ResultList search(VitalSelectQuery sq) throws Exception {
		return Factory.getVitalService().selectQuery(sq);
	}

	public boolean isExpandUsingSynonyms() {
		return false;
	}

	public ResultList getConnections(String uri_str) {

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
			
			sq.getComponents().add(new VitalPropertyConstraint(VitalCoreOntology.hasEdgeSource.getURI(), new URIPropertyValue(uri_str), Comparator.EQ));
			sq.getComponents().add(new VitalPropertyConstraint(VitalCoreOntology.hasEdgeDestination.getURI(), new URIPropertyValue(uri_str), Comparator.EQ));
			
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
			GraphObject graphObjectExpanded = Factory.getVitalService().getExpandedInSegments(VitalURI.withString(uri_str), serviceSegments );
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
			e.printStackTrace();
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
		return Factory.getVitalService().listSegments();
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
