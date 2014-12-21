package it.necst.grabnrun.selector;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.Choice;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.Label;
import java.awt.Panel;
import java.awt.SystemColor;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

public class MainFrame extends Frame {

	private static final long serialVersionUID = -3423546282965389324L;

	private boolean isValidApkToPatch;
	private TextField apkField;
	private Button apkButton;
	private Button quit;
	private Button next;
	
	private Panel contSelectPanel;

	static final int MAX_NUM_COLUMNS = 50;
	private static final int MAX_NUM_CONT = 7;
	
	private TextField[] contURLFields;
	
	private Set<String> validURIContSet;
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		new MainFrame();
		
	}
	
	private MainFrame() {
		super("Grab'n Run: Repackaging Tool");
		
		// Initialize array for containers URL TextFields
		contURLFields = new TextField[MAX_NUM_CONT];
		
		// Graphical setup
		this.buildContent();
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
                MainFrame.this.quit();
            }
		});
		
		this.pack();
		this.setLocationRelativeTo(null);
		this.setResizable(false);
		this.setVisible(true);
	}

	private void buildContent() {
		
		this.setBackground(SystemColor.control);
		this.apkField = new TextField(MAX_NUM_COLUMNS);
		this.apkField.setEditable(false);
		this.apkButton = new Button("Browse..");
		this.isValidApkToPatch = false;
		this.quit = new Button("Quit");
		this.next = new Button("Next..");
		
		this.quit.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				MainFrame.this.quit();
			}
		});
		
		this.next.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				MainFrame.this.next();
			}
		});
		
		this.apkButton.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				MainFrame.this.apkChoose();
			}
		});
		
		Panel pageEndPanel = new Panel();
		pageEndPanel.setLayout(new BoxLayout(pageEndPanel, BoxLayout.X_AXIS));
		pageEndPanel.add(quit);
		pageEndPanel.add(next);
		this.add(BorderLayout.PAGE_END, pageEndPanel);
		
		Panel apkTextPanel = new Panel();
		apkTextPanel.setLayout(new BoxLayout(apkTextPanel, BoxLayout.PAGE_AXIS));
		apkTextPanel.add(new Label("Select an APK container to patch:"));
		apkTextPanel.add(apkField);
		
		Panel apkButtonPanel = new Panel();
		apkButtonPanel.setLayout(new BoxLayout(apkButtonPanel, BoxLayout.Y_AXIS));
		apkButtonPanel.add(new Label());
		apkButtonPanel.add(apkButton);
		
		Panel apkChoosePanel = new Panel();
		apkChoosePanel.add(BorderLayout.WEST, apkTextPanel);
		apkChoosePanel.add(BorderLayout.EAST, apkButtonPanel);
		this.add(BorderLayout.PAGE_START, apkChoosePanel);
		
		contSelectPanel = new Panel();
		contSelectPanel.setLayout(new BoxLayout(contSelectPanel, BoxLayout.Y_AXIS));
		Panel contNumberPanel = new Panel();
		contNumberPanel.setLayout(new BoxLayout(contNumberPanel, BoxLayout.X_AXIS));
		contNumberPanel.add(new Label("Select the number of JAR/APK containers to use as sources: "));
		final Choice numberOfCont = new Choice();
		
		for (Integer i = 1; i <= MAX_NUM_CONT ; i ++) {
			numberOfCont.add(i.toString());
		}
		
		numberOfCont.addItemListener(new ItemListener() {
			
			public void itemStateChanged(ItemEvent ie) {
				
				updateContainerBoxes(Integer.valueOf(numberOfCont.getSelectedItem()));
	        }

		});
		
		contNumberPanel.add(numberOfCont);
		contSelectPanel.add(new JSeparator());
		contSelectPanel.add(contNumberPanel);
		
		contSelectPanel.add(new Label("Write down for each white line below either the remote URL or"));
		contSelectPanel.add(new Label("the local path of a JAR/APK container that you intend to use as"));
		contSelectPanel.add(new Label("a possible source for classes to dynamically load at runtime."));
		
		// TextFields for containers URL are initialized..
		for (int i = 0; i < MAX_NUM_CONT; i++) {
			
			contURLFields[i] = new TextField(MAX_NUM_COLUMNS);
			contURLFields[i].setVisible(true);
			contSelectPanel.add(contURLFields[i]);
		}
		
		updateContainerBoxes(Integer.valueOf(0));
		
		contSelectPanel.add(new JSeparator());
		
		this.add(BorderLayout.CENTER, contSelectPanel);
		//contSelectPanel.setVisible(false);
		
	}

	private void updateContainerBoxes(Integer boxToShow) {
		
		for (int i = 0; i < MAX_NUM_CONT; i ++) {
			
			if (isValidApkToPatch) {
				
				if (i < boxToShow) {
					
					//contURLFields[i].setVisible(true);
					contURLFields[i].setBackground(SystemColor.WHITE);
					contURLFields[i].setEditable(true);
					
				} else {
					
					//contURLFields[i].setVisible(false);
					//contURLFields[i] = new TextField(MAX_NUM_COLUMNS);
					contURLFields[i].setBackground(SystemColor.control);
					contURLFields[i].setEditable(false);
					//contURLFields[i].setVisible(true);
					
				}
			} else {
				
				contURLFields[i].setBackground(SystemColor.control);
				contURLFields[i].setEditable(false);
			}
		}
		
		this.pack();
		//this.setLocationRelativeTo(null);
	}

	private void apkChoose() {
		
		final FileDialog fileDialog = new FileDialog(this, "Choose an APK container..", FileDialog.LOAD);
		// fileDialog.setFile("*.apk");
		fileDialog.setAlwaysOnTop(true);
		fileDialog.setVisible(true);
		
		// Select an APK from "Recent tabs.." does not work.
		// This is a known issue of current JDK.. Oracle should fix it..
		if (fileDialog.getDirectory() != null && fileDialog.getFiles() != null && fileDialog.getFiles().length == 1) {
			
			String apkPath = fileDialog.getFiles()[0].getAbsolutePath();
			if (apkPath.toLowerCase().endsWith(".apk")) {
				
				this.apkField.setBackground(SystemColor.GREEN);
				this.apkField.setText(apkPath);
				
				contSelectPanel.setVisible(true);
				this.pack();
				// this.setLocationRelativeTo(null);
				
				this.isValidApkToPatch = true;
				
				updateContainerBoxes(1);
			}
			else {
				
				this.apkField.setBackground(SystemColor.control);
				this.apkField.setText("Invalid file! Choose an apk container!");
				
				//contSelectPanel.setVisible(false);
				//this.pack();
				//this.setLocationRelativeTo(null);
				
				this.isValidApkToPatch = false;
			}
		}
	}

	private void next() {
		
		boolean openDialog = false;
		
		if (isValidApkToPatch) {
			
			validURIContSet = new HashSet<String>();
			
			for (TextField textField : contURLFields) {
			
				String containerURLString = null;
				
				if (textField.isEditable())
					containerURLString = textField.getText();
				
				if (containerURLString != null && !containerURLString.isEmpty()) {
					
					// Try to parse this container location and see if
					// it is a valid remote URL
					URL containerURL = null;
					
					try {
						
						containerURL = new URL(containerURLString);
						
						if (	containerURL.getProtocol().equals("http") || 
								containerURL.getProtocol().equals("https")) {
							
							validURIContSet.add(containerURL.toString());
						}
						
					} catch (MalformedURLException e) {
						
						// Not a valid remote URL.. Test if at least this string 
						// matches a valid local container file on the system
						if (	containerURLString.toLowerCase().endsWith(".apk") ||
								containerURLString.toLowerCase().endsWith(".jar")) {
							
							File testForAFile = new File(containerURLString);
							if (testForAFile.exists() && testForAFile.isFile())
								validURIContSet.add(containerURLString);
						}
					}
				}
			}
			
			if (!validURIContSet.isEmpty())
				openDialog = true;
			
			if (openDialog) {
				
				new LinkContainerDialog(this, "Select validation method..", apkField.getText(), validURIContSet);
				//System.out.println("Found " + validURLContSet.size() + " valid distinct container URL.");
			} else {
				
				JOptionPane.showMessageDialog(this, "Insert at least one valid remote URL or local file path pointing to an APK/JAR container in one of the white enabled fields!", "No valid container remote URL or local path", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private void quit() {
        this.setVisible(false);
        System.exit(1);
    }
}
