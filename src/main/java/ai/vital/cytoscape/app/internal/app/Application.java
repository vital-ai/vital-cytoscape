package ai.vital.cytoscape.app.internal.app;

import java.awt.Color;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import ai.vital.domain.AdjectiveSynsetNode;
import ai.vital.domain.AdverbSynsetNode;
import ai.vital.domain.NounSynsetNode;
import ai.vital.domain.VerbSynsetNode;
import ai.vital.domain.ontology.VitalOntology;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.factory.Factory;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.datatype.VitalURI;
import ai.vital.vitalsigns.global.GlobalHashTable;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;

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
		
		o("Checking vital singleton...");
		VitalSigns.get().registerOntology(new VitalOntology());
		
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
//		return "http://127.0.0.1:9080";
		return "http://dataserver.moderni.st:80";
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
		try {
			GraphObject graphObjectExpanded = Factory.getVitalService().getExpanded(VitalURI.withString(uri_str), getWordnetSegment());
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
		
		return rs;
	}

	public VitalSegment getWordnetSegment() {
		VitalSegment s1 = new VitalSegment();
		s1.setId("wordnet");
		return s1;
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
