package controller.digitalTwin;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.models.JsonPatchDocument;
import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.BasicDigitalTwinMetadata;
import com.azure.digitaltwins.core.BasicRelationship;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.digitaltwins.core.models.DigitalTwinsModelData;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import controller.mqtt.MQTTBroker;

import controller.Controller;

public class DtManager {
	
	private static int TIMEOUT = 10000;
	private static int KEEP_ALIVE_TIME_MIN = 5;
	
	private final Controller controller;
	private MQTTBroker mqttBroker;
	
	private final DigitalTwinsClient dtClient;
	private final Map<String, LocalDateTime> dtsLastCom = new ConcurrentHashMap<>();
	private final Set<BasicRelationship> relationships = new ConcurrentHashSet<>();
	private final Set<String> disconnectedDTs = new ConcurrentHashSet<>();
	
	public DtManager(Controller controller) {
		this.controller = controller;
		//DT platform authentication
		TokenCredential credential = new DefaultAzureCredentialBuilder().build();
		
		dtClient = new DigitalTwinsClientBuilder()
				.credential(credential)
				.endpoint("https://DT-prova.api.weu.digitaltwins.azure.net")
				.buildClient();
		
		for (String dt : getDTsId()) {
			dtsLastCom.put(dt, LocalDateTime.of(2000, 11, 18, 13, 30));
		}
		
		this.synchRelationships();
		
		startCheckConnectionRoutine();
	}
	
	public void setMQTTBroker(MQTTBroker mqttBroker) {
		this.mqttBroker = mqttBroker;
	}
	
	public void createDT(final String dtId, final String modelId) {
		if(dtExist(dtId)) {
			return;
		}
		new Thread(() -> {
			
			DigitalTwinsModelData model = dtClient.getModel(modelId);
			JsonObject dtdl = new JsonObject(model.getDtdlModel());
			
			final JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			
			JsonArray contents = dtdl.getJsonArray("contents");
			for(int i = 0; i < contents.size(); i++) {
				JsonObject content = contents.getJsonObject(i);
				if(content.getString("@type").equals("Property")) {
					if(content.getString("schema").equals("boolean")) {
						if(content.getString("name").equals("alive")) {
							jsonPatchDocument.appendAdd("/alive", true);
						}else {
							jsonPatchDocument.appendAdd("/" + content.getString("name"), false);
						}
					}//else if().... other schemas
				}
			}
			
			BasicDigitalTwin basicTwin = new BasicDigitalTwin(dtId)
				.setMetadata(
					new BasicDigitalTwinMetadata()
					.setModelId(modelId)
				);

			BasicDigitalTwin createdTwin = dtClient.createOrReplaceDigitalTwin(
				basicTwin.getId(),
				basicTwin,
				BasicDigitalTwin.class);
			
			dtsLastCom.put(dtId, LocalDateTime.now());
			
			controller.addDT(createdTwin);

			System.out.println("Created digital twin with Id: " + createdTwin.getId());
			
			//jsonPatchDocument.appendAdd("/alive", true);
			
			dtClient.updateDigitalTwin(
					 dtId,
					 jsonPatchDocument);
			
			dtsLastCom.put(dtId, LocalDateTime.now());
			
			//System.out.println(jsonPatchDocument);
		}).start();
	}
	
	public void shadowDT(final String dtId, final JsonObject payload) {
		new Thread(() -> {
			
			final JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			
			dtClient.updateDigitalTwin(
					 dtId,
				     jsonPatchDocument);
			
			System.out.println(dtId + " has been shadowed: " + payload);
			
			this.react(dtId, payload);
			
			keepAlive(dtId);
		}).start();
	}
	
	public BasicDigitalTwin getDt(String id) throws TimeoutException {
		long startTime = System.currentTimeMillis();
		
		while(!dtExist(id)) {
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if(System.currentTimeMillis() - startTime >= TIMEOUT) {
				throw new TimeoutException("DT not found");
			}
		}
		
		return dtClient.getDigitalTwin(id, BasicDigitalTwin.class);
	}
	
	public void react(String dtId, JsonObject payload) {
		Set<BasicRelationship> rels = getRelBySourceAndName(dtId, "dtmi:progettotesi:button:control;1");
		if(payload.containsKey("isPressed")) {
			JsonObject msgPayload = new JsonObject();
			if(payload.getBoolean("isPressed")) {
				msgPayload.put("op", "on");
			}else {
				msgPayload.put("op", "off");
			}
			for (BasicRelationship rel : rels) {
				mqttBroker.publish("action/" + rel.getTargetId(), msgPayload);
			}
		}
	}

