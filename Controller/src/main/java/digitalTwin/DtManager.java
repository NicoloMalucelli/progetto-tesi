package digitalTwin;

import java.awt.dnd.DragGestureEvent;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

import javax.sound.sampled.TargetDataLine;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.models.JsonPatchDocument;
import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.BasicDigitalTwinMetadata;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;
import mqtt.MQTTBroker;

public class DtManager {
	
	private static int TIMEOUT = 10000;
	
	private MQTTBroker mqttBroker;
	
	private final DigitalTwinsClient dtClient;
	private final Set<String> dts = new ConcurrentHashSet<>();
	private final ConcurrentHashMap<String, Reaction> reactions = new ConcurrentHashMap<>();
	
	
	public DtManager() {
		//DT platform authentication
		TokenCredential credential = new DefaultAzureCredentialBuilder().build();
		
		dtClient = new DigitalTwinsClientBuilder()
				.credential(credential)
				.endpoint("https://DT-prova.api.weu.digitaltwins.azure.net")
				.buildClient();
		
		dts.addAll(getDTsId());
	}
	
	public void setMQTTBroker(MQTTBroker mqttBroker) {
		this.mqttBroker = mqttBroker;
		
		//just for test purpose
		reactions.put("button-01", new Reaction() {
			
			@Override
			public void react(JsonObject shadowPayload) {
				if(shadowPayload.containsKey("isPressed")) {
					JsonObject msgPayload = new JsonObject();
					if(shadowPayload.getBoolean("isPressed")) {
						msgPayload.put("op", "on");
					}else {
						msgPayload.put("op", "off");
					}
					mqttBroker.publish("action/light-01", msgPayload);
				}
			}
			
		});
	}
	
	public void createDT(final String dtId, final String modelId) {
		if(dts.contains(dtId)) {
			return;
		}
		new Thread(() -> {
			BasicDigitalTwin basicTwin = new BasicDigitalTwin(dtId)
				.setMetadata(
					new BasicDigitalTwinMetadata()
					.setModelId(modelId)
				);

			BasicDigitalTwin createdTwin = dtClient.createOrReplaceDigitalTwin(
				basicTwin.getId(),
				basicTwin,
				BasicDigitalTwin.class);
			
			dts.add(dtId);

			System.out.println("Created digital twin with Id: " + createdTwin.getId());
		}).start();
	}
	
	public void initDT(final String dtId, final JsonObject payload) {
		new Thread(() -> {
			final JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			payload.forEach(e -> {
				jsonPatchDocument.appendAdd("/" + e.getKey(), e.getValue());
			});

			System.out.println(jsonPatchDocument);
			
			dtClient.updateDigitalTwin(
					 dtId,
				     jsonPatchDocument);
			
			System.out.println(dtId + " has been shadowed: " + payload);
		}).start();
	}
	
	public void shadowDT(final String dtId, final JsonObject payload) {
		new Thread(() -> {
			
			BasicDigitalTwin dt;
			try {
				dt = this.getDt(dtId);
			} catch (TimeoutException e1) {
				System.out.println(e1);
				return;
			}
			
			final JsonPatchDocument jsonPatchDocument = new JsonPatchDocument();
			payload.forEach(e -> {
				if(dt.getContents().containsKey(e.getKey())) {
					jsonPatchDocument.appendReplace("/" + e.getKey(), e.getValue());
				} else {
					jsonPatchDocument.appendAdd("/" + e.getKey(), e.getValue());
				}
				
			});
			
			dtClient.updateDigitalTwin(
					 dtId,
				     jsonPatchDocument);
			
			System.out.println(dtId + " has been shadowed: " + payload);
			
			this.react(dtId, payload);
			
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
		/*
		Process p = Runtime.getRuntime().exec("python ");
		BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line = "";
		while ((line = reader.readLine()) != null) {
		    System.out.println(line + "\n");
		}
		*/
		if(reactions.keySet().contains(dtId)) {
			System.out.println("reacting");
			reactions.get(dtId).react(payload);
		}
	}

	private boolean dtExist(String id) {
		return dts.contains(id);
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
		dtClient.deleteRelationship(src, rel);
	}
	
	public void createRelationship(String src, String dest) {
		
	}
	
	//dtClient.createModels(dtdlModels);

}
