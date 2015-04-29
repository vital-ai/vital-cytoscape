package ai.vital.cytoscape.app.internal.queries

import ai.vital.property.URIProperty;
import ai.vital.query.querybuilder.VitalBuilder
import ai.vital.vitalservice.query.VitalGraphQueryTypeCriterion;
import ai.vital.vitalservice.query.VitalPathQuery;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.properties.PropertyTrait;
import ai.vital.vitalsigns.rdf.RDFUtils;
import ai.vital.vitalservice.query.VitalGraphQueryPropertyCriterion
import ai.vital.vitalservice.query.VitalGraphQueryPropertyCriterion.Comparator
import static ai.vital.query.Utils.*

class Queries {

	static def builder = new VitalBuilder()
	
	public static VitalSelectQuery searchQuery(List<VitalSegment> segmentsList, String searchString, Integer offset, Integer limit, String propertyURI, boolean orNotAnd) {
		
		String pname = RDFUtils.getPropertyShortName(propertyURI);
		
		VitalSelectQuery selectQuery = builder.query {
			
			SELECT {
				
				value segments: segmentsList
				
				value limit: limit
				
				value offset: offset
				
			
				OR {
					
					node_constraint { PropertyConstraint(propertyURI).equalTo_i(searchString) }
					
					orNotAnd ? OR {
						
						for(String kw : searchString.split("\\s+")) {
							
							node_constraint { ai.vital.query.Utils.PropertyConstraint(propertyURI).contains_i(kw) }
							
						}
						
					} : AND {
					
						for(String kw : searchString.split("\\s+")) {
					
							node_constraint { ai.vital.query.Utils.PropertyConstraint(propertyURI).contains_i(kw) }
						}
					
					}
					
					
					
				}	
				
				
			}
			
		}.toQuery()
		
		return selectQuery
		
	}
	
	public static VitalPathQuery connectionsQuery(List<VitalSegment> segments, String inputURI, List<Class<? extends VITAL_Edge>> forwardEdgeTypes, List<Class<? extends VITAL_Edge>> reverseEdgeTypes) {
		
		VitalPathQuery pathQuery = builder.query {
		
			PATH {
				
				value segments: segments
				
				value maxdepth: 1
				
				value rootURIs: [URIProperty.withString(inputURI)]

				if( forwardEdgeTypes.size() > 0 ) {
					
					ARC {
						
						value direction: 'forward'
						
						AND {
						OR {
							
							for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
								
								edge_constraint { c }
								
							}
							
						}
						}
					}
										
				}
				
				if( reverseEdgeTypes.size() > 0 ) {
					
					ARC {
					
						value direction: 'reverse'
					
						AND {
						OR {
							for( Class<? extends VITAL_Edge> c : reverseEdgeTypes ) {
								
								edge_constraint { c }
								
							}
						}
						}
						
					}
					
				}
				
			}
			
				
		}.toQuery()
		
		return pathQuery
		
		
	}
	
}
