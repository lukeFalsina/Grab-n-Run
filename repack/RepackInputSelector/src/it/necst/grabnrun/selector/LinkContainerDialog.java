package it.necst.grabnrun.selector;

import java.awt.BorderLayout;
import java.awt.Button;
import java.awt.CardLayout;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.Label;
import java.awt.Panel;
import java.awt.TextField;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;

public class LinkContainerDialog extends Dialog {

	private static final long serialVersionUID = 4683700510412528166L;
	private int MAX_NUMBER_OF_MAP_ENTRIES;

	private static final String DEFAULT_VALUE = "default";
	
	private CardLayout cardLayout;
	private Panel cardPanel, valOneCertPan, valManyCertPan, valWithAssocMapPan, comboBoxPanel;

	private TextField oneCertPathField, defInManyCertsField;
	private List<TextField> manyCertPathTextFieldList;
	private List<TextField> assocMapTextFieldList;
	
	private Button finish;
	private JComboBox<String> comboBox;
	
	private String apkPath;
	private Set<String> validURLContSet;
	private JCheckBox allowDefault;
	
	private static final String VAL_ONE_CERT = "Validate all containers with one certificate";
	private static final String VAL_MANY_CERTS = "Link each container with a certificate";
	private static final String VAL_ASS_MAP = "Provide associative map";
	
