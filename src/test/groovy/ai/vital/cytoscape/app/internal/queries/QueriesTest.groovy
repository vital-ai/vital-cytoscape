package ai.vital.cytoscape.app.internal.queries

import ai.vital.domain.ontology.VitalOntology;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalPathQuery;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.meta.PathElement;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Edge
import ai.vital.vitalsigns.model.VitalApp
import ai.vital.vitalsigns.model.VitalSegment;
import ai.vital.vitalsigns.model.VitalServiceKey;
import ai.vital.vitalsigns.model.property.URIProperty;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;
import junit.framework.TestCase


class QueriesTest extends TestCase {

	public static void main(String[] args) {
		
		_testSearchQuery();
		
		_testConnectionsQuery();
		
	}
	
	public void test() {
		
	}

	public static void _testSearchQuery() {
		
		VitalSigns vs = VitalSigns.get()
		
		VitalSelectQuery q = Queries.searchQuery([VitalSegment.withId("wordnet")], "apple two", 0, 1000, VitalCoreOntology.NS + "hasName", true)
		
		VitalServiceKey sk = new VitalServiceKey().generateURI((VitalApp) null)
		sk.key = 'skey-skey-skey'
		VitalService service = VitalServiceFactory.openService(sk)
		
		ResultList rl = service.query(q);
		
		println rl.results.size()
		
		println q
	}	

	public static void _testConnectionsQuery() {
		
		VitalSigns vs = VitalSigns.get()

		VitalServiceKey sk = new VitalServiceKey().generateURI((VitalApp) null)
		sk.key = 'skey-skey-skey'
		VitalService service = VitalServiceFactory.openService(sk)

		
		Class<? extends GraphObject> nounSynsetNodeClass = VitalSigns.get().getClass(URIProperty.withString('http://vital.ai/ontology/vital-wordnet#NounSynsetNode'))
		if(nounSynsetNodeClass == null) {
			throw new RuntimeException("vital-nlp apparently not loaded, http://vital.ai/ontology/vital-wordnet#NounSynsetNode class not found")
		}
		
		List<List<PathElement>> fPaths = vs.getClassesRegistry().getPaths(nounSynsetNodeClass, true);
		List<List<PathElement>> rPaths = vs.getClassesRegistry().getPaths(nounSynsetNodeClass, true);
		
		List<Class<? extends VITAL_Edge>> fC = []
		for(List<PathElement> p : fPaths) {
			if(!p[0].isHyperedge()) {
				fC.add(p[0].getEdgeClass())
			}
		}
		
		List<Class<? extends VITAL_Edge>> rC = []
		for(List<PathElement> p : rPaths) {
			if(!p[0].isHyperedge()) {
				rC.add(p[0].getEdgeClass())
			}
		}
		
		VitalPathQuery q = Queries.connectionsQuery([VitalSegment.withId("wordnet")], 'http://vital.ai/vital/vital/NounSynsetNode/1415978158890_707822408', 1, fC, rC)
		
		ResultList rl = service.query(q);
		
		println rl.results.size()
		
		println q
		
	}	
}
