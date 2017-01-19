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
    private boolean saving = false;
    private int probes = 0;

    public NexusVerticle(String owner_, String id_, Position position_) {
        super(owner_, id_, position_);
        logger = LogManager.getLogger(this.getClass());
        commands.put("spawn:probe", (1000 / TICKRATE) * (PROBE_SPAWNTIME + PROBE_SPAWNTIME_READYUP) + OFFSET);
        commands.put("spawn:scout", (1000 / TICKRATE) * (SCOUT_SPAWNTIME + SCOUT_SPAWNTIME_READYUP) + OFFSET);
        commands.put("spawn:beacon", (1000 / TICKRATE) * (BEACON_SPAWNTIME + BEACON_SPAWNTIME_READYUP) + OFFSET);
        commands.put("spawn:templar", (1000 / TICKRATE) * (TEMPLAR_SPAWNTIME + TEMPLAR_SPAWNTIME_READYUP) + OFFSET);
        commands.put("status", (1000 / TICKRATE) * (DEFAULT_STATUS_PRETIME + DEFAULT_STATUS_POSTTIME) + OFFSET);
        commands.put("playerstatus", (1000 / TICKRATE) * (DEFAULT_PLAYERSTATUS_PRETIME + DEFAULT_PLAYERSTATUS_POSTTIME) + OFFSET);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        logger.info("Nexus: {}, {}, Id{{}}", owner, position, id);
        makeDecision();
    }

    @Override
    public void makeDecision() {
        getResources(resources -> {
            logger.info("nexus {} resources: {}", id, resources);
            int biomass = resources.getInteger("biomass");
            int minerals = resources.getInteger("minerals");
            if (!saving && biomass >= PROBE_BIOMASS && minerals >= PROBE_MINERAL) {
                spawn("probe");
            } else {
                logger.error("not enough resources");
                vertx.setTimer(10000, __ -> makeDecision());
            }
        });
    }

    private void getResources(Consumer<JsonObject> callback) {
        int timer = commands.get("playerstatus");
        post("/command/" + owner + "/" + id + "/playerstatus", json -> {
            String reportId = json.getString("reportId");
            reportAndCallbackArgs(timer, reportId, "NexusVerticle:getResources", json2 -> {
                logger.info("resources: {}", json2);
                callback.accept(json2);
            });
        });
    }

    private void spawn(String agent) {
        logger.info("nexus {}: spawn {}", id, agent);
        int timer = commands.get("spawn:" + agent);
        post("/command/" + owner + "/" + id + "/spawn:" + agent, json -> {
            String reportId = json.getString("reportId");
            reportAndCallbackArgs(timer, reportId, "NexusVerticle:spawn", json2 -> {
                vertx.eventBus().send("/spawn/" + agent, json2, __ -> {
                    if (__.succeeded()) {
                        probes++;
                        if (probes >= 3) {
                            logger.info("probe {}: start saving...", id);
                            saving = true;
                            vertx.eventBus().send("/spawnNexus", true);
                        }
                    }
                });
                makeDecision();
            });
        });
    }
}
