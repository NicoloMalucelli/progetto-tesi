package View;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import com.azure.digitaltwins.core.BasicDigitalTwin;

import controller.Controller;
import javax.swing.JButton;

public class ControllerFrame extends JFrame{

	private static final long serialVersionUID = 1L;
	private Controller controller;
	
	private final JList<String> buttons = new JList<>();
	private final JList<String> lights = new JList<>();
	
	private final JButton btnCreateRelationship = new JButton("create relationship");
	private final JButton btnRemoveRelationship = new JButton("delete relationship");
	
	private final DefaultListModel<String> buttonsList = new DefaultListModel<>();
	private final DefaultListModel<String> lightsList = new DefaultListModel<>();
	
	public ControllerFrame() {
		super("Digital Twin Manager");
	    setSize(800, 600);
	    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    getContentPane().setLayout(null);
	    
	    buttons.setBounds(10, 10, 250, 317);
	    getContentPane().add(buttons);
	    
	    lights.setBounds(285, 10, 250, 317);
	    getContentPane().add(lights);
	    
	    btnCreateRelationship.setBounds(565, 45, 85, 21);
	    getContentPane().add(btnCreateRelationship);
	    
	    btnRemoveRelationship.setBounds(565, 14, 85, 21);
	    getContentPane().add(btnRemoveRelationship);
	    
	    this.setVisible(true);
	    initElements();
	}
	
	private void initElements() {
		controller = new Controller();
		
	    for (BasicDigitalTwin dt : controller.getDTsOf("dtmi:contosocom:DigitalTwins:Button;1")) {
	    	buttonsList.addElement(dt.getId());
		}
		buttons.setModel(buttonsList);
		
		for (BasicDigitalTwin dt : controller.getDTsOf("dtmi:contosocom:DigitalTwins:Light;1")) {
	    	lightsList.addElement(dt.getId());
		}
		lights.setModel(lightsList);
	
		btnCreateRelationship.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				
			}
			
		});
		
		btnRemoveRelationship.addActionListener(new ActionListener() {
			
			public void actionPerformed(ActionEvent e) {
				if(buttons.getSelectedIndices().length != 1) {
					JOptionPane.showMessageDialog(null, "Select exactly a button");
					return;
				}
				controller.deleteRelationship(buttons.getSelectedValue(), "dtmi:com:adt:dtsample:button:control;1");
			}
			
		});
	}
}
