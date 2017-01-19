package Verticles;

import Model.BlockValue;
import Model.Position;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * Created by SegFault on 18/01/2017.
 */
public abstract class AgentVerticle extends AbstractVerticle {
    //<editor-fold desc="Constants time">
    // Conf ------------------------------------------------------------------------------------------------------------

    protected final int TICKRATE = 20;
    protected final int TIMEOUT = 30 * 1000;
    protected final int ALLOWED_MISSES = 100;
    protected final int PLAYER_START_BIOMASS = 200;
    protected final int PLAYER_START_MINERALS = 200;


    // Spawns ----------------------------------------------------------------------------------------------------------

    protected final int PROBE_MINERAL = 50;
    protected final int PROBE_BIOMASS = 5;
    protected final int PROBE_SPAWNTIME = TICKRATE * 15;
    protected final int PROBE_SPAWNTIME_READYUP = TICKRATE * 0;

    protected final int SCOUT_MINERAL = 150;
    protected final int SCOUT_BIOMASS = 0;
    protected final int SCOUT_SPAWNTIME = TICKRATE * 5;
    protected final int SCOUT_SPAWNTIME_READYUP = TICKRATE * 0;

    protected final int TEMPLAR_MINERAL = 100;
    protected final int TEMPLAR_BIOMASS = 300;
    protected final int TEMPLAR_SPAWNTIME = TICKRATE * 30;
    protected final int TEMPLAR_SPAWNTIME_READYUP = TICKRATE * 0;

    protected final int BEACON_MINERAL = 200;
    protected final int BEACON_BIOMASS = 500;
    protected final int BEACON_SPAWNTIME = TICKRATE * 30;
    protected final int BEACON_SPAWNTIME_READYUP = TICKRATE * 0;

    protected final int NEXUS_MINERAL = 300;
    protected final int NEXUS_BIOMASS = 300;
    protected final int NEXUS_SPAWNTIME = TICKRATE * 15;
    protected final int NEXUS_SPAWNTIME_READYUP = TICKRATE * 0;


    // Actions ---------------------------------------------------------------------------------------------------------

    protected final int NOOP_POSTTIME = TICKRATE * 0;
    protected final int NOOP_PRETIME = TICKRATE * 1;

    protected final int DEFAULT_CONVERT_POSTTIME = TICKRATE * 0;
    protected final int DEFAULT_CONVERT_PRETIME = TICKRATE / 2;

    protected final int DEFAULT_MINE_POSTTIME = TICKRATE * 0;
    protected final int DEFAULT_MINE_PRETIME = TICKRATE * 1;

    protected final int DEFAULT_MOVE_POSTTIME = TICKRATE * 0;
    protected final int DEFAULT_MOVE_PRETIME = TICKRATE / 2;

    protected final int DEFAULT_PLAYERSTATUS_POSTTIME = TICKRATE / 2;
    protected final int DEFAULT_PLAYERSTATUS_PRETIME = TICKRATE / 2;

    protected final int DEFAULT_SCAN3_POSTTIME = TICKRATE / 2;
    protected final int DEFAULT_SCAN3_PRETIME = TICKRATE / 2;

    protected final int DEFAULT_SCAN5_POSTTIME = TICKRATE;
    protected final int DEFAULT_SCAN5_PRETIME = TICKRATE;

    protected final int DEFAULT_SCAN9_POSTTIME = TICKRATE * 2;
    protected final int DEFAULT_SCAN9_PRETIME = TICKRATE * 2;

    protected final int DEFAULT_STATUS_POSTTIME = TICKRATE / 4;
    protected final int DEFAULT_STATUS_PRETIME = TICKRATE / 4;

    protected final int DEFAULT_SPHERE_POSTTIME = TICKRATE * 5;
    protected final int DEFAULT_SPHERE_PRETIME = TICKRATE * 5;

    protected final int DEFAULT_IONDISCHARGE_POSTTIME = TICKRATE * 5;
    protected final int DEFAULT_IONDISCHARGE_PRETIME = TICKRATE * 5;

    protected final int DEFAULT_ORBITALLASER_POSTTIME = TICKRATE * 10;
    protected final int DEFAULT_ORBITALLASER_PRETIME = TICKRATE * 10;

    protected final int OFFSET = 100;
    //</editor-fold>
    //<editor-fold desc="Constants block">
    protected Map<String, BlockValue> blockValues;
    //</editor-fold>

    protected String owner;
    protected String id;
    protected Position position;
    protected Map<String, Integer> commands;
    protected HttpClient client;
    protected Logger logger;

    protected AgentVerticle(String owner_, String id_, Position position_) {
        owner = owner_;
        id = id_;
        position = position_;
        commands = new ConcurrentHashMap<>();
        blockValues = new HashMap<>();
    }

