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

import ai.vital.cytoscape.app.internal.CyActivator;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Node;


public class VitalTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;

	public boolean canImport(JComponent comp, DataFlavor[] flavors) {
		
		System.out.println("CAN IMPORT launched");
		
		for (int i = 0; i < flavors.length; i++) {
			System.out.print("\nChecking flavor #"+i+" " + flavors[i].getRepresentationClass() +"...");
			if (flavors[i].equals(VitalEntityFlavor.flavor)) {
				System.out.print("OK\n");
				return true;
			}
			System.out.print("NOPE\n");
		}

		return false;
	}
	
	public boolean importData(JComponent comp, Transferable t) {
		
		System.out.println("IMPORT DATA launched");
		
		System.out.println("Transferable : " + t);
		
		LinkedList<GraphObject> acquiredEntites = new LinkedList<GraphObject>();
				
		int length = t.getTransferDataFlavors().length;
		for(int i  = 0 ; i < length ; i ++) {
			DataFlavor dataFlavor = t.getTransferDataFlavors()[i];
			try {
				System.out.println("Flavor #"+i+ " " +dataFlavor +
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
							System.out.println("Aqcuired entity: " + entites[j]);
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
			System.out.println("No entity was acquired !!!");
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
