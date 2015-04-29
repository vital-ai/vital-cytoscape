package ai.vital.cytoscape.app.internal.queries

import ai.vital.domain.NounSynsetNode;
import ai.vital.domain.ontology.VitalOntology;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalPathQuery;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.meta.PathElement;
import ai.vital.vitalsigns.model.VITAL_Edge
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
		
		VitalServiceFactory.setServiceProfile("default")
		
		VitalService service = VitalServiceFactory.getVitalService();
		
		ResultList rl = service.query(q);
		
		println rl.results.size()
		
		println q
	}	

	public static void _testConnectionsQuery() {
		
		VitalSigns vs = VitalSigns.get()
		
		
		VitalServiceFactory.setServiceProfile("default")
		
		VitalService service = VitalServiceFactory.getVitalService();
		
		List<List<PathElement>> fPaths = vs.getClassesRegistry().getPaths(NounSynsetNode.class, true);
		List<List<PathElement>> rPaths = vs.getClassesRegistry().getPaths(NounSynsetNode.class, true);
		
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
		
		VitalPathQuery q = Queries.connectionsQuery([VitalSegment.withId("wordnet")], 'http://vital.ai/vital/vital/NounSynsetNode/1415978158890_707822408', fC, rC)
		
		ResultList rl = service.query(q);
		
		println rl.results.size()
		
		println q
		
	}	
}