	public LinkContainerDialog(	Frame owner, 
								String title, 
								String apkPath, 
								Set<String> validURLContSet) {
	
		super(owner, title, true);
		
		this.addWindowListener(new WindowAdapter() {
			public void windowClosing(final WindowEvent e) {
                setVisible(false);
                dispose();
            }
		});
		
		this.MAX_NUMBER_OF_MAP_ENTRIES = validURLContSet.size() + 3;
		
		this.apkPath = apkPath;
		this.validURLContSet = validURLContSet;
		
		// setSize(100, 100);
		
		cardPanel = new Panel();
		comboBoxPanel = new Panel();
		
		// Setup cards layout
		cardLayout = new CardLayout();
		cardPanel.setLayout(cardLayout);
		
		valOneCertPan = new Panel();
		valManyCertPan = new Panel();
		valWithAssocMapPan = new Panel();
		
		// Setup of the first card:
		// One certificate to validate all the containers.
		valOneCertPan.setLayout(new BorderLayout());
		allowDefault = new JCheckBox("<html>Allow the tool to patch also containers not listed in the previous dialog with the same remote certificate.</html>");
		allowDefault.setMnemonic(KeyEvent.VK_A);
        JLabel explValOneCertLabel = new JLabel("<html>Here all the containers will be evaluated against a certificate located at the provided not empty remote URL.<br>Please also note that HTTPS protocol is required for this entry.</html>", JLabel.CENTER);
        oneCertPathField = new TextField(MainFrame.MAX_NUM_COLUMNS);
        valOneCertPan.add(explValOneCertLabel, BorderLayout.CENTER);
        valOneCertPan.add(allowDefault, BorderLayout.NORTH);
        valOneCertPan.add(oneCertPathField, BorderLayout.SOUTH);

        // Setup of the second card:
     	// Each container is linked to a certificate.
        valManyCertPan.setLayout(new BorderLayout());
        JLabel explValManyCertLabel = new JLabel("<html>Here each container must be linked with a not empty URL pointing to a remote certificate.<br>HTTPS protocol is required for all the provided entries.<br>There is also an optional final entry in case you want to validate all the other found<br>containers with a default remote certificate.</html>", JLabel.CENTER);
        valManyCertPan.add(explValManyCertLabel, BorderLayout.NORTH);
        Panel contAndCertPan = new Panel();
        contAndCertPan.setLayout(new GridLayout(0, 2));
        contAndCertPan.add(new JLabel("<html>Container Remote URL / Local File Path</html>", JLabel.CENTER));
        contAndCertPan.add(new JLabel("<html>Remote certificate URL <strong>[HTTPS]</strong></html>", JLabel.CENTER));
        
        // Insert two entries for each valid container URL
        // First show the container URL, while the second asks for the certificate URL.
        Iterator<String> validContainerURLIterator = validURLContSet.iterator();
        manyCertPathTextFieldList = new ArrayList<TextField>();
		
		while (validContainerURLIterator.hasNext()) {
			
			// Add the label for the container URL
			contAndCertPan.add(new Label(validContainerURLIterator.next()));
			
			// Create and add the TextField for the remote certificate URL
			TextField oneCertPathTextField = new TextField(MainFrame.MAX_NUM_COLUMNS);
			manyCertPathTextFieldList.add(oneCertPathTextField);
			contAndCertPan.add(oneCertPathTextField);
		}
		
		// Finally add the default TextField in case the user wants to enable the default value..
		contAndCertPan.add(new Label("Other containers [This field is OPTIONAL]"));
		defInManyCertsField = new TextField(MainFrame.MAX_NUM_COLUMNS);
		contAndCertPan.add(defInManyCertsField);
		
		// Fill in the remaining entries to match the third card size
		for (int i=0; i < MAX_NUMBER_OF_MAP_ENTRIES - validURLContSet.size() - 1; i ++) {
			
			// Fill in a line with two labels.
			contAndCertPan.add(new Label());
			contAndCertPan.add(new Label());
		}
        
        valManyCertPan.add(contAndCertPan, BorderLayout.CENTER);
        
        // Setup of the third card:
     	// Provide an associative map between package names and certificates.
        valWithAssocMapPan.setLayout(new BorderLayout());
        JLabel explAssMapLabel = new JLabel("<html>Here you can directly provide an associative map which links package names to not empty URL location pointing<br>to remote certificates (HTTPS protocol is required for all entries).<br>Refer to GNR documentation to learn how to populate this map; otherwise use one of the previous two options!</html>", JLabel.CENTER);
        valWithAssocMapPan.add(explAssMapLabel, BorderLayout.NORTH);
        Panel assocMapPan = new Panel();
        assocMapPan.setLayout(new GridLayout(0, 2));
        assocMapPan.add(new JLabel("<html>Package name</html>", JLabel.CENTER));
        assocMapPan.add(new JLabel("<html>Remote certificate URL <strong>[HTTPS]</strong></html>", JLabel.CENTER));
        
        // Insert two TextFields for each line.
        // First asks for the package name, while the second for the certificate URL.
        assocMapTextFieldList = new ArrayList<TextField>();
		
        for (int i = 0; i < MAX_NUMBER_OF_MAP_ENTRIES; i++) {
        	
        	// Create and add the TextField for a package name entry.
        	TextField packageNameTextField = new TextField(MainFrame.MAX_NUM_COLUMNS);
        	assocMapTextFieldList.add(packageNameTextField);
        	assocMapPan.add(packageNameTextField);
        	
        	// Create and add the TextField for a remote certificate URL entry.
        	TextField oneCertPathTextField = new TextField(MainFrame.MAX_NUM_COLUMNS);
        	assocMapTextFieldList.add(oneCertPathTextField);
        	assocMapPan.add(oneCertPathTextField);
        }
        
		valWithAssocMapPan.add(assocMapPan, BorderLayout.CENTER);
        
        // Setup of the combo box to show different cards
        cardPanel.add(valOneCertPan, VAL_ONE_CERT);
        cardPanel.add(valManyCertPan, VAL_MANY_CERTS);
        cardPanel.add(valWithAssocMapPan, VAL_ASS_MAP);
        
        String comboBoxItems[] = { VAL_ONE_CERT, VAL_MANY_CERTS, VAL_ASS_MAP };
        
        comboBox = new JComboBox<String>(comboBoxItems);
        comboBox.setEditable(false);
        comboBox.addItemListener(new ItemListener() {
			
			@Override
			public void itemStateChanged(ItemEvent e) {
				
				CardLayout cl = (CardLayout) cardPanel.getLayout();
				cl.show(cardPanel, (String) e.getItem());
			}
		});
        comboBoxPanel.add(comboBox);
        
        Panel centerPanel = new Panel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.add(new JSeparator());
        centerPanel.add(cardPanel);
        centerPanel.add(new JSeparator());
        
        // Setup of the finish button
        finish = new Button("Finish");
        finish.addActionListener(new ActionListener() {
			
			@Override
			public void actionPerformed(ActionEvent e) {
				
				LinkContainerDialog.this.validateInput();
			}
		});
        
        add(comboBoxPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(finish, BorderLayout.SOUTH);
		
		// Show dialog
        this.setResizable(false);
		this.pack();
		this.setLocationRelativeTo(owner);
		this.setVisible(true);
		
	}

	private void validateInput() {
		
		// Depending on which card is currently shown,
		// the verification process changes..
		String chosenCard = (String) comboBox.getSelectedItem();
		
		Map<String, String> keyToCertificateURLMap = new HashMap<String, String> ();
		
		switch(chosenCard) {
		
		case VAL_ONE_CERT:
			
			String certificateURL = oneCertPathField.getText();
			
			if (isValidRemoteCertURL(certificateURL)) {
				
				Iterator<String> validContainerURLIterator = validURLContSet.iterator();
				
				while (validContainerURLIterator.hasNext()) {
					
					// Populate a map which links container URL with 
					// the same certificate URL..
					keyToCertificateURLMap.put(validContainerURLIterator.next(), certificateURL);
				}
				
				if (allowDefault.isSelected()) {
					
					// If the user allow the tool to patch all the found containers
					// add a special default entry as well..
					keyToCertificateURLMap.put(DEFAULT_VALUE, certificateURL);
				}
				
				// Store all the preferences into an helper file
				// and then exit.
				saveAndFinish(keyToCertificateURLMap, false);
				
			} else
				JOptionPane.showMessageDialog(this, "The provided URL was empty or did not match a remote URL using HTTPS!", "Invalid certificate URL", JOptionPane.ERROR_MESSAGE);
			break;
		
		case VAL_MANY_CERTS:
			
			boolean isUserInputValid = true;
			
			// Check that all the entries are not empty and
			// valid remote URL with HTTPS protocol.
			for (TextField currentTextField : manyCertPathTextFieldList) {
				
				if (!isValidRemoteCertURL(currentTextField.getText())) {
					isUserInputValid = false;
					break;
				}
			}
			
			if (isUserInputValid) {
				
				Iterator<String> validContainerURLIterator = validURLContSet.iterator();
				int indexToPickTextField = 0;
				
				while (validContainerURLIterator.hasNext()) {
					
					// Populate a map which links container URL with 
					// the same certificate URL..
					keyToCertificateURLMap.put(	validContainerURLIterator.next(), 
												manyCertPathTextFieldList.get(indexToPickTextField).getText());
					indexToPickTextField++;
				}
				
				// Validation of the default value, if present..
				String defaultRemoteCertURL = defInManyCertsField.getText();
				
				if (defaultRemoteCertURL.isEmpty()) {
					
					// Store all the preferences into an helper file
					// and then exit.
					saveAndFinish(keyToCertificateURLMap, false);
					
				} else {
					
					if (isValidRemoteCertURL(defaultRemoteCertURL)) {
						
						// Add also the default entry since its certificate it's valid..
						keyToCertificateURLMap.put(DEFAULT_VALUE, defaultRemoteCertURL);
						
						// Store all the preferences into an helper file
						// and then exit.
						saveAndFinish(keyToCertificateURLMap, false);
						
					} else {
						JOptionPane.showMessageDialog(this, "The default entry used for not listed containers must be either empty or match a remote URL using HTTPS!", "Invalid certificate URL", JOptionPane.ERROR_MESSAGE);
					}
				}
				
			} else {
				JOptionPane.showMessageDialog(this, "At least one of the required entries was empty or did not match a remote URL using HTTPS!", "Invalid certificate URL", JOptionPane.ERROR_MESSAGE);
			}
			
			break;
			
		case VAL_ASS_MAP:
			
			boolean areUserPackagesValid = true;
			boolean areUserCertURLsValid = true;
			boolean inputCheckActive = false;
			Iterator<TextField> assocMapTextFieldIterator = assocMapTextFieldList.iterator();
			Map<String, String> partialAssociativeMap = new HashMap<String, String>();
			
			while (assocMapTextFieldIterator.hasNext()) {
				
				// Retrieve elements from the table in couples..
				String currentPackageName = assocMapTextFieldIterator.next().getText();
				String currentCertURL = assocMapTextFieldIterator.next().getText();
				
				if (currentPackageName != null & !currentPackageName.isEmpty()) {
					
					inputCheckActive = true;
					
					if (isValidPackageName(currentPackageName)) {
						if (isValidRemoteCertURL(currentCertURL)) {
							
							// Valid couple of package name and certificate.
							// Add this couple to the partial map
							partialAssociativeMap.put(currentPackageName, currentCertURL);
							
						} else {
							areUserCertURLsValid = false;
							break;
						}
						
					} else {
						areUserPackagesValid = false;
						break;
					}
				}
			}
			
			// If at least a left not null entry is present then evaluate the
			// correctness of the input data.
			if (inputCheckActive) {
				
				if (areUserPackagesValid) {
					if (areUserCertURLsValid) {
						
						// In this case all the entries were valid so
						// store them in the preference file.
						saveAndFinish(partialAssociativeMap, true);
						
					} else
						JOptionPane.showMessageDialog(this, "At least one of the right entries linked to a valid package name was empty or did not match a remote URL using HTTPS!", "Invalid certificate URL", JOptionPane.ERROR_MESSAGE);
					
				} else
					JOptionPane.showMessageDialog(this, "<html>At least one of the not empty left entries was not a valid package name!<br>If you are in doubts please check out the definiton of package name at this link:<br>http://grab-n-run.readthedocs.org/en/latest/tutorial.html#using-standard-dexclassloader-to-load-code-dynamically</html>", "Invalid package name", JOptionPane.ERROR_MESSAGE);
			}
			
			break;
			
		default:
			throw new RuntimeException("An invalid value was returned from the combo box");
		}
		
	}
	
	private void saveAndFinish(Map<String, String> keyToCertificateURLMap,
			boolean isKeyPackageName) {
		
		File helperFile = new File("./preferences");
		
		// At first erase any copy of an older pref file, if any..
		if (helperFile.exists())
			helperFile.delete();
		
		BufferedWriter writer = null;
		
		try {
			
			writer = new BufferedWriter(new FileWriter(helperFile));
			String newline = System.getProperty("line.separator");
			
			// First write the path to the APK to patch
			writer.write(apkPath + newline);
			
			// Then write whether the following keys of the map are 
			// going to be package names or container URLs
			writer.write(isKeyPackageName + newline);
			
			// Finally write for each key the related URL
			// of the remote certificate to validate it.
			Iterator<String> keyIterator = keyToCertificateURLMap.keySet().iterator();
			
			while (keyIterator.hasNext()) {
				
				String keyToWrite = keyIterator.next();
				writer.write(keyToWrite + " | " + keyToCertificateURLMap.get(keyToWrite) + newline);
			}
		
		} catch (IOException e) {
			e.printStackTrace();
			finish(false);
		} finally {
			
			if (writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
					finish(false);
				}
			}
		}
		
		// Everything went smoothly..
		finish(true);
		
	}
	
