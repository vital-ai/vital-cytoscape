package ai.vital.cytoscape.app.internal.dnd;

import java.awt.datatransfer.DataFlavor;

import ai.vital.vitalsigns.model.GraphObject;

public class VitalEntityFlavor {

	public static final DataFlavor flavor = new DataFlavor(GraphObject.class,"Entity");
	
	
}
