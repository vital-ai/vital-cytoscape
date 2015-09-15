package ai.vital.cytoscape.app.internal.queries

import ai.vital.vitalsigns.model.property.BooleanProperty;
import ai.vital.vitalsigns.model.property.DateProperty;
import ai.vital.vitalsigns.model.property.DoubleProperty;
import ai.vital.vitalsigns.model.property.FloatProperty;
import ai.vital.vitalsigns.model.property.GeoLocationProperty;
import ai.vital.vitalsigns.model.property.IProperty
import ai.vital.vitalsigns.model.property.IntegerProperty;
import ai.vital.vitalsigns.model.property.LongProperty;
import ai.vital.vitalsigns.model.property.StringProperty;
import ai.vital.vitalsigns.model.property.URIProperty;
import ai.vital.query.Utils;
import ai.vital.query.querybuilder.VitalBuilder
import ai.vital.vitalservice.query.VitalGraphQuery;
import ai.vital.vitalservice.query.VitalGraphQueryTypeCriterion;
import ai.vital.vitalservice.query.VitalPathQuery;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.properties.PropertyMetadata;
import ai.vital.vitalsigns.properties.PropertyTrait;
import ai.vital.vitalsigns.rdf.RDFDate;
import ai.vital.vitalsigns.rdf.RDFUtils;
import ai.vital.vitalservice.query.VitalGraphQueryPropertyCriterion
import ai.vital.vitalservice.query.VitalGraphQueryPropertyCriterion.Comparator
import static ai.vital.query.Utils.*

class Queries {

	static def builder = new VitalBuilder()
	
	public static VitalSelectQuery searchQuery(List<VitalSegment> segmentsList, String searchString, Integer offset, Integer limit, PropertyMetadata pm, boolean orNotAnd) {
		
		String propertyURI = pm.getURI()
		
		VitalSelectQuery selectQuery = null
		
		Class<? extends IProperty> bc = pm.getBaseClass()
		
		if(bc.equals(StringProperty.class)) {
		
			selectQuery = builder.query {
				
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
				
		} else {
		
		
			Object val = null
			
			if( bc.equals(DoubleProperty.class)) {
			
				try {
					val = Double.parseDouble(searchString.trim())
				} catch(Exception e) {
					throw new Exception("Couldn't convert string: " + searchString + " into double - property " + propertyURI + " is a double one" )
				}
			
			} else if(bc.equals(FloatProperty.class)) {
			
				
				try {
					val = Float.parseFloat(searchString.trim())
				} catch(Exception e) {
					throw new Exception("Couldn't convert string: " + searchString + " into float - property " + propertyURI + " is a float one" )
				}
			
			} else if( bc.equals(IntegerProperty.class) ) {
			
				try {
					val = Integer.parseInt(searchString.trim())
				} catch(Exception e) {
					throw new Exception("Couldn't convert string: " + searchString + " into integer - property " + propertyURI + " is an integer one" )
				}	
				
			} else if( bc.equals(LongProperty.class)) {
			
				try {
					val = Long.parseLong(searchString.trim())
				} catch(Exception e) {
					throw new Exception("Couldn't convert string: " + searchString + " into long integer - property " + propertyURI + " is a long one" )
				}
					
			} else if( bc.equals(DateProperty.class) ) {
			
				//parse time
				try {
					RDFDate.fromXSDString(searchString.trim())
				} catch(Exception e) {
					throw new Exception("Couldn't convert string: " + searchString + " into date - property " + propertyURI + " is a date one, error: " + e.getLocalizedMessage())
				}
			
			} else if( bc.equals(URIProperty.class) ) {
			
				val = URIProperty.withString(searchString)
			
			} else if( bc.equals(BooleanProperty.class)) {
			
				if(searchString.equalsIgnoreCase("true") ) {
					val = true
				} else if(searchString.equals("false")) {
					val = false
				} else {
					throw new RuntimeException("Couldn't convert string: " + searchString + " into boolean - property " + propertyURI + " is a boolean one")
				}
			
			} else {
				throw new RuntimeException("Property unsupported: " + propertyURI + ", type: " + bc.getSimpleName()) 
			}
			
			
			
			selectQuery = builder.query {
				
				SELECT {
					
					value segments: segmentsList
					
					value limit: limit
					
					value offset: offset
					
				
					AND {
						
						node_constraint { PropertyConstraint(propertyURI).equalTo(val) }
						
					}
					
					
				}
				
			}.toQuery()
				
		
		}
		

		
		return selectQuery
		
	}
	
	public static VitalGraphQuery connectionsQueyGraph(List<VitalSegment> segments, String inputURI, Integer depth, int offset, int limit, List<Class<? extends VITAL_Edge>> forwardEdgeTypes, List<Class<? extends VITAL_Edge>> reverseEdgeTypes) {
		
		if(depth > 2) throw new RuntimeException("max depth 2 supported at this moment")
		
		//split it into n queries
		
		def graphQueryObj = null;
		
		//both
		
		if(forwardEdgeTypes.size() > 0 && reverseEdgeTypes.size() > 0) {
			
			
			graphQueryObj = builder.query {
				
				GRAPH {
					
					value segments: segments
					
					value offset: offset
					
					value limit: limit
					
					value inlineObjects: true
					
					ARC {
						
						node_constraint { "URI = ${inputURI}" }
						
						ARC_OR {
							
							if(depth > 1) {
								
								//--> -->
								ARC {
		
									value direction: "forward"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
										
									}
									
									ARC {
											
//										value optional: true
											
										value direction: "forward"
											
										AND {
											
											OR {
												
												for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
													
													edge_constraint { c }
													
												}
												
												
											}
											
										}
											
									}
									
								}
								
								
								//--> <--
								ARC {
									
									value direction: "forward"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
										
									}
									
									ARC {
										
//										value optional: true
										
										value direction: "reverse"
											
										AND {
											
											OR {
												
												for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
													
													edge_constraint { c }
													
												}
												
												
											}
											
										}
										
									}
									
								}
								
								//<-- -->
								ARC {
		
//									value direction: "reverse"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
										
									}
									
									ARC {
										
//										value optional: true
										
										value direction: "forward"
											
										AND {
											
											OR {
												
												for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
													
													edge_constraint { c }
													
												}
												
												
											}
											
										}
										
									}
									
								}
								
								//<-- <--
								ARC {
		
									value direction: "reverse"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
										
									}
									
									ARC {
										
//										value optional: true
										
										value direction: "reverse"
											
										AND {
											
											OR {
												
												for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
													
													edge_constraint { c }
													
												}
												
												
											}
											
										}
										
									}
									
								}
								