	private void finish(boolean isCorrectTermination) {
		
		// Hide and release this dialog.
		setVisible(false);
        dispose();
        
        if (isCorrectTermination)
        	System.exit(0);
        
        // An abnormal termination occurred..
        System.exit(1);
	}

	private boolean isValidRemoteCertURL(String candidateURLString) {
		
		if (candidateURLString == null || candidateURLString.isEmpty())
			return false;
		
		URL candidateURL = null;
		
		try {
			
			candidateURL = new URL(candidateURLString);
			
			if (!candidateURL.getProtocol().equals("https"))
				throw new MalformedURLException();
			
			// We have a valid URL
			return true;
			
		} catch (MalformedURLException e) {
			// When this exception is raised the URL is invalid..
			return false;
		}
	}
	
	private boolean isValidPackageName(String candidatePackNameString) {
		
		if (candidatePackNameString == null || candidatePackNameString.isEmpty())
			return false;
		
		String[] packStrings = candidatePackNameString.split("\\.");
		
		for (String packString : packStrings) {
			
			// Requirement: all the subfields should contain at least one char..
			if (packString.isEmpty())
				return false;
		}
		
		// Package names should not be too general..
		// At least two strings dot separated.
		// Example: "com" is rejected, while "com.polimi" is not.
		if (packStrings.length < 2)
			return false;
		
		// Otherwise we have a valid package name.
		return true;
	}
}
