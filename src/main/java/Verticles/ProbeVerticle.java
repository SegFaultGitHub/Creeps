package Verticles;

import Model.Position;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import javafx.util.Pair;
import org.apache.logging.log4j.LogManager;

import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

/**
 * Created by SegFault on 18/01/2017.
 */
public class ProbeVerticle extends AgentVerticle {
    private Queue<Pair<Position, Boolean>> path;
    private boolean shouldSpawnNexus = false;

    private int favoriteX, favoriteZ;

    public ProbeVerticle(String owner_, String id_, Position position_) {
        super(owner_, id_, position_);
        path = new ConcurrentLinkedQueue<>();
        logger = LogManager.getLogger(this.getClass());
        commands.put("moveup", (1000 / TICKRATE) * (DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME) + OFFSET);
        commands.put("movedown", (1000 / TICKRATE) * (DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME) + OFFSET);
        commands.put("movenorth", (1000 / TICKRATE) * (DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME) + OFFSET);
        commands.put("movesouth", (1000 / TICKRATE) * (DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME) + OFFSET);
        commands.put("movewest", (1000 / TICKRATE) * (DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME) + OFFSET);
        commands.put("moveeast", (1000 / TICKRATE) * (DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME) + OFFSET);
        commands.put("scan", (1000 / TICKRATE) * (DEFAULT_SCAN3_PRETIME + DEFAULT_SCAN3_POSTTIME) + OFFSET);
        commands.put("scan5", (1000 / TICKRATE) * (DEFAULT_SCAN5_PRETIME + DEFAULT_SCAN5_POSTTIME) + OFFSET);
        commands.put("convert", (1000 / TICKRATE) * (DEFAULT_CONVERT_PRETIME + DEFAULT_CONVERT_POSTTIME) + OFFSET);
        commands.put("mine", (1000 / TICKRATE) * (DEFAULT_MINE_PRETIME + DEFAULT_MINE_POSTTIME) + OFFSET);
        commands.put("status", (1000 / TICKRATE) * (DEFAULT_STATUS_PRETIME + DEFAULT_STATUS_POSTTIME) + OFFSET);

        Random random = new Random();
        favoriteX = random.nextBoolean() ? -1 : 1;
        favoriteZ = random.nextBoolean() ? -1 : 1;
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        logger.info("Probe: Owner{{}}, {}, Id{{}}", owner, position, id);
        vertx.setTimer((1000 / TICKRATE) * PROBE_SPAWNTIME_READYUP + OFFSET, __ ->
                makeDecision()
        );
    }

    @Override
    protected void makeDecision() {
        getStatus(jsonObject -> {
            JsonObject block = jsonObject.getJsonObject("block");
            position = new Position(
                    block.getInteger("x"),
                    block.getInteger("y"),
                    block.getInteger("z")
            );
            if (!jsonObject.getString("alive").equals("alive")) {
                logger.info("probe {}: dead", id);
                vertx.undeploy(this.deploymentID());
                return;
            }
            if (shouldSpawnNexus) {

            } else {
                if (path.isEmpty()) {
                    logger.info("probe {} status: {}", jsonObject);
                    scan5();
                } else {
                    Pair<Position, Boolean> next = path.poll();
                    Runnable then = this::makeDecision;
                    if (path.isEmpty() && next.getValue()) {
                        then = this::mine;
                    } else if (next.getValue()) {
                        then = this::convert;
                    }
                    if (next.getKey().getY() > position.getY()) {
                        moveAnd("up", then);
                    } else if (next.getKey().getY() < position.getY()) {
                        moveAnd("down", then);
                    } else if (next.getKey().getX() > position.getX()) {
                        moveAnd("east", then);
                    } else if (next.getKey().getX() < position.getX()) {
                        moveAnd("west", then);
                    } else if (next.getKey().getZ() > position.getZ()) {
                        moveAnd("south", then);
                    } else if (next.getKey().getZ() < position.getZ()) {
                        moveAnd("north", then);
                    }
                }
            }
        });
    }

    private void getStatus(Consumer<JsonObject> callback) {
        int timer = commands.get("status");
        post("/command/" + owner + "/" + id + "/status", json -> {
            String reportId = json.getString("reportId");
            reportAndCallbackArgs(timer, reportId, "ProbeVerticle:getStatus", callback);
        });
    }

    //<editor-fold desc="Actions">
    private void moveAnd(String direction, Runnable then) {
        int timer = commands.get("move" + direction);
//        logger.info("probe {}: move {}", id, direction);
        post("/command/" + owner + "/" + id + "/move" + direction, json -> {
            String reportId = json.getString("reportId");
            reportAndCallbackArgs(timer, reportId, "ProbeVerticle:moveAnd", json2 -> then.run());
        });
    }