								// -->
								ARC {
									
									value direction: "forward"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
									}
								}
								
								
								// <--
								ARC {
									
									value direction: "reverse"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
										
									}
									
								}
								
							} else {
							
								ARC {
								
									value direction: "forward"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
									}
								}
								
								ARC {
									
									value direction: "reverse"
											
									AND {
															
										OR {
												
											for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
													
												edge_constraint { c }
													
											}
												
										}
										
									}
									
								}
								
							}
							
						}
						
					}
					
				}
				
			}
			
		} else if(forwardEdgeTypes.size() > 0) {
		
			graphQueryObj = builder.query {
				
				GRAPH {
					
					value segments: segments
					
					value offset: offset
					
					value limit: limit
					
					value inlineObjects: true
					
					ARC {
						
						node_constraint { "URI = ${inputURI}" }
						
						//foward arc
						ARC {
	
							value direction: "forward"
									
							AND {
													
								OR {
										
									for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
											
										edge_constraint { c }
											
									}
										
								}
								
							}
								
							if(depth > 1) {
								
								ARC {
									
									value optional: true
									
									value direction: "forward"
										
									AND {
										
										OR {
											
											for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
												
												edge_constraint { c }
												
											}
											
											
										}
										
									}
									
								}
									
							}
							
						}
							
					}
					
				}
				
			}
			
		} else if(reverseEdgeTypes.size() > 0) {
		
			graphQueryObj = builder.query {
				
				GRAPH {
					
					value segments: segments
					
					value offset: offset
					
					value limit: limit
					
					value inlineObjects: true
					
					ARC {
						
						node_constraint { "URI = ${inputURI}" }
						
						//reverse arc
						ARC {

							value direction: "reverse"
									
							AND {
													
								OR {
										
									for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
											
										edge_constraint { c }
											
									}
										
								}
								
							}
							
							if(depth > 1) {
								
								ARC {
									
									value optional: true
									
									value direction: "reverse"
										
									AND {
										
										OR {
											
											for(Class<? extends VITAL_Edge> c : reverseEdgeTypes) {
												
												edge_constraint { c }
												
											}
											
										}
										
									}
									
								}
							
							}
							
						}
						
					}
					
				}
				
			}
		
		} else {
			throw new RuntimeException("No forward/reverse classes for graph query");
		}
		
		VitalGraphQuery graphQuery = graphQueryObj.toQuery()
		
		return graphQuery
		
	}
	
	public static VitalPathQuery connectionsQuery(List<VitalSegment> segments, String inputURI, Integer depth, List<Class<? extends VITAL_Edge>> forwardEdgeTypes, List<Class<? extends VITAL_Edge>> reverseEdgeTypes) {
		
		VitalPathQuery pathQuery = builder.query {
		
			PATH {
				
				value segments: segments
				
				value maxdepth: depth
				
				value rootURIs: [URIProperty.withString(inputURI)]

				if( forwardEdgeTypes.size() > 0 ) {
					
					ARC {
						
						value direction: 'forward'
						
						edge_constraint { Utils.PropertyConstraint("edgeSource").exists() }
						
/*						
						AND {
						OR {
							
							for(Class<? extends VITAL_Edge> c : forwardEdgeTypes) {
								
								edge_constraint { c }
								
							}
							
						}
						}
*/
					}
										
				}
				
				if( reverseEdgeTypes.size() > 0 ) {
					
					ARC {
					
						value direction: 'reverse'
						
						edge_constraint { Utils.PropertyConstraint("edgeSource").exists() }
						
/*
						AND {
						OR {
							for( Class<? extends VITAL_Edge> c : reverseEdgeTypes ) {
								
								edge_constraint { c }
								
							}
						}
						}
*/
						
					}
					
				}
				
			}
			
				
		}.toQuery()
		
		return pathQuery
		
		
	}
	
}
