package Verticles;

import Model.Position;
import io.vertx.core.Future;
import io.vertx.core.json.JsonObject;
import org.apache.logging.log4j.LogManager;

/**
 * Created by SegFault on 18/01/2017.
 */
public class ProbeVerticle extends AgentVerticle {
    public ProbeVerticle(String owner_, String id_, Position position_) {
        super(owner_, id_, position_);
        logger = LogManager.getLogger(this.getClass());
        commands.put("moveup", DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME + OFFSET);
        commands.put("movedown", DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME + OFFSET);
        commands.put("movenorth", DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME + OFFSET);
        commands.put("movesouth", DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME + OFFSET);
        commands.put("movewest", DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME + OFFSET);
        commands.put("moveeast", DEFAULT_MOVE_PRETIME + DEFAULT_MOVE_POSTTIME + OFFSET);
        commands.put("scan3", DEFAULT_SCAN3_PRETIME + DEFAULT_SCAN3_POSTTIME + OFFSET);
        commands.put("scan5", DEFAULT_SCAN5_PRETIME + DEFAULT_SCAN5_POSTTIME + OFFSET);
        commands.put("convert", DEFAULT_CONVERT_PRETIME + DEFAULT_CONVERT_POSTTIME + OFFSET);
        commands.put("mine", DEFAULT_MINE_PRETIME + DEFAULT_MINE_POSTTIME + OFFSET);
        commands.put("status", DEFAULT_STATUS_PRETIME + DEFAULT_STATUS_POSTTIME + OFFSET);
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);

        logger.info("Probe: {}, {}, Id{{}}", owner, position, id);
        makeDecision();
    }

    @Override
    protected void makeDecision() {
        mine();
    }

    private void mine() {
        logger.info("mine: starting...");
        int timer = commands.get("scan5");
        post("/command/" + owner + "/" + id + "/scan5", json -> {
            String reportId = json.getString("reportId");
            vertx.setTimer(timer, __ -> get("/report/" + reportId, json2 -> {
                logger.info("scan report: {}", json2);
                makeDecision();
            }));
        });
    }
}