    protected void makeDecision() { }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        HttpClientOptions options = new HttpClientOptions();
        options.setDefaultPort(1337);
        options.setDefaultHost("localhost");
        client = vertx.createHttpClient(options);

        //<editor-fold desc="Constants block">
        blockValues.put("SAND", new BlockValue(3, 5));
        blockValues.put("COBBLESTONE", new BlockValue(0, 2));
        blockValues.put("STONE", new BlockValue(10, 20));
        blockValues.put("COAL_ORE", new BlockValue(100, 50));
        blockValues.put("IRON_ORE", new BlockValue(0, 100));
        blockValues.put("GOLD_ORE", new BlockValue(0, 150));
        blockValues.put("LAPIS_ORE", new BlockValue(0, 200));
        blockValues.put("REDSTONE_ORE", new BlockValue(25, 150));
        blockValues.put("DIAMOND_ORE", new BlockValue(0, 500));
        blockValues.put("OBSIDIAN", new BlockValue(250, 250));
        blockValues.put("BEDROCK", new BlockValue(-100000, -100000));
        blockValues.put("WOOL", new BlockValue(100, 100));

        blockValues.put("DIRT", new BlockValue(10, 0));
        blockValues.put("GRASS", new BlockValue(20, 0));
        blockValues.put("GRASS_PATH", new BlockValue(20, 0));
        blockValues.put("LONG_GRASS", new BlockValue(30, 0));
        blockValues.put("LEAVES", new BlockValue(50, 0));
        blockValues.put("LEAVES_2", new BlockValue(50, 0));
        blockValues.put("WOOD", new BlockValue(100, 0));
        blockValues.put("CACTUS", new BlockValue(100, 0));
        blockValues.put("DEAD_BUSH", new BlockValue(10, 0));
        blockValues.put("RED_ROSE", new BlockValue(100, 0));
        blockValues.put("YELLOW_FLOWER", new BlockValue(100, 0));
        blockValues.put("CHORUS_FLOWER", new BlockValue(100, 0));
        blockValues.put("LOG", new BlockValue(100, 0));
        blockValues.put("LOG_2", new BlockValue(100, 0));
        blockValues.put("PUMPKIN", new BlockValue(100, 0));

        blockValues.put("WATER", new BlockValue(-1, -1));
        blockValues.put("STATIONARY_WATER", new BlockValue(-100, -100));
        blockValues.put("STATIONARY_LAVA", new BlockValue(-500, -500));
        blockValues.put("LAVA", new BlockValue(-500, -500));
        blockValues.put("TNT", new BlockValue(-500, -500));
        blockValues.put("AIR", new BlockValue(0, 0));
        //</editor-fold>
    }

    @Override
    public String toString() {
        return super.toString();
    }

    protected void get(String query, Consumer<JsonObject> callback) {
        client.getNow(query, response -> response.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            callback.accept(json);
        }));
    }

    protected void post(String query, Consumer<JsonObject> callback) {
        client.post(query, response -> response.bodyHandler(buffer -> {
            JsonObject json = buffer.toJsonObject();
            callback.accept(json);
        }))
                .putHeader("content-type", "application/json")
                .end(Json.encode(new JsonObject()));
    }

    protected int getBlockValue(String blockType) {
        if (!blockValues.keySet().contains(blockType)) {
            return -100;
        }
        return blockValues.get(blockType).getValue();
    }

    protected void reportAndCallback(int timer, String reportId, String caller, Runnable callback) {
        vertx.setTimer(timer, __ -> get("/report/" + reportId, json -> {
            if (json.getString("error") == null || json.getString("error").isEmpty()) {
                callback.run();
//            } else if (json.getString("agentStatus") != null || !json.getString("agentStatus").equals("alive")) {
//                logger.info("{}: death of agent {}", caller, id);
//                vertx.undeploy(this.deploymentID());
            }
            else {
                logger.info("Retrying ({}): {}", caller, json);
                reportAndCallback(1000, reportId, caller, callback);
            }
        }));
    }

    protected void reportAndCallbackArgs(int timer, String reportId, String caller, Consumer<JsonObject> callback) {
        vertx.setTimer(timer, __ -> get("/report/" + reportId, json -> {
            if (json.getString("error") == null || json.getString("error").isEmpty()) {
                callback.accept(json);
//            } else if (json.getString("agentStatus") != null || !json.getString("agentStatus").equals("alive")) {
//                logger.info("{}: death of agent {}", caller, id);
//                vertx.undeploy(this.deploymentID());
            } else {
                logger.info("Retrying ({}): {}", caller, json);
                reportAndCallbackArgs(1000, reportId, caller, callback);
            }
        }));
    }
}