	private boolean dtExist(String id) {
		return dtsLastCom.keySet().contains(id);
	}
	
	private Set<String> getDTsId(){
		String query = "SELECT T.$dtId FROM DIGITALTWINS T";
		PagedIterable<BasicDigitalTwin> res = dtClient.query(query, BasicDigitalTwin.class);
		Set<String> out = new HashSet<>();
		for (BasicDigitalTwin dt : res) {
			out.add(dt.getId());
		}
		return out;
	}
	
	public Set<BasicDigitalTwin> getAllDTs(){
		String query = "SELECT * FROM DIGITALTWINS T";
		PagedIterable<BasicDigitalTwin> res = dtClient.query(query, BasicDigitalTwin.class);
		Set<BasicDigitalTwin> out = new HashSet<>();
		for (BasicDigitalTwin dt : res) {
			out.add(dt);
		}
		return out;
	}
	
	public Set<BasicDigitalTwin> getDTsOf(String model){
		return getAllDTs().stream()
				.filter(dt -> dt.getMetadata().getModelId().equals(model))
				.collect(Collectors.toSet());
	}
	
	public void deleteRelationship(String src, String rel) {
		if(getRelBySourceAndName(src, rel).size() == 0) {
			return;
		}
		dtClient.deleteRelationship(src, rel);
		relationships.remove(getRelBySourceAndName(src, rel).toArray()[0]);
	}
	
	public void createRelationship(String src, String dest, String rel, String name) {
		BasicRelationship relationship = new BasicRelationship(rel, src, dest, name);
		
		if(!getRelBySourceAndName(src, rel).isEmpty()) {
			deleteRelationship(src, rel);
		}
		

		dtClient.createOrReplaceRelationship(
		     src,
		     rel,
		     relationship,
		     BasicRelationship.class);
		
		relationships.add(relationship);
	}
	
	private void synchRelationships() {
		for (String dt : dtsLastCom.keySet()) {
			PagedIterable<BasicRelationship> pagedRelationshipsByItem = dtClient.listRelationships(
				     dt,
				     BasicRelationship.class);
			
				for (BasicRelationship rel : pagedRelationshipsByItem) {
				    relationships.add(rel);
				}
		}
	}
	
	public Set<BasicRelationship> getRelBySource(String src){
		return relationships.stream().
				filter(r -> r.getSourceId().equals(src))
				.collect(Collectors.toSet());
	}
	
	public Set<BasicRelationship> getRelByDestination(String dst){
		return relationships.stream().
				filter(r -> r.getTargetId().equals(dst))
				.collect(Collectors.toSet());
	}
	
	public Set<BasicRelationship> getRelBySourceAndName(String src, String relId){
		return relationships.stream()
				.filter(r -> r.getSourceId().equals(src))
				.filter(r -> r.getId().equals(relId))
				.collect(Collectors.toSet());
	}
	
	public Set<BasicRelationship> getRelByDestinationAndName(String dst, String relId){
		return relationships.stream()
				.filter(r -> r.getTargetId().equals(dst))
				.filter(r -> r.getId().equals(relId))
				.collect(Collectors.toSet());
	}
	
	public void keepAlive(String dtId) {
		if(disconnectedDTs.contains(dtId)) {
			disconnectedDTs.remove(dtId);
		
			final JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			jsonPatchDocument.appendReplace("/alive", true);
			
			dtClient.updateDigitalTwin(
					 dtId,
				     jsonPatchDocument);
			
			System.out.println(dtId + " is alive");
			
		}
		dtsLastCom.put(dtId, LocalDateTime.now());
	}
	
	private void startCheckConnectionRoutine() {
		Timer t = new Timer();
		t.schedule(new TimerTask() {
		    @Override
		    public void run() {
		    	for (Entry<String, LocalDateTime> e : dtsLastCom.entrySet()) {
					if(disconnectedDTs.contains(e.getKey())) {
						return;
					}
					if(Duration.between(e.getValue(), LocalDateTime.now()).toMinutes() > KEEP_ALIVE_TIME_MIN) {
						disconnectedDTs.add(e.getKey());

						final JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
						
						jsonPatchDocument.appendReplace("/alive", false);
						
						dtClient.updateDigitalTwin(
								 e.getKey(),
							     jsonPatchDocument);
						
						System.out.println(e.getKey() + " is not alive");
						
					}
				}
		    }
		}, 0, 60000);
	}
}
