package ai.vital.cytoscape.app.internal.model;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class Attributes {
	
	
	public final static String uri = "URI";
	
//	public final static String category = "category";
	
//	public final static String canonicalName = "canonicalName";
	
	public final static String scope = "scope";
	
	public final static String segment = "segment";
	
//	public final static String edge_type = "edge_type";
	
//	public final static String numReferences = "numReferences";

//	public final static String numInstances = "numInstances";
//
//	public final static String numSubsumptionEdges = "numSubsumptionEdges";
//
//	public final static String numInteractionEdges = "numInteractionEdges";
	
//	public final static String avgDestinationNormConfidence = "avgDestinationNormConfidence";
//	
//	public final static String avgSemanticConfidence = "avgSemanticConfidence";
//	
//	public final static String avgSourceNormConfidence = "avgSourceNormConfidence"; 
	
	public final static String timestamp = "timestamp";
	
	public final static String edgeSourceURI = "edgeSourceURI";
	
	public final static String edgeDestinationURI = "edgeDestinationURI";
	
	public final static String modificationDate = "modificationDate";

	public static String marked = "marked";
	
//	public static String type_id = "typeId";
	
	public static String edgeTypeURI = "edgeTypeURI";
	
	public static String nodeTypeURI = "nodeTypeURI";

	public static String type = "type";
	
	public static String score = "score";

	public static String name = "name"; 
	
	public final static String relationsCount = "relationsCount"; 
	

	//list attributes for GO Layout!
	public final static String GO_BIOLOGICAL_PROCESS = "annotation.GO BIOLOGICAL_PROCESS";
	
	public final static String GO_CELLULAR_COMPONENT = "annotation.GO CELLULAR_COMPONENT";
	
	public final static String GO_MOLECULAR_FUNCTION= "annotation.GO MOLECULAR_FUNCTION";

	
	public static Set<String> constants() {
		
		HashSet<String> attribs = new HashSet<String>();
		
		attribs.add(uri);
//		attribs.add(category);
//		attribs.add(canonicalName);
		
		return attribs;
		
	}
	
//	map.put(edge_type.class);
//	public final static String edge_type = "edge_type";
//	numReferences
//	numInstances
	
	public static LinkedHashMap<String,Class<?>> getNodeAttributesMap() {
		LinkedHashMap<String, Class<?>> map= new LinkedHashMap<String, Class<?>>();
		map.put(uri, String.class);
//		map.put(canonicalName, String.class);
//		map.put(category , String.class);
		map.put(scope,String.class);
		map.put(segment,String.class);
//		map.put(numInteractionEdges,Integer.class);
//		map.put(numSubsumptionEdges,Integer.class);
		map.put(timestamp,String.class);
		map.put(modificationDate,String.class);
		map.put(marked,Boolean.class);
		map.put(nodeTypeURI, String.class);
		
		map.put(GO_BIOLOGICAL_PROCESS, List.class);
		map.put(GO_CELLULAR_COMPONENT, List.class);
		map.put(GO_MOLECULAR_FUNCTION, List.class);
		
		//GO
//		map.put(AddGOAnnotations.GO_BIOLOGICAL_PROCESS, String.class);
//		map.put(AddGOAnnotations.GO_CELLURAL_COMPONENT, String.class);
//		map.put(AddGOAnnotations.GO_MOLECULAR_FUNCTION, String.class);
		
		return map;
	}
	
	public static LinkedHashMap<String,Class<?>> getEdgeAttributesMap() {
		LinkedHashMap<String, Class<?>> map= new LinkedHashMap<String, Class<?>>();
		map.put(uri, String.class);
//		map.put(canonicalName, String.class);
//		map.put(edge_type,String.class);
		map.put(edgeTypeURI, String.class);
//		map.put(category , String.class);
		map.put(scope,String.class);
		map.put(segment,String.class);
//		map.put(numInstances,Integer.class);
//		map.put(numReferences,Integer.class);
		map.put(timestamp,String.class);
		map.put(modificationDate,String.class);
//		map.put(marked,Boolean.class);
		return map;
	}
	
	
	
	
	

}
