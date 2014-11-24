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
import java.util.LinkedList;

import javax.swing.JComponent;
import javax.swing.TransferHandler;

import org.cytoscape.model.CyNetwork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.vitalsigns.model.GraphObject;


public class VitalTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	private final static Logger log = LoggerFactory.getLogger(VitalTransferHandler.class);
	
	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		
		log.debug("CAN IMPORT launched");
		
		for (int i = 0; i < flavors.length; i++) {
			log.debug("Checking flavor #"+i+" " + flavors[i].getRepresentationClass() +"...");
			if (flavors[i].equals(VitalEntityFlavor.flavor)) {
				log.debug("OK\n");
				return true;
			}
			log.debug("NOPE\n");
		}

		return false;
	}
	
	public boolean importData(JComponent comp, Transferable t) {
		
		log.debug("IMPORT DATA launched");
		
		log.debug("Transferable : " + t);
		
		LinkedList<GraphObject> acquiredEntites = new LinkedList<GraphObject>();
				
		int length = t.getTransferDataFlavors().length;
		for(int i  = 0 ; i < length ; i ++) {
			DataFlavor dataFlavor = t.getTransferDataFlavors()[i];
			try {
				log.debug("Flavor #"+i+ " " +dataFlavor +
						" mimeType: " + dataFlavor.getMimeType() + 
						"\n\t\ttransferData ="+  t.getTransferData(dataFlavor));
				
			} catch (UnsupportedFlavorException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(dataFlavor.equals(VitalEntityFlavor.flavor)) {
				try {
					Object transferData = t.getTransferData(dataFlavor);
					if(transferData instanceof GraphObject[]) {
						GraphObject[] entites = (GraphObject[]) transferData;
						for(int j = 0 ; j < entites.length; j ++) {
							log.debug("Aqcuired entity: " + entites[j]);
							acquiredEntites.add((GraphObject) entites[j]);
						}
					}
					
					
				} catch (UnsupportedFlavorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			
		}
		
		if(acquiredEntites.size()== 0) {
			log.debug("No entity was acquired !!!");
			return false;
		}

		CyNetwork cyNetwork = VitalAICytoscapePlugin.getCurrentNetwork();
		
		if(cyNetwork == null) {
			return false;
		}
		
		Utils.placeNodesInTheNetwork(cyNetwork, acquiredEntites);
		
		return true;
	}
	
}
