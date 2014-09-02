/*
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

package ai.vital.cytoscape.app.internal.tabs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;

import com.hp.hpl.jena.rdf.model.ResourceFactory;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.dnd.ListElementWrapper;
import ai.vital.cytoscape.app.internal.dnd.ListTransferHandler;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;
import ai.vital.vitalservice.query.VitalPropertyConstraint;
import ai.vital.vitalservice.query.VitalPropertyConstraint.Comparator;
import ai.vital.vitalservice.query.VitalQueryContainer;
import ai.vital.vitalservice.query.VitalTypeConstraint;
import ai.vital.vitalservice.query.VitalQueryContainer.Type;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.domain.AdjectiveSynsetNode;
import ai.vital.domain.AdverbSynsetNode;
import ai.vital.domain.NounSynsetNode;
import ai.vital.domain.SynsetNode;
import ai.vital.domain.VerbSynsetNode;
import ai.vital.domain.ontology.VitalOntology;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Node;

public class SearchTab extends JPanel implements ListSelectionListener,
		ItemListener {

	private static final long serialVersionUID = 1L;
	
	private final static String MEME = "Meme Search";
	
	private final static String DOCUMENT = "Document Search";

	private JTextField textField = new JTextField("", 20);

	private JComboBox searchTypeCombo = new JComboBox();

	private JComboBox nameSpaceCombo = new JComboBox();

	private JButton searchButton;

	private JButton importButton;

	private NetworkListPanel networkListPanel = new NetworkListPanel();

	private DefaultListModel listModel = new DefaultListModel();
	private LinkedHashMap<String, VITAL_Node> entities = new LinkedHashMap<String, VITAL_Node>();
	
	private JList rsList = new JList(listModel);

	private QueryListener queryListener = new QueryListener();

	private LinkedList<VITAL_Node> lastResults = new LinkedList<VITAL_Node>();

	private KeyListener keyListener;
	
	
	private JRadioButton andRadioButton = new JRadioButton("AND");
	private JRadioButton orRadioButton = new JRadioButton("OR");
	
	public SearchTab() {
		super();

		keyListener = new KeyAdapter(){

			public void keyReleased(KeyEvent e) {
				
//				System.out.println("KEY:" + e.getKeyCode());
				
				if(e.getKeyCode() == KeyEvent.VK_ENTER) {
					searchButton.doClick();
				}
			}
			
		};		

//		searchTypeCombo.addItem("<all>");
//		searchTypeCombo.addItem("--------------");

		searchTypeCombo.addItem(MEME);
		searchTypeCombo.addItem(DOCUMENT);
		
		searchTypeCombo.addItemListener(this);
		searchTypeCombo.addKeyListener(keyListener);

		/*
		while (iterator.hasNext()) {
			String catergory = iterator.next();
			searchTypeCombo.addItem(catergory);
		}
		*/

		nameSpaceCombo.addItem("<all>");
		nameSpaceCombo.addItem("--------------");

		nameSpaceCombo.addItemListener(this);
		nameSpaceCombo.addKeyListener(keyListener);
		
		
		setLayout(new BorderLayout());

		JPanel northPanel = createPanelNorth();
		add(northPanel, BorderLayout.NORTH);

		add(getResultsPanel(), BorderLayout.CENTER);

		updateSearchButton();

		updateImportButton();

	}

	private JPanel createPanelNorth() {
		int hspace = 5;
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));

		// Create Titled Border
		TitledBorder border = new TitledBorder("Search");
		northPanel.setBorder(border);

		JPanel buttonBar = new JPanel();
		buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));

		// Create Listener
		searchButton = new JButton("Search");
		searchButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		// ExecuteQuery queryListener = new ExecuteQuery(cyMap, searchRequest,
		// searchList, consolePanel, searchButton, this);
