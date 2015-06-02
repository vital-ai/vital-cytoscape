package ai.vital.cytoscape.app.internal.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import ai.vital.vitalsigns.model.property.IProperty;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;

import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.StmtIterator;

public class PropertyUtils {
	
	public static Map<Class<? extends GraphObject>, List<String>> cachedClass2PropertyName = Collections.synchronizedMap(new HashMap<Class<? extends GraphObject>, List<String>>());
	
	public static String resolveName(GraphObject entity) {
		
		
		List<String> pnames = cachedClass2PropertyName.get(entity.getClass());
		
		Object val = null;
		
		if(pnames != null) {
			
			for(String pname : pnames) {
				
				val = entity.getProperty(pname);
				
				if(val != null) return "" + val;
			}
			
			
		}
		
		try {
			
			//collect the label properties
			for(Entry<String, IProperty> entry : entity.getPropertiesMap().entrySet()) {
					
				String n = entry.getKey();
				
				OntProperty ontProperty = VitalSigns.get().getOntologyModel().getOntProperty(n);
				if(ontProperty != null) {
					
					for ( StmtIterator listProperties = ontProperty.listProperties(VitalCoreOntology.isDefaultLabel); listProperties.hasNext(); ) {
						
						Literal literal = listProperties.nextStatement().getLiteral();
						Object v = literal.getValue();
						
						if(Boolean.TRUE.equals(v) || (v instanceof String &&"true".equalsIgnoreCase((String) v))) {
							
							//keep the property in the cache
							if(pnames == null) {
								pnames = new ArrayList<String>();
								cachedClass2PropertyName.put(entity.getClass(), pnames);
								pnames.add(n);
							} else if( !pnames.contains(n)) {
								pnames.add(n);
							}
							
							val = entity.getProperty(n);
							
							if(val != null) {
								return "" + val;
							}
										
						}
						
					}
					
				}
				
			}
			
			return (String) entity.getProperty("name");
			
		} catch(Exception e) {
			
			return "Unhandled: " + entity.toString();
			
		}
		
	}
	
}
