package ai.vital.cytoscape.app.internal.model;

import ai.vital.vitalsigns.model.GraphObject;

public class PropertyUtils {
	
	public static String resolveName(GraphObject entity) {
		
		String nameCandidate = null;
		
		try {
			return (String) entity.getProperty("name");
		} catch(Exception e) {
			return "Unhandled: " + entity.toString();
		}
		
	}
	
}
