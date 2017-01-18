package Verticles;

import Model.Position;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;

import java.util.function.Consumer;

/**
 * Created by SegFault on 18/01/2017.
 */
public class NexusVerticle extends AgentVerticle {
    public NexusVerticle(String owner_, String id_, Position position_) {
        super(owner_, id_, position_);
        logger = LogManager.getLogger(this.getClass());
        commands.put("spawn:probe", PROBE_SPAWNTIME + PROBE_SPAWNTIME_READYUP + OFFSET);
        commands.put("spawn:scout", SCOUT_SPAWNTIME + SCOUT_SPAWNTIME_READYUP + OFFSET);
        commands.put("spawn:beacon", BEACON_SPAWNTIME + BEACON_SPAWNTIME_READYUP + OFFSET);
        commands.put("spawn:templar", TEMPLAR_SPAWNTIME + TEMPLAR_SPAWNTIME_READYUP + OFFSET);
        commands.put("status", DEFAULT_STATUS_PRETIME + DEFAULT_STATUS_POSTTIME + OFFSET);
        commands.put("playerstatus", DEFAULT_PLAYERSTATUS_PRETIME + DEFAULT_PLAYERSTATUS_POSTTIME + 5000);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        logger.info("Nexus: {}, {}, Id{{}}", owner, position, id);
        makeDecision();
    }

    @Override
    public void makeDecision() {
        updateResources(resources -> {
            int biomass = resources.getInteger("biomass");
            int minerals = resources.getInteger("minerals");
            if (biomass >= PROBE_BIOMASS && minerals >= PROBE_MINERAL) {
                spawn("probe");
            } else {
                logger.error("not enough resources");
            }
        });
    }

    private void updateResources(Consumer<JsonObject> callback) {
        logger.info("updating resources...");
        int timer = commands.get("playerstatus");
        post("/command/" + owner + "/" + id + "/playerstatus", json -> {
            String reportId = json.getString("reportId");
            vertx.setTimer(timer, __ -> get("/report/" + reportId, json2 -> {
                logger.info("resources: {}", json2);
                callback.accept(json2);
            }));
        });
    }

    private void spawn(String agent) {
        logger.info("spawn " + agent + ": starting...");
        int timer = commands.get("spawn:" + agent);
        post("/command/" + owner + "/" + id + "/spawn:" + agent, json -> {
            String reportId = json.getString("reportId");
            vertx.setTimer(timer, __ -> get("/report/" + reportId, json2 -> {
                logger.info("spawn report: {}", json2);
                if (!json2.getString("error").equals("")) {
                    logger.error("report fail");
                    return;
                }
                vertx.eventBus().send("/spawn/" + agent, json2);
                makeDecision();
            }));
        });
    }
}
