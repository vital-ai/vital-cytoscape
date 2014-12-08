package ai.vital.cytoscape.app.internal.dnd;

import java.awt.datatransfer.Transferable;

import javax.swing.DefaultListModel;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.TransferHandler;

import ai.vital.vitalsigns.model.GraphObject;

public class ListTransferHandler extends TransferHandler {

	private static final long serialVersionUID = 1L;
	

	@SuppressWarnings("rawtypes")
	public ListTransferHandler(JList list) {
		super();
	}

	@SuppressWarnings("rawtypes")
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
