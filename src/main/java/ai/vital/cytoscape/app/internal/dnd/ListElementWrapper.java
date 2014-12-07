package ai.vital.cytoscape.app.internal.dnd;

import ai.vital.vitalsigns.model.VITAL_Node;

public class ListElementWrapper {
	
	protected VITAL_Node entity;

	private String specialLabel;
	
	protected String parentEdgeTypeURI;
	
	public String getParentEdgeTypeURI() {
		return parentEdgeTypeURI;
	}

	public void setParentEdgeTypeURI(String parentEdgeTypeURI) {
		this.parentEdgeTypeURI = parentEdgeTypeURI;
	}

	
	public ListElementWrapper(VITAL_Node entity, String parentEdgeTypeID, String specialLabel) {
		super();
		this.entity = entity;
		this.parentEdgeTypeURI = parentEdgeTypeID;
		this.specialLabel = specialLabel;
	}

	public void setSpecialLabel(String specialLabel) {
		this.specialLabel = specialLabel;
	}
	
	public VITAL_Node getEntity() {
		return entity;
	}
	
	@Override
	public String toString() {
		if(specialLabel != null) {
			return specialLabel;


		} else {
			String s = (String) entity.getProperty("name");
			if(s == null || s.isEmpty()) {
				s = "(no label)";
			}
			return s;
		}
	}
	
	
}