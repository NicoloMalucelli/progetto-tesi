package digitalTwin;

import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import com.azure.core.credential.TokenCredential;
import com.azure.core.models.JsonPatchDocument;
import com.azure.digitaltwins.core.BasicDigitalTwin;
import com.azure.digitaltwins.core.BasicDigitalTwinMetadata;
import com.azure.digitaltwins.core.DigitalTwinsClient;
import com.azure.digitaltwins.core.DigitalTwinsClientBuilder;
import com.azure.identity.DefaultAzureCredentialBuilder;

import io.vertx.core.impl.ConcurrentHashSet;
import io.vertx.core.json.JsonObject;

public class DtManager {
	
	private static int TIMEOUT = 10000;
	
	private final DigitalTwinsClient dtClient;
	private final Set<String> dts = new ConcurrentHashSet<>();
	
	
	public DtManager() {
		//DT platform authentication
		
		TokenCredential credential = new DefaultAzureCredentialBuilder().build();
		
		dtClient = new DigitalTwinsClientBuilder()
				.credential(credential)
				.endpoint("https://DT-prova.api.weu.digitaltwins.azure.net")
				.buildClient();  
	}
	
	public void createDT(final String dtId, final String modelId) {
		
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
	
	public void react(String dtId) {
		//TODO eseguire le azioni corrispondenti al DT indicato
	}

	private boolean dtExist(String id) {
		return dts.contains(id);
	}
	
	//dtClient.createModels(dtdlModels);

}