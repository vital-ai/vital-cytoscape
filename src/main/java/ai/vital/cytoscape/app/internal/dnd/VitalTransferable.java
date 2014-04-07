/*
 * Copyright 2008 Alitora Systems
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use these files except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and limitations under the License.
 */

package ai.vital.cytoscape.app.internal.dnd;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;

import ai.vital.vitalsigns.model.GraphObject;


public class VitalTransferable implements Transferable {

//    private ASAPI_Entity entity;
						   
//    public MemomicsTransferable(ASAPI_Entity entity) {
//        this.entity = entity;
//    }

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
