package ai.vital.cytoscape.app.internal.tabs;

import groovy.lang.Binding;
import groovy.lang.GroovyShell;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.commons.io.IOUtils;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.view.presentation.property.BasicVisualLexicon;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ai.vital.cytoscape.app.internal.app.Application;
import ai.vital.cytoscape.app.internal.app.VitalAICytoscapePlugin;
import ai.vital.cytoscape.app.internal.model.Utils;
import ai.vital.cytoscape.app.internal.model.VisualStyleUtils;
import ai.vital.cytoscape.app.internal.panels.NetworkListPanel;
import ai.vital.domain.Datascript;
import ai.vital.domain.DatascriptInfo;
import ai.vital.vitalservice.VitalStatus;
import ai.vital.vitalservice.query.ResultElement;
import ai.vital.vitalservice.query.ResultList;
import ai.vital.vitalsigns.model.GraphObject;
import ai.vital.vitalsigns.model.PropertyInterface;
import ai.vital.vitalsigns.model.VITAL_Edge;
import ai.vital.vitalsigns.model.VITAL_Node;

public class DatascriptsTab extends JPanel {

	private final static Logger log = LoggerFactory.getLogger(DatascriptsTab.class);
	
	private static final long serialVersionUID = -2702184714041125270L;

	private DefaultListModel<DatascriptItem> model;
	
	private JList<DatascriptItem> scriptsList; 
	
	private JLabel selectedScriptLabel;
	
	private DatascriptItem selectedScript;

	private JRadioButton fileRadioButton = new JRadioButton("File");
	private JRadioButton textRadioButton = new JRadioButton("Text");
	
	private JLabel selectedFileLabel = new JLabel("<html><i>(none)</i></html>");
	private File selectedFile = null;
	
	private JFileChooser paramsFC = new JFileChooser();
	JButton browseButton = new JButton("...");
	
	private JTextArea textArea = new JTextArea();
	
	public DatascriptsTab() {

		setLayout(new BorderLayout());
		
		int hspace = 5;
		JPanel northPanel = new JPanel();
		northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
		northPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		
		model = new DefaultListModel<DatascriptsTab.DatascriptItem>();
		//scriptsList.setB
		scriptsList = new JList<DatascriptsTab.DatascriptItem>(model);
		scriptsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		scriptsList.setPreferredSize(new Dimension(0, 150));
		
		scriptsList.addListSelectionListener(new ListSelectionListener() {
			
			@Override
			public void valueChanged(ListSelectionEvent event) {

				DatascriptItem selected = scriptsList.getSelectedValue();
				if(selected != null) {
					selectedScript = selected;
					selectedScriptLabel.setText("<html><strong>" + selected.toString() + "</strong></html>");
				} else {
					selectedScript = null;
					selectedScriptLabel.setText("<html><i>(none)</i></html>");
				}
				
			}
		});
		
		northPanel.add(new JScrollPane(scriptsList));
		
		scriptsList.setEnabled(false);

		
		
		JPanel selectedScriptPanel = new JPanel(); 
		selectedScriptPanel.setLayout(new BoxLayout(selectedScriptPanel, BoxLayout.X_AXIS));
		selectedScriptPanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		selectedScriptPanel.add(new JLabel("selected:"));
		selectedScriptPanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		selectedScriptLabel = new JLabel("<html><i>(none)</i></html>");
		selectedScriptPanel.add(selectedScriptLabel);
		
		northPanel.add(selectedScriptPanel);
		
		
		
		ButtonGroup logicalGroup = new ButtonGroup();
		logicalGroup.add(fileRadioButton);
		logicalGroup.add(textRadioButton);
		
		ChangeListener cl = new ChangeListener(){

			@Override
			public void stateChanged(ChangeEvent arg0) {

				onRadioChanged();
				
			}};
		
		fileRadioButton.addChangeListener(cl);
		textRadioButton.addChangeListener(cl);
		
		JPanel inputFilePanel = new JPanel();
		inputFilePanel.setLayout(new BoxLayout(inputFilePanel, BoxLayout.X_AXIS));
		inputFilePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		inputFilePanel.add(fileRadioButton);
		inputFilePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		inputFilePanel.add(selectedFileLabel);
		inputFilePanel.add(Box.createRigidArea(new Dimension(hspace, 0)));
		
		paramsFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
		
		browseButton.setEnabled(false);
		browseButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent arg0) {
				
				int v = paramsFC.showOpenDialog(null);
				if(v == JFileChooser.APPROVE_OPTION) {
					selectedFile = paramsFC.getSelectedFile();
					selectedFileLabel.setText("<html><strong>" + selectedFile.getName() + "</strong></html>");
				} else {
					//cancelled
				}
			}
		});
		inputFilePanel.add(browseButton);
		
		northPanel.add(inputFilePanel);
		
		
		
		JPanel inputTextRadioPanel = new JPanel();
		inputTextRadioPanel.setLayout(new BorderLayout());
