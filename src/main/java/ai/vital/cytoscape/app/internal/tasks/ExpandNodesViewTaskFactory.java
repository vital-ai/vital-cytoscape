package ai.vital.cytoscape.app.internal.tasks;

import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class ExpandNodesViewTaskFactory extends AbstractNodeViewTaskFactory {

	@Override
	public TaskIterator createTaskIterator(View<CyNode> arg0, CyNetworkView arg1) {

		TaskIterator ti = new TaskIterator(
				new ExpandNodesTask(arg0, ExpandNodesTask.NORMAL)
		);
		
		return ti;
	}

}
