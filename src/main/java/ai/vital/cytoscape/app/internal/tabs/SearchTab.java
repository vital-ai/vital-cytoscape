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
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.dnd.ListElementWrapper;
import ai.vital.cytoscape.app.internal.dnd.ListTransferHandler;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;
import ai.vital.cytoscape.app.internal.panels.SegmentsPanel;
import ai.vital.cytoscape.app.internal.queries.Queries;
import ai.vital.vitalservice.exception.VitalServiceException;
import ai.vital.vitalservice.exception.VitalServiceUnimplementedException;
import ai.vital.vitalservice.factory.VitalServiceFactory;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalservice.query.VitalSelectQuery;
import ai.vital.vitalservice.segment.VitalSegment;
import ai.vital.vitalsigns.VitalSigns;
import ai.vital.vitalsigns.classes.ClassMetadata;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.VITAL_Node;
import ai.vital.vitalsigns.model.property.IProperty;
import ai.vital.vitalsigns.ontology.VitalCoreOntology;
import ai.vital.vitalsigns.properties.PropertiesRegistry;
import ai.vital.vitalsigns.properties.PropertyMetadata;

public class SearchTab extends JPanel implements ListSelectionListener,
		ItemListener {

	private static final long serialVersionUID = 1L;
	
	private final static Logger log = LoggerFactory.getLogger(SearchTab.class);
	
	
	//TODO cleanup these, remove "meme" stuff
	private final static String MEME = "Meme Search";
	
	private final static String DOCUMENT = "Document Search";

	private JTextField textField = new JTextField("", 20);

	@SuppressWarnings("rawtypes")
	private JComboBox searchTypeCombo = new JComboBox();

	@SuppressWarnings("rawtypes")
	private JComboBox nameSpaceCombo = new JComboBox();

	private JButton searchButton;

	private JButton importButton;

	private NetworkListPanel networkListPanel = new NetworkListPanel(true);

	@SuppressWarnings("rawtypes")
	private DefaultListModel listModel = new DefaultListModel();
	private LinkedHashMap<String, VITAL_Node> entities = new LinkedHashMap<String, VITAL_Node>();
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private JList rsList = new JList(listModel);

	private QueryListener queryListener = new QueryListener();

	private LinkedList<VITAL_Node> lastResults = new LinkedList<VITAL_Node>();

	private KeyListener keyListener;
	
	
	private JRadioButton andRadioButton = new JRadioButton("AND");
	private JRadioButton orRadioButton = new JRadioButton("OR");

	private SegmentsPanel segmentsPanel = new SegmentsPanel();
	
	private JComboBox<PropertyItem> propertiesBox;
	
	@SuppressWarnings("unchecked")
	public SearchTab() {
		super();

		keyListener = new KeyAdapter(){

			public void keyReleased(KeyEvent e) {
				
//				log.debug("KEY:" + e.getKeyCode());
				
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
		
		
		
		propertiesBox = new JComboBox<PropertyItem>();
		
//		initProprtiesBox();
		initPropertiesBox2();
		
		
		setLayout(new BorderLayout());

		JPanel northPanel = createPanelNorth();
		add(northPanel, BorderLayout.NORTH);

		add(getResultsPanel(), BorderLayout.CENTER);

		updateSearchButton();

		updateImportButton();

	}

//	final static Set<Class<? extends IProperty>> searchableClasses = new HashSet<Class<? extends IProperty>>(Arrays.asList(
//		(Class<? extends IProperty>) StringProperty.class,
//		URIProperty.class,
//		DoubleProperty.class,
//		IntegerProperty.class,
//		FloatProperty.class,
//		LongProperty.class
//	));
	
	private void initPropertiesBox2() {
		
		List<PropertyItem> pItems = new ArrayList<PropertyItem>();
		
		PropertiesRegistry propertiesRegistry = VitalSigns.get().getPropertiesRegistry();
		
		List<ClassMetadata> allClasses = VitalSigns.get().getClassesRegistry().listAllClasses();
		
		Set<String> pURIs = new HashSet<String>();
		
		for(ClassMetadata cm : allClasses) {
			
			if( ! VITAL_Node.class.isAssignableFrom( cm.getClazz() ) ) continue;
			
			List<PropertyMetadata> classProperties = propertiesRegistry.getClassProperties(cm.getClazz());
		
			for(PropertyMetadata pm : classProperties) {
				
				if(pURIs.contains( pm.getURI() ) ) continue;
				
//				if(searchableClasses.contains(pm.getBaseClass())) continue;
//				if( ! StringProperty.class.equals( pm.getBaseClass() ) ) continue;
				
				pItems.add(new PropertyItem(pm.getShortName(), pm.getURI(), pm));
				
				pURIs.add(pm.getURI());
				
			}
			
		}
		
		Collections.sort(pItems, new java.util.Comparator<PropertyItem>(){

			@Override
			public int compare(PropertyItem arg0, PropertyItem arg1) {
				int c = arg0.propertyName.compareToIgnoreCase(arg1.propertyName);
				if(c != 0) return c;
				return arg0.propertyURI.compareToIgnoreCase(arg1.propertyURI);
			}});
		
		
		PropertyItem nameItem = null;
		
		for(PropertyItem p : pItems) {
			propertiesBox.addItem(p);
			if((VitalCoreOntology.NS + "hasName").equals(p.propertyURI)) {
				nameItem = p;
			}
		}
		
		if(nameItem != null) {
			propertiesBox.setSelectedItem(nameItem);
		}
		
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

		
		
		JPanel propertyPanel = new JPanel();
		propertyPanel.setLayout(new BoxLayout(propertyPanel, BoxLayout.X_AXIS));
		propertyPanel.add(Box.createRigidArea(new Dimension(10, 1)));
		propertyPanel.add(new JLabel("Property:"));
		propertyPanel.add(Box.createRigidArea(new Dimension(10, 1)));
		propertiesBox.setPreferredSize(new Dimension(120, 30));
		propertyPanel.add(propertiesBox);
		
		northPanel.add(propertyPanel);
		
		
		JPanel namespacePanel = new JPanel();
		namespacePanel.setLayout(new BoxLayout(namespacePanel, BoxLayout.X_AXIS));
		namespacePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		namespacePanel.add(new JLabel("namespace:"));
		namespacePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		namespacePanel.add(nameSpaceCombo);
		namespacePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));

//		northPanel.add(namespacePanel);
		segmentsPanel.setPreferredSize(new Dimension(0, 100));
		northPanel.add(segmentsPanel);

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

		@SuppressWarnings("unchecked")
		public void actionPerformed(ActionEvent e) {
			if (searchButton.isEnabled()) {

				List<VitalSegment> segments = segmentsPanel.getSelectedSegments();
				
				if(segments.size() < 1) {
					JOptionPane.showMessageDialog(null, "No segments selected - at least one required to perform search", "No segments selected", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				listModel.clear();

				lastResults.clear();

				entities.clear();

				
//				ConsolePanel.log("\nPerforming query...");
				String searchString = textField.getText().trim();
				String propertyURI = ((PropertyItem)propertiesBox.getSelectedItem()).propertyURI;
				
				PropertyMetadata pm = VitalSigns.get().getPropertiesRegistry().getProperty(propertyURI);
				if(pm == null) {
					JOptionPane.showMessageDialog(null, "Property not found: " + propertyURI, "Property not found", JOptionPane.ERROR_MESSAGE);
					return;
				}
				
				VitalSelectQuery sq = null;
				
				try {
					sq = Queries.searchQuery(segments, searchString, 0, 1000, pm, orRadioButton.isSelected());
				} catch(Exception ex) {
					JOptionPane.showMessageDialog(null, ex.getLocalizedMessage(), "Query generation error", JOptionPane.ERROR_MESSAGE);
					return;					
				}
				
				/*
				VitalSelectQuery sq = VitalSelectQuery.createInstance();
//				Type.or, "wordnet", 0, 100
				sq.setLimit(100);
				sq.setOffset(0);
				sq.setProjectionOnly(false);
				sq.setSegments(segments);
				
				VitalGraphCriteriaContainer cc = sq.getCriteriaContainer();
				cc.setType(QueryContainerType.or);

				
				
				String[] split = searchString.split("\\s+");
				
				
				
//				VitalSigns.get().getProperty(URIProperty)
				
				//main containers
				List<VitalGraphCriteriaContainer> keywordsContainers = new ArrayList<VitalGraphCriteriaContainer>();
				
//				for(Class cls : types) {
					
//					VitalQueryContainer qc = new VitalQueryContainer();
//					qc.setType(Type.and);
					
//					String rdfType = //VitalSigns.get().getRDFClass(cls);
//							VitalOntology.NS + cls.getSimpleName();
					
//					log.debug(cls.getSimpleName() + " -> " + rdfType);
					
//					qc.getComponents().add(new VitalTypeConstraint(cls));
					
					VitalGraphCriteriaContainer keywords = new VitalGraphCriteriaContainer();
//					orRadioButton.isSelected() ? Type.or : Type.and
					
					keywords.setType(orRadioButton.isSelected() ? QueryContainerType.or : QueryContainerType.and);
					
//					qc.getComponents().add(keywords);
					
					keywordsContainers.add(keywords);
					
//					sq.getComponents().add(qc);
					
					cc.add(keywords);
					
//				}
				
				if(searchString.length() > 0) {
					
					//extra scores for exact match
//					for(Class cls : types) {
						
//						VitalQueryContainer qc = new VitalQueryContainer();
//						qc.setType(Type.and);
						
//						String rdfType = VitalOntology.NS + cls.getSimpleName();
						
//						qc.getComponents().add(new VitalTypeConstraint(cls));
						
						String q = searchString.toLowerCase().replaceAll("\\s+", " ").trim();
						
						
//						sq.getComponents().add(qc);
						
						VitalGraphQueryPropertyCriterion c = new VitalGraphQueryPropertyCriterion(propertyURI);
						c.setComparator(Comparator.EQ_CASE_INSENSITIVE);
						c.setValue(q);
						
						cc.add(c);
						
//						sq.getComponents().add(new VitalPropertyConstraint(propertyURI, q, Comparator.EQ_CASE_INSENSITIVE, false));
						
//					}
					
				}

				for(VitalGraphCriteriaContainer keywordsC : keywordsContainers) {
					
					for(int i = 0 ; i < split.length; i ++) {
						
						String chunk = split[i];
						
						if(!chunk.trim().equals("")) {
							
							VitalGraphQueryPropertyCriterion c = new VitalGraphQueryPropertyCriterion(propertyURI);
							c.setComparator(Comparator.CONTAINS_CASE_INSENSITIVE);
							c.setValue(chunk);
							keywordsC.add(c);
//							keywordsC.getComponents().add(new VitalPropertyConstraint(propertyURI, chunk, Comparator.CONTAINS_CASE_INSENSITIVE, false));
							
						}
						
					}
					
				}
				*/
				
				Set<String> uris = new HashSet<String>();
				
				List<String> nsList = new ArrayList<String>(VitalSigns.get().getOntologyURI2ImportsTree().keySet());
				
//				for(String ns : VitalSigns.get().getOntologyURI2Segment().keySet()) {
					
					ResultList rsx = VitalSigns.get().query(sq, nsList);
					
					for(ResultElement r : rsx.getResults()) {
						GraphObject g = r.getGraphObject();
						if(g instanceof VITAL_Node && uris.add(g.getURI())) {
							lastResults.add((VITAL_Node) g);
						}
					}
					
//				}
				
				
				sq.setSegments(segments);
				
				
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
						if(uris.add(n.getURI())) {
							lastResults.add(n);
						}
						
					}
					
				}
				
				for (int i = 0; i < lastResults.size(); i++) {
					VITAL_Node entity = lastResults.get(i);
					ListElementWrapper element = new ListElementWrapper(entity, null, null);
					
					IProperty prop = (IProperty) entity.getProperty(((PropertyItem)propertiesBox.getSelectedItem()).propertyName);
					if(prop != null) {
						element.setSpecialLabel(prop.rawValue().toString());
					}
					
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
						
						log.debug("Updating network view...");
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

	public VITAL_Node getEntity(String URI) {
		return entities.get(URI);
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

	public SegmentsPanel getSegmentsPanel() {
		return segmentsPanel;
	}
	
	

	public static void main(String[] args) throws VitalServiceException, VitalServiceUnimplementedException {
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Application.initForTests(VitalServiceFactory.getVitalService());

		SearchTab panel = new SearchTab();

		for(int i = 0 ; i < panel.propertiesBox.getModel().getSize(); i++) {
			
			PropertyItem elementAt = panel.propertiesBox.getModel().getElementAt(i);
			
			System.out.println(elementAt.propertyURI + "\t" + elementAt.propertyName);
			
		}
		
		if(true)return;
		
		
		panel.getSegmentsPanel().setSegmentsList(Application.get().getServiceSegments());
		
		panel.setSize(400, 400);

		frame.setMinimumSize(new Dimension(800, 600));
		frame.setSize(800, 600);
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		

//		ResultList rl = Application.get().getConnections("http://vital.ai/customer/app/CorporateDivision/1417795451415_913912318", "http://vital.ai/ontology/bloomberg-compliance#CorporateDivision");
//		ResultList rl = Application.get().getConnections("http://vital.ai/customer/app/CorporateOrganization/1417795451226_913912317", "http://vital.ai/ontology/bloomberg-compliance#CorporateOrganization");
		
		ResultList rl = Application.get().getConnections("http://vital.ai/customer/app/CorporateStaffMember/1417795624138_913917224", "http://vital.ai/ontology/bloomberg-compliance#CorporateStaffMember", 0, 1000);
		
		System.out.println(rl.toString());
		
	}

	public static class PropertyItem {
		
		private String propertyName;
		
		private String propertyURI;

		private PropertyMetadata pm;
		
		public PropertyItem(String propertyName, String propertyURI, PropertyMetadata pm) {
			super();
			this.propertyName = propertyName;
			this.propertyURI = propertyURI;
			this.pm = pm;
		}



		@Override
		public String toString() {
			String type = pm.getBaseClass().getSimpleName();
			if(type.endsWith("Property")) {
				type = type.substring(0, type.length() - "Property".length());
			}
			return propertyName + "   [" + propertyURI + "] (" + type + ")";
		}
		
	}
	
}