//		buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));

		// Create Search Text Field

		Font font = textField.getFont();
		textField.setFont(new Font(font.getName(), Font.PLAIN, 11));
		textField.setToolTipText("Enter Search Term(s)");
		textField.addKeyListener(new KeyListener() {

			public void keyPressed(KeyEvent e) {
			}

			public void keyReleased(KeyEvent e) {
				updateSearchButton();

			}

			public void keyTyped(KeyEvent e) {
			}
		});
		textField.addKeyListener(keyListener);

		textField.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		textField.setMinimumSize(new Dimension(100, 50));
		buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));
		buttonBar.add(textField);
		buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));

		// // Create Organism Combo Box
		// JComboBox orgCombo = createOrganismComboBox();
		// orgCombo.setFont(new Font(font.getName(), Font.PLAIN, 11));
		// orgCombo.setToolTipText("Filter by Organism");
		// // Used to specify a default size for pull down menu
		// orgCombo.setPrototypeDisplayValue
		// (new String("Saccharomyces cerevisiae"));
		// orgCombo.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		// buttonBar.add(orgCombo);
		// buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));

		// // Create Result Limit Combo Box
		// JComboBox limitCombo = createResultLimitComboBox();
		// limitCombo.setFont(new Font(font.getName(), Font.PLAIN, 11));
		// limitCombo.setToolTipText("Limit Result Set or Get All");
		// limitCombo.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		//
		// // Used to specify a default size for pull down menu;
		// // Particularly important for Windows, see Bug #520.
		// limitCombo.setPrototypeDisplayValue
		// (new String("Get All --- Get All"));
		// buttonBar.add(limitCombo);
		// buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));

		// Create Search Button
		searchButton.setToolTipText("Execute Search Query");
		searchButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		buttonBar.add(searchButton);
		buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));
		searchButton.addActionListener(queryListener);

		// JButton helpButton = new JButton("Help");
		// helpButton.setToolTipText("View Quick Reference Manual");
		// helpButton.addActionListener(new QuickReferenceDialog((JFrame)
		// this));
		// helpButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		// buttonBar.add(helpButton);
		// buttonBar.add(Box.createRigidArea(new Dimension(hspace, 0)));
		//
		// JButton aboutButton = new JButton("About");
		// aboutButton.setToolTipText("About the PlugIn");
		// aboutButton.addActionListener(new AboutDialog((JFrame) this));
		// aboutButton.setAlignmentY(Component.BOTTOM_ALIGNMENT);
		// buttonBar.add(aboutButton);
		// buttonBar.add(Box.createHorizontalGlue());

		northPanel.add(buttonBar);
		
		ButtonGroup logicalGroup = new ButtonGroup();
		logicalGroup.add(orRadioButton);
		logicalGroup.add(andRadioButton);
		orRadioButton.setSelected(true);
		JPanel logicalRadiosPanel = new JPanel();
		logicalRadiosPanel.setLayout(new BoxLayout(logicalRadiosPanel, BoxLayout.X_AXIS));
		logicalRadiosPanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		logicalRadiosPanel.add(orRadioButton);
		logicalRadiosPanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		logicalRadiosPanel.add(andRadioButton);
		logicalRadiosPanel.add(Box.createRigidArea(new Dimension(hspace, 0)));

		northPanel.add(logicalRadiosPanel);
		
		JPanel categoryTypePanel = new JPanel();
		categoryTypePanel.setLayout(new BoxLayout(categoryTypePanel, BoxLayout.X_AXIS));
		categoryTypePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		categoryTypePanel.add(new JLabel("search type:"));
		categoryTypePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		categoryTypePanel.add(searchTypeCombo);
		categoryTypePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));

//		northPanel.add(categoryTypePanel);

		JPanel namespacePanel = new JPanel();
		namespacePanel.setLayout(new BoxLayout(namespacePanel, BoxLayout.X_AXIS));
		namespacePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		namespacePanel.add(new JLabel("namespace:"));
		namespacePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		namespacePanel.add(nameSpaceCombo);
		namespacePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));

