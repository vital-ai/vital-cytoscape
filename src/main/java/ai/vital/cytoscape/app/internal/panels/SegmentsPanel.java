package ai.vital.cytoscape.app.internal.panels;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.DefaultListModel;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import ai.vital.vitalsigns.model.VitalSegment;
import ai.vital.vitalsigns.model.properties.Property_hasSegmentID;

public class SegmentsPanel extends JPanel {

	private static final long serialVersionUID = 1709502351370538946L;

	private JList<CheckListItem> list; 
	
	private DefaultListModel<CheckListItem> model;
	
	public SegmentsPanel() {
		
		super();
		
		this.setBorder(new TitledBorder("Segments"));
		
		model = new DefaultListModel<SegmentsPanel.CheckListItem>();
		list = new JList<SegmentsPanel.CheckListItem>(model);
		list.setCellRenderer(new CheckListRenderer());
		list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		list.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent event) {
				@SuppressWarnings("unchecked")
				JList<CheckListItem> list = (JList<CheckListItem>) event.getSource();

				// Get index of item clicked

				int index = list.locationToIndex(event.getPoint());
				CheckListItem item = (CheckListItem) list.getModel()
						.getElementAt(index);

				// Toggle selected state

				item.setSelected(!item.isSelected());

				// Repaint cell

				list.repaint(list.getCellBounds(index, index));
			}
		});

		JScrollPane scrollPane = new JScrollPane(list);
		
        setLayout(new BorderLayout());
        
        add(scrollPane, BorderLayout.CENTER);
        
	}
	
	public void setSegmentsList(List<VitalSegment> segments) {
		
		model.removeAllElements();
		
		for(VitalSegment segment : segments) {
			
			model.addElement(new CheckListItem(segment, true));
			
		}
	}		

	public static void main(String args[]) throws InterruptedException {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		SegmentsPanel panel = new SegmentsPanel();
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		
		Thread.sleep(3000);
		
		panel.setSegmentsList(Arrays.asList(
			VitalSegment.withId("s1"),
			VitalSegment.withId("s2"),
			VitalSegment.withId("s3")
		));
		
	}

	// Handles rendering cells in the list using a check box

	static class CheckListRenderer extends JCheckBox implements
			ListCellRenderer<CheckListItem> {

		private static final long serialVersionUID = 4750202362196988313L;

		public Component getListCellRendererComponent(JList<? extends CheckListItem> list, CheckListItem value,
				int index, boolean isSelected, boolean hasFocus) {
			setEnabled(list.isEnabled());
			setSelected(((CheckListItem) value).isSelected());
			setFont(list.getFont());
			setBackground(list.getBackground());
			setForeground(list.getForeground());
			setText(value.toString());
			return this;
		}
	}

	static class CheckListItem {
		private VitalSegment segment;
		private boolean isSelected = false;

		public CheckListItem(VitalSegment segment, boolean selected) {
			this.segment = segment;
			this.isSelected = selected;
		}

		public boolean isSelected() {
			return isSelected;
		}

		public void setSelected(boolean isSelected) {
			this.isSelected = isSelected;
		}

		public String toString() {
			return "" + segment.get(Property_hasSegmentID.class);
		}

		public VitalSegment getSegment() {
			return segment;
		}
		
		
	}

	public List<VitalSegment> getSelectedSegments() {
		List<VitalSegment> l = new ArrayList<VitalSegment>();
		for(int i = 0 ; i < model.size(); i++) {
			CheckListItem el = model.elementAt(i);
			if(el.isSelected()) {
				l.add(el.getSegment());
			}
		}
		return l;
	}

}