    private void mine() {
        logger.info("probe {}: mine", id);
        int timer = commands.get("mine");
        post("/command/" + owner + "/" + id + "/mine", json -> {
            String reportId = json.getString("reportId");
            reportAndCallback(timer, reportId, "mine", this::makeDecision);
        });
    }

    private void convert() {
        int timer = commands.get("convert");
        logger.info("probe {}: convert", id);
        post("/command/" + owner + "/" + id + "/convert", json -> {
            String reportId = json.getString("reportId");
            reportAndCallbackArgs(timer, reportId, "ProbeVerticle:convert", json2 -> makeDecision());
        });
    }
    //</editor-fold>

    //<editor-fold desc="Scan">
    private Position getBestPosition(JsonObject info) {
        int maxDistance = 0;
        int maxValue = -10000;
        int maxFav = 0;
        Position goal = position;
        for (String key : info.getMap().keySet()) {
            JsonObject obj = info.getJsonObject(key);
            String type = obj.getString("type");
            int value = getBlockValue(type);
            if (obj.getString("player").equals(owner)) {
                value = -200;
            }
            Position pos = new Position(
                    obj.getInteger("x"),
                    obj.getInteger("y"),
                    obj.getInteger("z")
            );
            int distance = pos.distance(position);
            int fav = 0;
            fav += (int) Math.signum(pos.getX() - goal.getX()) == favoriteX ? 1 : 0;
            fav += (int) Math.signum(pos.getZ() - goal.getZ()) == favoriteZ ? 1 : 0;
            if (value > maxValue || (value >= maxValue && pos.getY() >= goal.getY() && fav >= maxFav && distance > maxDistance)) {
                maxDistance = distance;
                maxValue = value;
                goal = pos;
                maxFav = fav;
            }
        }
        return goal;
    }

    private void makePath(JsonObject info, Position goal) {
        int direction = (int) Math.signum(goal.getY() - position.getY());
        int currY = position.getY();
        while (currY != goal.getY()) {
            Position newPos = new Position(
                    position.getX(),
                    currY,
                    position.getZ()
            );
            String index = newPos.getX() + ".0," + newPos.getY() + ".0," + newPos.getZ() + ".0";
            JsonObject blockInfo = info.getJsonObject(index);
            boolean shouldConvert = !blockInfo.getString("player").equals(owner) &&
                    getBlockValue(blockInfo.getString("type")) >= 0;
            path.add(new Pair<>(
                    newPos,
                    shouldConvert
            ));
            currY += direction;
        }
        direction = (int) Math.signum(goal.getX() - position.getX());
        int currX = position.getX();
        while (currX != goal.getX()) {
            Position newPos = new Position(
                    currX,
                    goal.getY(),
                    position.getZ()
            );
            String index = newPos.getX() + ".0," + newPos.getY() + ".0," + newPos.getZ() + ".0";
            JsonObject blockInfo = info.getJsonObject(index);
            boolean shouldConvert = !blockInfo.getString("player").equals(owner) &&
                    getBlockValue(blockInfo.getString("type")) >= 0;
            path.add(new Pair<>(
                    newPos,
                    shouldConvert
            ));
            currX += direction;
        }
        direction = (int) Math.signum(goal.getZ() - position.getZ());
        int currZ = position.getZ();
        while (currZ != goal.getZ()) {
            Position newPos = new Position(
                    goal.getX(),
                    goal.getY(),
                    currZ
            );
            String index = newPos.getX() + ".0," + newPos.getY() + ".0," + newPos.getZ() + ".0";
            JsonObject blockInfo = info.getJsonObject(index);
            boolean shouldConvert = !blockInfo.getString("player").equals(owner) &&
                    getBlockValue(blockInfo.getString("type")) >= 0;
            path.add(new Pair<>(
                    newPos,
                    shouldConvert
            ));
            currZ += direction;
        }
        path.poll();
    }

    private void scan5() {
        logger.info("probe {}: scan", id);
        int timer = commands.get("scan5");
        post("/command/" + owner + "/" + id + "/scan5", json -> {
            String reportId = json.getString("reportId");
            reportAndCallbackArgs(timer, reportId, "ProbeVerticle:scan5", json2 -> {
                JsonObject info = json2.getJsonObject("info");
                Position goal = getBestPosition(info);
                path.clear();
                makePath(info, goal);
                makeDecision();
            });
        });
    }
    //</editor-fold>
}
