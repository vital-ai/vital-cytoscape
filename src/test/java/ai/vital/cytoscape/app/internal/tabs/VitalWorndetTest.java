package ai.vital.cytoscape.app.internal.tabs;

/*
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.vitalai.domain.wordnet.NounSynsetNode;

import ai.vital.cytoscape.app.internal.queries.Queries2;
import ai.vital.cytoscape.app.internal.tabs.PathsTab.ExpansionDirection;
import ai.vital.vitalservice.VitalService;
import ai.vital.vitalservice.VitalStatus;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalGraphQuery;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.classes.ClassMetadata;
import ai.vital.vitalsigns.meta.PathElement;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VitalSegment;
import ai.vital.vitalsigns.model.VitalServiceKey;
import ai.vital.vitalsigns.model.properties.Property_hasKey;
*/

public class VitalWorndetTest {

	public static void main(String[] args) throws Exception {
		/*
		
		List<List<PathElement>> fPaths = null;
		List<List<PathElement>> rPaths = null;
		Class<? extends GraphObject> gClass = NounSynsetNode.class;
		fPaths = VitalSigns.get().getClassesRegistry().getPaths(gClass, true);
		rPaths = VitalSigns.get().getClassesRegistry().getPaths(gClass, false);
		
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
		
		
		VitalServiceKey key = new VitalServiceKey();
		key.set(Property_hasKey.class, "aaaa-aaaa-aaaa");
		
		VitalService service = VitalServiceFactory.openService(key, "sqlwordnet");
		
		List<VitalSegment> segments = Arrays.asList(service.getSegment("wordnet"));
		String inputURI = "http://vital.ai/vital.ai/app/NounSynsetNode/1447109396794_1265258334";
		
		VitalGraphQuery connectionsQueyGraph = Queries2.connectionsQueyGraph(segments, inputURI, 1, 0, 100, fClasses, rClasses);
		System.out.println(connectionsQueyGraph.debugString());
		ResultList rl = service.query(connectionsQueyGraph);
		System.out.println(rl.getStatus());

		for(GraphObject g : rl) {
			System.out.println(g);
		}
		
		
		 */
	}
	
}
