package ai.vital.cytoscape.app.internal.dnd;

import ai.vital.domain.SynsetNode;
import ai.vital.vitalsigns.model.VITAL_Node;

public class ListElementWrapper {
	
	protected VITAL_Node entity;

	private String specialLabel;
	
	protected String parentEdgeTypeUMIS;
	
	public String getParentEdgeTypeUMIS() {
		return parentEdgeTypeUMIS;
	}

	public void setParentEdgeTypeUMIS(String parentEdgeTypeUMIS) {
		this.parentEdgeTypeUMIS = parentEdgeTypeUMIS;
	}

//	public ListElementWrapper(ASAPI_Entity entity) {
//		this(entity,null);
//	}
	
	public ListElementWrapper(VITAL_Node entity, String parentEdgeTypeID, String specialLabel) {
		super();
		this.entity = entity;
		this.parentEdgeTypeUMIS = parentEdgeTypeID;
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
//		} else if(entity instanceof SynsetNode) {
//			return (String) entity.getProperty("name");
		} else {
			String s = (String) entity.getProperty("name");
			if(s == null || s.isEmpty()) {
				s = "(no label)";
			}
			return s;
		}
	}
	
	
}