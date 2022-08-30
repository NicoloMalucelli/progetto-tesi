package controller;

import java.util.Set;
import java.util.stream.Collectors;

import com.azure.digitaltwins.core.BasicDigitalTwin;

import digitalTwin.DtManager;
import mqtt.MQTTBroker;

public class Controller {

	private final MQTTBroker mqttBroker;
	private final DtManager dtManager;
	
	public Controller() {
		dtManager = new DtManager();
		mqttBroker = new MQTTBroker(dtManager);
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
	
	public void createRelationship(String src, String dest) {
		dtManager.createDT(src, dest);
	}
}
