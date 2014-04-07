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

import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import ai.vital.vitalsigns.model.GraphObject;

public class ListTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	

	public ListTransferHandler(JList list) {
		super();
	}

	protected Transferable createTransferable(JComponent c) {
		JList list = (JList) c;
		DefaultListModel listModel = (DefaultListModel) list.getModel();
		int[] selectedIndices = list.getSelectedIndices();
		GraphObject[] entities = new GraphObject[selectedIndices.length];

		for (int i = 0; i < selectedIndices.length; i++) {
			entities[i] = ((ListElementWrapper) listModel
					.get(selectedIndices[i])).getEntity();
		}
		return new VitalTransferable(entities);
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY;
	}
}
