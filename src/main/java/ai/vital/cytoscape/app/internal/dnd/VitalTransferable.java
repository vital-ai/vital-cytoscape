package ai.vital.cytoscape.app.internal.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import ai.vital.vitalsigns.model.GraphObject;


public class VitalTransferable implements Transferable {

	private GraphObject[] graphObjects;
	
    public VitalTransferable(GraphObject[] objects) {
    	this.graphObjects = objects;
	}

	public DataFlavor[] getTransferDataFlavors() {
    	return new DataFlavor[]{VitalEntityFlavor.flavor};
    }

    public boolean isDataFlavorSupported(DataFlavor flavor) {
    	
    	if(flavor.equals(VitalEntityFlavor.flavor)) {
	        return true;
	    }
    	
    	return false;
    }

    public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException
    {

    	if (flavor.equals(VitalEntityFlavor.flavor)) {
    		return graphObjects;
    	} else {
    		throw new UnsupportedFlavorException(flavor);
    	}
    }

}
