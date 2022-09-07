package controller;

import java.util.Set;

import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.BasicRelationship;

import View.DTManagerView;
import controller.digitalTwin.DtManager;
import controller.mqtt.MQTTBroker;

public class Controller {

	private DTManagerView view;
	private final MQTTBroker mqttBroker;
	private final DtManager dtManager;
	
	public Controller() {
		dtManager = new DtManager(this);
		mqttBroker = new MQTTBroker(dtManager);
		mqttBroker.start();
	}
	
	public Set<BasicDigitalTwin> getAllDTs(){
		return dtManager.getAllDTs();
	}
	
	public Set<BasicDigitalTwin> getDTsOf(String model){
		return dtManager.getDTsOf(model);
	}
	
	public void deleteRelationship(String src, String rel) {
		dtManager.deleteRelationship(src, rel);
	}
	
	public void createRelationship(String src, String dest, String rel, String name) {
		dtManager.createRelationship(src, dest, rel, name);
	}
	
	public Set<BasicRelationship> getRelBySource(String src){
		return dtManager.getRelBySource(src);
	}
	
	public Set<BasicRelationship> getRelByDestination(String dst){
		return dtManager.getRelByDestination(dst);
	}
	
	public Set<BasicRelationship> getRelBySourceAndName(String src, String relId){
		return dtManager.getRelBySourceAndName(src, relId);
	}
	
	public Set<BasicRelationship> getRelByDestinationAndName(String dst, String relId){
		return dtManager.getRelByDestinationAndName(dst, relId);
	}

	public void setView(DTManagerView view) {
		this.view = view;
	}
	
	public void addDT(BasicDigitalTwin dt) {
		view.addDT(dt);
	}
	
	public void createRoom(String id) {
		dtManager.createDT(id, "dtmi:progettotesi:room;1");
	}
}
