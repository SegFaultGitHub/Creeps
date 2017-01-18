import Model.Position;
import Verticles.NexusVerticle;
import Verticles.ProbeVerticle;
import Verticles.ScoutVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Created by SegFault on 18/01/2017.
 */
public class Manager {
    private Logger logger;
    private int port = 1337;
    private String host = "localhost";


    public Manager() {
        logger = LogManager.getLogger(Manager.class);
    }

    public void execute() {
        VertxOptions vertxOptions = new VertxOptions();
        vertxOptions.setBlockedThreadCheckInterval(1000 * 60 * 60);
        vertxOptions.setClusterPublicHost(host);
        vertxOptions.setClusterPublicPort(port);
        Vertx vertx = Vertx.vertx(vertxOptions);

        HttpClientOptions httpClientOptions = new HttpClientOptions();
        httpClientOptions.setDefaultPort(port);
        httpClientOptions.setDefaultHost(host);
        HttpClient client = vertx.createHttpClient(httpClientOptions);

        vertx.eventBus().consumer("/init", message -> client.getNow("/init/" + message.body(), response -> response.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            logger.info("user: {}", json);
            newUser(json, vertx);
        })));

        vertx.eventBus().consumer("/spawn/probe", message -> {
            JsonObject json = (JsonObject) message.body();
            String owner = json.getString("login");
            String id = json.getString("id");
            JsonObject location = json.getJsonObject("location");
            Position position = new Position(
                    location.getInteger("x"),
                    location.getInteger("y"),
                    location.getInteger("z")
            );
            String type = json.getString("agentType");
            switch (type) {
                case "probe":
                    vertx.deployVerticle(new ProbeVerticle(
                            owner,
                            id,
                            position
                    ), new DeploymentOptions().setWorker(true));
                    break;
                case "scout":
                    vertx.deployVerticle(new ScoutVerticle(
                            owner,
                            id,
                            position
                    ), new DeploymentOptions().setWorker(true));
                    break;
                case "nexus":
                    vertx.deployVerticle(new ProbeVerticle(
                            owner,
                            id,
                            position
                    ), new DeploymentOptions().setWorker(true));
                    break;
                case "templar":
                    vertx.deployVerticle(new ScoutVerticle(
                            owner,
                            id,
                            position
                    ), new DeploymentOptions().setWorker(true));
                    break;
                default:
                    logger.error("unable to spawn");
                    break;
            }
        });
        init(vertx, "111111111111111");
    }

    private void init(Vertx vertx, String login) {
        vertx.eventBus().send("/init", login);
    }

    private void newUser(JsonObject json, Vertx vertx) {
        String error = json.getString("error");
        if (error != null) {
            logger.error("new user: {}", error);
            return;
        }
        String owner = json.getString("login");
        String probeId = json.getString("probeId");
        String nexusId = json.getString("baseId");
        Position position = new Position(
                json.getInteger("startX"),
                json.getInteger("startY"),
                json.getInteger("startZ")
        );
        vertx.deployVerticle(new ProbeVerticle(
                owner,
                probeId,
                position
        ), new DeploymentOptions().setWorker(true));
        vertx.deployVerticle(new NexusVerticle(
                owner,
                nexusId,
                position
        ), new DeploymentOptions().setWorker(true));
    }
}
