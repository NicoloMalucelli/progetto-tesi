package digitalTwin;

import io.vertx.core.json.JsonObject;

public interface Reaction {
	
	public void react(JsonObject payload);
	
}