//		inputTextPanel.add(Box.createRigidArea(new Dimension(hspace,0)));
		
		JPanel inputTextRow = new JPanel();
		inputTextRow.setLayout(new BoxLayout(inputTextRow, BoxLayout.X_AXIS));
		inputTextRow.add(Box.createRigidArea(new Dimension(hspace, 0)), BorderLayout.WEST);
		inputTextRow.add(textRadioButton);
		inputTextRadioPanel.add(inputTextRow, BorderLayout.WEST);
		
		JPanel inputTextAreaPanel = new JPanel();
		inputTextAreaPanel.setLayout(new BoxLayout(inputTextAreaPanel, BoxLayout.X_AXIS));
		inputTextAreaPanel.add(textArea);
		textArea.setEditable(true);
		textArea.setRows(5);
		
		northPanel.add(inputTextRadioPanel);
		northPanel.add(inputTextAreaPanel);
		
		
		
		onRadioChanged();

		JPanel runButtonPanel = new JPanel();
		runButtonPanel.setLayout(new BorderLayout());
		
		JButton runButton = new JButton("Run");
		
		runButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent ae) {
				runDatascript();
			}
		});
		
		runButtonPanel.add(runButton, BorderLayout.EAST);
		northPanel.add(Box.createRigidArea(new Dimension(0, 15)));
		northPanel.add(runButtonPanel);
		
		add(northPanel, BorderLayout.NORTH);
		
		//fetch datascripts in another thread
		
		Thread thread = new Thread(){
			@Override
			public void run() {
				try {
					List<Datascript> datascripts = Application.get().getDatascripts();
					for(Datascript g : datascripts) {
						model.addElement(new DatascriptItem(g));
					}
					scriptsList.setEnabled(true);
				} catch (Exception e) {
					log.error(e.getLocalizedMessage(), e);
					JOptionPane.showMessageDialog(null, "Datascripts listing error: " + e.getLocalizedMessage(), "Datascripts error", JOptionPane.ERROR_MESSAGE);
				}
			}
			
		};
		
		thread.setDaemon(true);
		thread.start();
	
		
	}
	
	protected void runDatascript() {

		Reader paramsReader = null;
		
		Map<String, Object> runParams = null;
		
		try {
			
			if(selectedScript == null) {
				throw new Exception("No datascript selected");
			}
			
			if(! (fileRadioButton.isSelected() || textRadioButton.isSelected()) ) {
				throw new Exception("No selection: file/text parameters map input");
			}
			
			
			
			if(fileRadioButton.isSelected()) {
				
				if(selectedFile == null) {
					throw new Exception("No parameters file selected");
				}
				
				paramsReader = new InputStreamReader(new FileInputStream(selectedFile), "UTF-8");
				
			} else if(textRadioButton.isSelected()) {
				
				String text = textArea.getText();
				
				if(text == null || text.trim().isEmpty()) {
					
					throw new Exception("No parameters map text");
					
				}
				
				paramsReader = new StringReader(text);
				
			}
			
			Binding binding = new Binding();
			GroovyShell shell = new GroovyShell(binding);
			Object _object = shell.evaluate(paramsReader, "input");
	
			
			if(!(_object instanceof Map)) throw new Exception("Datascript input script must return a map of datascript params.");

			runParams = (Map<String, Object>) _object;	
			
		} catch(Exception e) {
			JOptionPane.showMessageDialog(null, e.getLocalizedMessage(), "Validation error", JOptionPane.ERROR_MESSAGE);
			return;
		} finally {
			IOUtils.closeQuietly(paramsReader);
		}
		
		DialogTaskManager dialogTaskManager = VitalAICytoscapePlugin.getDialogTaskManager();

		
		final Map<String, Object> runParamsF = runParams;
		AbstractTask task = new AbstractTask() {
			
			@Override
			public void run(TaskMonitor taskMonitor) throws Exception {

				String title = "Datascript: " + selectedScript.toString();
				
				if(taskMonitor != null) {
					taskMonitor.setTitle(title);
					taskMonitor.setProgress(-1d);
					taskMonitor.setStatusMessage("Executing datascript: " + selectedScript.toString());
				} else {
					System.out.println("No task monitor - printing to console");
					System.out.println(title);
				}

				final ResultList results = Application.get().executeDatascript((String)selectedScript.script.getProperty("scriptPath"), runParamsF);
				
				if(taskMonitor != null) {
					taskMonitor.setProgress(1D);
					taskMonitor.setStatusMessage("DONE");
				} else {
					System.out.println("DONE");
				}
				
				Thread resultsThread = new Thread(){
					@Override
					public void run() {
						
						ResultsDialog dialog = new ResultsDialog(selectedScript, results);
						dialog.setVisible(true);
						
					}
				};
				
				resultsThread.setDaemon(true);
				resultsThread.start();
//				for(ResultElement re : results.getResults()) {
//					
//				}
				
			}
		};
		
		if(dialogTaskManager != null) {
			dialogTaskManager.execute(new TaskIterator(task));
		} else {
			try {
				task.run(null);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		
	}

	protected void onRadioChanged() {

		if( fileRadioButton.isSelected() ) {
			
			selectedFileLabel.setEnabled(true);
			browseButton.setEnabled(true);
			
		} else {

			selectedFileLabel.setEnabled(false);
			browseButton.setEnabled(false);
			
		}
			
		if(textRadioButton.isSelected()){
			
			textArea.setEnabled(true);
			
		} else {
			
			textArea.setEnabled(false);
			
		}
		
	}

	public static class DatascriptItem {

		Datascript script = null;
		
		public DatascriptItem(Datascript g) {
			script = g;
		}
		
		@Override
		public String toString() {
			String n = null;//(String) script.getProperty("name");
			String p = (String) script.getProperty("scriptPath");
			if(p.startsWith("commons")) {
				n += " (common)";
			}
			
			int lastSlash = p.lastIndexOf('/');
			if(lastSlash >= 0) {
				n = p.substring(lastSlash+1);
			} else {
				n = p;
			}
			
			return n;
		}
		
	}
	
	public static void main(String args[]) throws InterruptedException {
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		Application.initForTests();

		DatascriptsTab panel = new DatascriptsTab();
		
		frame.getContentPane().add(panel);
		frame.pack();
		frame.setVisible(true);
		
	}

	public static class ResultsDialog extends JDialog {

		private static final long serialVersionUID = 9007033164640098691L;

		private NetworkListPanel networkListPanel = null;
		
		public ResultsDialog(DatascriptItem datascript, ResultList rl){
			
			setTitle("Datascript results - " + datascript.toString());
			setModal(true);
			setSize(400, 500);
			setLayout(new BorderLayout());

			JPanel northPanel = new JPanel();
			northPanel.setLayout(new BoxLayout(northPanel, BoxLayout.Y_AXIS));
			
			
			JPanel bottomPanelW = new JPanel(new BorderLayout());
			JPanel bottomPanel = new JPanel();
			bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.X_AXIS));
			bottomPanelW.add(bottomPanel, BorderLayout.EAST);
			JButton closeButton = new JButton("Close");
			closeButton.addActionListener(new ActionListener() {
				
				@Override
				public void actionPerformed(ActionEvent arg0) {
					dispose();
				}
			});
			
			
			
			bottomPanel.add(closeButton);
			bottomPanel.add(Box.createRigidArea(new Dimension(10, 0)));
			
			add(northPanel, BorderLayout.NORTH);
			add(bottomPanelW, BorderLayout.SOUTH);
			
			if(rl.getStatus().getStatus() != VitalStatus.Status.ok) {
				northPanel.add(new JLabel("<html><span style=\"color: red;\">ERROR: " + rl.getStatus().getMessage() + "</span></html>"));
				return;
			}
			
			List<DatascriptInfo> infos = new ArrayList<DatascriptInfo>();
			
			final List<VITAL_Node> nodes = new ArrayList<VITAL_Node>();
			final List<VITAL_Edge> edges = new ArrayList<VITAL_Edge>();
			
			for(ResultElement re : rl.getResults()) {
				GraphObject g = re.getGraphObject();
				if(g instanceof DatascriptInfo) {
					infos.add((DatascriptInfo) g);
				} else if(g instanceof VITAL_Node) {
					nodes.add((VITAL_Node) g);
				} else if(g instanceof VITAL_Edge) {
					edges.add((VITAL_Edge) g);
				}
			}
			
			String s = "<html>INFO objects: [" + infos.size() + "]";
			
			int i = 0;
			for(DatascriptInfo info : infos) {
				i++;
				s += ("<br/>" + i +":");
				for(Object e : info.getProperties().entrySet()) {
					Entry entry = (Entry) e;
					String key= (String) entry.getKey();
					PropertyInterface val = (PropertyInterface) entry.getValue();
					s+= ( "   " + key + "=" + val.getValue());
				}
				
			}
			
			s+= "</html>";
			
			JPanel sw = new JPanel(new BorderLayout());
			sw.add(new JLabel(s), BorderLayout.WEST);
			northPanel.add(sw);
			northPanel.add(Box.createRigidArea(new Dimension(0, 10)));
			
			
			JPanel statsW = new JPanel(new BorderLayout());
			statsW.add(new JLabel("Nodes: " + nodes.size() + " edges:" + edges.size()), BorderLayout.WEST);
			northPanel.add(statsW);
			
			
			if(nodes.size() > 0 || edges.size() > 0) {

				JPanel tnw = new JPanel(new BorderLayout());
				tnw.add(new JLabel("Target network: "), BorderLayout.WEST);
				northPanel.add(tnw);

				networkListPanel = new NetworkListPanel(false);
				northPanel.add(networkListPanel);
				
				JButton importResultsButton = new JButton("Import");
				
				final JLabel importStatus = new JLabel();
				JPanel isl = new JPanel(new BorderLayout());
				isl.add(importStatus, BorderLayout.WEST);
				northPanel.add(isl);
				
				importResultsButton.addActionListener(new ActionListener() {
					
					@Override
					public void actionPerformed(ActionEvent arg0) {

						
						CyNetwork cyNetwork = networkListPanel.getSelectedNetwork();
						
						log.info("importing nodes and edges into network");
						
						Utils.placeNodesEdgesInTheNetwork(cyNetwork, nodes.toArray(new VITAL_Node[nodes.size()]), edges.toArray(new VITAL_Edge[edges.size()]));
						
						VitalAICytoscapePlugin.getEventHelper().flushPayloadEvents();
						
						CyNetworkView networkView = VitalAICytoscapePlugin.getNetworkView(cyNetwork);
						
						if (networkView != null) {

							// Utils.defaultLayout.doLayout(networkView);
							// networkView.applyLayout(Utils.defaultLayout);

//							networkView.updateView();
							
							log.debug("Updating network view...");
							VisualStyleUtils.applyVisualStyle(networkView);

						}
						
						importStatus.setText("Import complete.");
						
					}
				});
				
				bottomPanel.add(importResultsButton);
				
			}
			
//			northPanel.add(new JLabel("<html><span style=\"color: red;\">ERROR: " + rl.getStatus().getMessage() + "</span></html>"));
			
			
		}
		
	}
}
