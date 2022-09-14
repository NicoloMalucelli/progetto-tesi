package View;

import java.util.Set;

import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;

import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.BasicRelationship;

import controller.Controller;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.JTextField;
import java.awt.Font;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class DTManagerView extends JFrame{

	private static final long serialVersionUID = 1L;
	private Controller controller;
	
	private final JList<String> buttons = new JList<>();
	private final JList<String> lights = new JList<>();
	private final JList<String> rooms = new JList<String>();
	
	private final JButton btnCreateRelationship = new JButton("create relationship");
	private final JButton btnRemoveRelationship = new JButton("delete relationship");
	private final JButton btnChangeSelectedButton = new JButton("update the button room");
	private final JButton btnChangeSelectedLight = new JButton("update the light room");
	private final JButton btnCreateRoom = new JButton("create room");
	
	private final DefaultListModel<String> buttonsList = new DefaultListModel<>();
	private final DefaultListModel<String> lightsList = new DefaultListModel<>();
	private final DefaultListModel<String> roomsList = new DefaultListModel<>();
	
	private final JTextField newRoomId = new JTextField();
	
	JLabel lblButtonRoom = new JLabel("Room: ");
	JLabel lblButtonRel = new JLabel("Controls: ");
	JLabel lblLightRoom = new JLabel("Room: ");
	JLabel lblLightRel = new JLabel("Controlled by: ");
	
	public DTManagerView() {
		super("Device Manager");
	    setSize(800, 600);
	    setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
	    getContentPane().setLayout(null);
	    buttons.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    buttons.setBounds(10, 36, 250, 296);
	    buttons.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
	    getContentPane().add(buttons);
	    lights.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    lights.setBounds(285, 36, 250, 296);
	    lights.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
	    getContentPane().add(lights);
	    btnCreateRelationship.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    btnCreateRelationship.setBounds(10, 436, 200, 21);
	    getContentPane().add(btnCreateRelationship);
	    btnRemoveRelationship.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    btnRemoveRelationship.setBounds(10, 468, 200, 21);
	    getContentPane().add(btnRemoveRelationship);
	    lblButtonRoom.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    lblButtonRoom.setBounds(10, 338, 250, 37);
	    getContentPane().add(lblButtonRoom);	    
	    lblButtonRel.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    lblButtonRel.setVerticalAlignment(SwingConstants.TOP);
	    
	    lblButtonRel.setBounds(10, 388, 250, 37);
	    getContentPane().add(lblButtonRel);
	    lblLightRoom.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    lblLightRoom.setBounds(285, 338, 250, 37);
	    getContentPane().add(lblLightRoom);
	    lblLightRel.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    lblLightRel.setVerticalAlignment(SwingConstants.TOP);
	    
	    lblLightRel.setBounds(285, 388, 250, 131);
	    getContentPane().add(lblLightRel);
	    
	    JLabel lblButtons = new JLabel("Buttons");
	    lblButtons.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    lblButtons.setHorizontalAlignment(SwingConstants.CENTER);
	    lblButtons.setBounds(10, 11, 250, 26);
	    getContentPane().add(lblButtons);
	    
	    JLabel lblLights = new JLabel("Lights");
	    lblLights.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    lblLights.setHorizontalAlignment(SwingConstants.CENTER);
	    lblLights.setBounds(285, 11, 250, 26);
	    getContentPane().add(lblLights);
	    
	    JLabel lblRooms = new JLabel("Rooms");
	    lblRooms.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    lblRooms.setHorizontalAlignment(SwingConstants.CENTER);
	    lblRooms.setBounds(550, 17, 229, 14);
	    getContentPane().add(lblRooms);
	    rooms.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    rooms.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    rooms.setBounds(555, 36, 224, 188);
	    getContentPane().add(rooms);
	    btnChangeSelectedButton.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent e) {
	    	}
	    });
	    btnChangeSelectedButton.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    btnChangeSelectedButton.setBounds(555, 299, 224, 21);
	    getContentPane().add(btnChangeSelectedButton);
	    btnChangeSelectedLight.setFont(new Font("Tahoma", Font.PLAIN, 14));

	    btnChangeSelectedLight.setBounds(555, 331, 224, 21);
	    getContentPane().add(btnChangeSelectedLight);
	    newRoomId.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    newRoomId.setBounds(555, 235, 224, 20);
	    getContentPane().add(newRoomId);
	    newRoomId.setColumns(10);
	    btnCreateRoom.setFont(new Font("Tahoma", Font.PLAIN, 14));
	    
	    btnCreateRoom.setEnabled(false);
	    btnCreateRoom.setBounds(555, 261, 224, 21);
	    getContentPane().add(btnCreateRoom);
	    
	    this.setVisible(true);
	}
	
	public void setController(Controller controller) {
		this.controller = controller;
		this.initElements();
	}
	
	private void initElements() {		
		btnCreateRoom.setEnabled(true);
		
		btnCreateRoom.addActionListener(e -> {
			if(newRoomId.getText().isEmpty()) {
				return;
			}
			controller.createRoom(newRoomId.getText());
			newRoomId.setText("");			
		});
		
	    for (BasicDigitalTwin dt : controller.getDTsOf("dtmi:progettotesi:button;1")) {
	    	buttonsList.addElement(dt.getId());
		}
		buttons.setModel(buttonsList);
		
		for (BasicDigitalTwin dt : controller.getDTsOf("dtmi:progettotesi:light;1")) {
	    	lightsList.addElement(dt.getId());
		}
		lights.setModel(lightsList);
		
		for (BasicDigitalTwin dt : controller.getDTsOf("dtmi:progettotesi:room;1")) {
	    	roomsList.addElement(dt.getId());
		}
		rooms.setModel(roomsList);
		
		btnCreateRelationship.addActionListener(e -> {
			
			if(buttons.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a button");
				return;
			}
			if(lights.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a light");
				return;
			}
			controller.createRelationship(
					buttons.getSelectedValue(), 
					lights.getSelectedValue(), 
					"dtmi:progettotesi:button:control;1", 
					"control");
			
			updateRelLabels();
			
		});
		
		btnRemoveRelationship.addActionListener(e -> {
			
			if(buttons.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a button");
				return;
			}
			controller.deleteRelationship(buttons.getSelectedValue(), "dtmi:progettotesi:button:control;1");
			
			updateRelLabels();
				
		});
		
		btnChangeSelectedButton.addActionListener(e -> {
			
			if(buttons.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a button");
				return;
			}
			if(rooms.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a room");
				return;
			}
			
			controller.createRelationship(buttons.getSelectedValue(), rooms.getSelectedValue(), "dtmi:progettotesi:button:contained;1", "Contained");
			
			updateButtonRoomLabel();
		});
		
		btnChangeSelectedLight.addActionListener(e -> {
			
			if(lights.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a light");
				return;
			}
			if(rooms.getSelectedIndices().length != 1) {
				JOptionPane.showMessageDialog(null, "Select a room");
				return;
			}
			
			controller.createRelationship(lights.getSelectedValue(), rooms.getSelectedValue(), "dtmi:progettotesi:light:contained;1", "Contained");
			
			updateLightRoomLabel();
			
		});
		
		
		buttons.addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				updateButtonRoomLabel();
				
				updateButtonRelLabel();
			}
		});
		
		lights.addListSelectionListener(e -> {
			if(!e.getValueIsAdjusting()) {
				updateLightRoomLabel();
				
				updateLightRelLabel();
			}
		});
		
	}
	
	private void updateRelLabels() {
		updateButtonRelLabel();
		updateLightRelLabel();
	}
	
	private void updateButtonRoomLabel() {
		if(!buttons.isSelectionEmpty()) {
			Set<BasicRelationship> rel = controller.getRelBySourceAndName(buttons.getSelectedValue(), "dtmi:progettotesi:button:contained;1");
			if(!rel.isEmpty()) {
				String str = "";
				for (BasicRelationship r : rel) {
					str += r.getTargetId() + "; ";
				}
				lblButtonRoom.setText("Room: " + str);
			}else {
				lblButtonRoom.setText("Room: ");
			}
		}
	}
	
	private void updateLightRoomLabel() {
		if(!lights.isSelectionEmpty()) {
			Set<BasicRelationship> rel = controller.getRelBySourceAndName(lights.getSelectedValue(), "dtmi:progettotesi:light:contained;1");
			if(!rel.isEmpty()) {
				String str = "";
				for (BasicRelationship r : rel) {
					str += r.getTargetId() + "; ";
				}
				lblLightRoom.setText("Room: " + str);
			}else {
				lblLightRoom.setText("Room: ");
			}
		}
	}
	
	private void updateButtonRelLabel() {
		if(!buttons.isSelectionEmpty()) {
			Set<BasicRelationship> rel = controller.getRelBySourceAndName(buttons.getSelectedValue(), "dtmi:progettotesi:button:control;1");
			if(!rel.isEmpty()) {
				String str = "";
				for (BasicRelationship r : rel) {
					str += r.getTargetId() + "; ";
				}
				lblButtonRel.setText("Controls: " + str);
			}else {
				lblButtonRel.setText("Controls: ");
			}
		}
	}
	
	private void updateLightRelLabel() {
		if(!lights.isSelectionEmpty()) {
			Set<BasicRelationship> rel = controller.getRelByDestinationAndName(lights.getSelectedValue(), "dtmi:progettotesi:button:control;1");
			if(!rel.isEmpty()) {
				String str = "";
				for (BasicRelationship r : rel) {
					str += r.getSourceId() + ";<br>";
				}
				lblLightRel.setText("<html>Controlled by:<br>" + str);
			}else {
				lblLightRel.setText("Controlled by: ");
			}
		}
	}
	
	public void addDT(BasicDigitalTwin dt) {
		if(dt.getMetadata().getModelId().equals("dtmi:progettotesi:button;1")) {
			buttonsList.addElement(dt.getId());
			buttons.setModel(buttonsList);
		}else if(dt.getMetadata().getModelId().equals("dtmi:progettotesi:light;1")) {
			lightsList.addElement(dt.getId());
			lights.setModel(lightsList);
		}else if(dt.getMetadata().getModelId().equals("dtmi:progettotesi:room;1")) {
			roomsList.addElement(dt.getId());
			rooms.setModel(roomsList);
		}
	}
}