//		northPanel.add(namespacePanel);

		return northPanel;
	}

	// /**
	// * Show Error Message.
	// *
	// * @param exception Exception.
	// */
	// private void showError (Throwable exception) {
	// ErrorDisplay errorDisplay = new ErrorDisplay(this);
	// errorDisplay.displayError(exception, consolePanel);
	// }

	public void updateSearchButton() {
		searchButton.setEnabled(validateForm());
	}

	private boolean validateForm() {

		if (textField.getText() == null || textField.getText().equals("")) {
			return false;
		}

		return true;

	}

	private boolean validateResultsForm() {

		if (rsList.getSelectedIndex() < 0) {

			return false;

		}
		// else {
		// int[] selectedIndices = rsList.getSelectedIndices();
		// for(int i = 0 ; i < selectedIndices.length ; i++) {
		// int selectedIndex = selectedIndices[i];
		//    			
		// rsList.get
		//    			
		// }
		// }

		return true;

	}

	private class QueryListener implements ActionListener {

		public void actionPerformed(ActionEvent e) {
			if (searchButton.isEnabled()) {

				listModel.clear();

				lastResults.clear();

				entities.clear();

//				ConsolePanel.log("\nPerforming query...");
				
				VitalSelectQuery sq = new VitalSelectQuery();
//				Type.or, "wordnet", 0, 100
				sq.setLimit(100);
				sq.setOffset(0);
				sq.setProjectionOnly(false);
				List<VitalSegment> segments = new ArrayList<VitalSegment>();
				segments.add(Application.get().getWordnetSegment());
				
				sq.setSegments(segments);
				sq.setType(Type.or);
				
				String searchString = textField.getText();
				
				String[] split = searchString.split("\\s+");
				
				Class[] types = new Class[]{
					AdjectiveSynsetNode.class, AdverbSynsetNode.class,
					NounSynsetNode.class, VerbSynsetNode.class
				};

				//main containers
				List<VitalQueryContainer> keywordsContainers = new ArrayList<VitalQueryContainer>();
				
				for(Class cls : types) {
					
					VitalQueryContainer qc = new VitalQueryContainer();
					qc.setType(Type.and);
					
					String rdfType = //VitalSigns.get().getRDFClass(cls);
							VitalOntology.NS + cls.getSimpleName();
					
					System.out.println(cls.getSimpleName() + " -> " + rdfType);
					
					qc.getComponents().add(new VitalTypeConstraint(cls));
					
					VitalQueryContainer keywords = new VitalQueryContainer();
//					orRadioButton.isSelected() ? Type.or : Type.and
					keywords.setType(orRadioButton.isSelected() ? Type.or : Type.and);
					
					qc.getComponents().add(keywords);
					
					keywordsContainers.add(keywords);
					
					sq.getComponents().add(qc);
					
				}
				
				if(searchString.length() > 0) {
					
					//extra scores for exact match
					for(Class cls : types) {
						
						VitalQueryContainer qc = new VitalQueryContainer();
						qc.setType(Type.and);
						
						String rdfType = VitalOntology.NS + cls.getSimpleName();
						
						qc.getComponents().add(new VitalTypeConstraint(cls));
						
						String q = searchString.toLowerCase().replaceAll("\\s+", " ").trim();
						
						qc.getComponents().add(new VitalPropertyConstraint(VitalOntology.NS + "hasName", q, Comparator.EQ_CASE_INSENSITIVE, false));
						
						sq.getComponents().add(qc);
						
					}
					
				}

				for(VitalQueryContainer keywordsC : keywordsContainers) {
					
					for(int i = 0 ; i < split.length; i ++) {
						
						String chunk = split[i];
						
						if(!chunk.trim().equals("")) {
							
							keywordsC.getComponents().add(new VitalPropertyConstraint(VitalOntology.NS + "hasName", chunk, Comparator.CONTAINS_CASE_INSENSITIVE, false));
							
						}
						
					}
					
				}

				ResultList rs = null;
				try {
					rs = Application.get().search(sq);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
					throw new RuntimeException(e1);
				}
//				if (!rs.isOk()) {
//					ConsolePanel.logLine("Error occured: " + rs.getStatusCode());
//				}

//				ConsolePanel.log("done!\n");

//				ArrayList<Object> objects = rs.getObjects();

//				ConsolePanel.logLine("Found " + objects.size() + " results:");

//				ResultsSorter.sortRankedResults(objects);
	
				for(ResultElement r : rs.getResults()) {
					
					if(r.getGraphObject() instanceof VITAL_Node) {
						
						VITAL_Node n = (VITAL_Node) r.getGraphObject();
						
						lastResults.add(n);
						
					}
					
				}
				
				for (int i = 0; i < lastResults.size(); i++) {
					VITAL_Node entity = lastResults.get(i);
					ListElementWrapper element = new ListElementWrapper(entity, null, null);
					listModel.addElement(element);
					entities.put(entity.getURI(), entity);
				}

			}
		}

	}

	private JPanel getResultsPanel() {
		JPanel rsPanel = new JPanel();

		rsPanel.setLayout(new BorderLayout());

		JScrollPane scrollPanel = new JScrollPane(rsList);

		rsPanel.setBorder(new TitledBorder("Results"));

		rsList.addListSelectionListener(this);

		rsList.setDragEnabled(true);
		
		rsList.setTransferHandler(new ListTransferHandler(rsList));
		
//				new TransferHandler(){
//			protected Transferable createTransferable(JComponent c) {
//				JList list = (JList) c;
//				int[] selectedIndices = list.getSelectedIndices();
//				ASAPI_Entity[] entities = new ASAPI_Entity[selectedIndices.length];
//
//				for (int i = 0; i < selectedIndices.length; i++) {
//					entities[i] = ((ListElementWrapper)listModel
//							.get(selectedIndices[i])).getEntity();
//				}
//				return new MemomicsTransferable(entities);
//			}
//			
//			@Override
//			public int getSourceActions(JComponent c) {
//				return COPY;
//			}
//		});
		
		rsPanel.add(scrollPanel, BorderLayout.CENTER);

		JPanel controlBars = new JPanel();

		controlBars.setLayout(new BoxLayout(controlBars, BoxLayout.Y_AXIS));

		JPanel buttonBar = new JPanel();

		buttonBar.setLayout(new BoxLayout(buttonBar, BoxLayout.X_AXIS));

		importButton = new JButton("Import");

		importButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				if (importButton.isEnabled()) {

					int[] selectedIndices = rsList.getSelectedIndices();
					GraphObject[] entities = new GraphObject[selectedIndices.length];

					for (int i = 0; i < selectedIndices.length; i++) {
						entities[i] = ((ListElementWrapper)listModel
								.get(selectedIndices[i])).getEntity();
					}

					CyNetwork cyNetwork = networkListPanel.getSelectedNetwork();

					Utils.placeNodesInTheNetwork(cyNetwork, entities);

					// Utils.applyVisualStyle(cyNetwork);

					// if(newNetworkMenu) {
					// Cytoscape.getNetworkView(cyNetwork.getIdentifier()).addNodeContextMenuListener(MemomicsCytoscapePlugin.nodeMenuListener);
					// Utils.applyVisualStyle(cyNetwork);
					// }

					CyNetworkView networkView = VitalAICytoscapePlugin.getNetworkView(cyNetwork);
					
					VitalAICytoscapePlugin.getEventHelper().flushPayloadEvents();
					
					if (networkView != null) {

						// Utils.defaultLayout.doLayout(networkView);
						// networkView.applyLayout(Utils.defaultLayout);

//						networkView.updateView();
						
						System.out.println("Updating network view...");
						VisualStyleUtils.applyVisualStyle(networkView);

					}

				}

			}
		});

		JButton selectAllButton = new JButton(" all ");
		selectAllButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				int size = listModel.getSize();

				int[] selection = new int[size];

				for (int i = 0; i < size; i++) {
					selection[i] = i;
				}
				rsList.setSelectedIndices(selection);

			}
		});

		JButton selectNoneButton = new JButton("none");
		selectNoneButton.addActionListener(new ActionListener() {

			public void actionPerformed(ActionEvent e) {

				rsList.clearSelection();

			}
		});

		buttonBar.add(new JLabel("select: "));
		buttonBar.add(Box.createRigidArea(new Dimension(3, 0)));
		buttonBar.add(selectAllButton);
		buttonBar.add(Box.createRigidArea(new Dimension(3, 0)));
		buttonBar.add(selectNoneButton);
		// buttonBar.add(Box.createRigidArea(new Dimension(30, 0)));

		JPanel importBar = new JPanel();
		importBar.setLayout(new BoxLayout(importBar, BoxLayout.X_AXIS));
		importBar.add(new JLabel("target:"));
		importBar.add(Box.createRigidArea(new Dimension(3, 0)));
		importBar.add(networkListPanel);

		importBar.add(Box.createRigidArea(new Dimension(10, 0)));

		importBar.add(importButton);

		controlBars.add(buttonBar);
		controlBars.add(importBar);

		rsPanel.add(controlBars, BorderLayout.SOUTH);

		return rsPanel;

	}

	public void valueChanged(ListSelectionEvent e) {
		updateImportButton();
	}

	public void updateImportButton() {

		importButton.setEnabled(validateResultsForm());

	}

	public VITAL_Node getEntity(String umis) {
		return entities.get(umis);
	}

	public void itemStateChanged(ItemEvent e) {
		if(e.getSource() == nameSpaceCombo) {
			int selectedIndex = nameSpaceCombo.getSelectedIndex();
			if (selectedIndex == 1) {
				nameSpaceCombo.setSelectedIndex(0);
			}
		}
		
		/*

		selectedIndex = searchTypeCombo.getSelectedIndex();
		if (selectedIndex == 1) {
			searchTypeCombo.setSelectedIndex(0);
		}
		 */
	}


}
