package Verticles;

import Model.Position;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;

/**
 * Created by SegFault on 18/01/2017.
 */
public class TemplarVerticle extends AgentVerticle {
    public TemplarVerticle(String owner_, String id_, Position position_) {
        super(owner_, id_, position_);
        logger = LogManager.getLogger(this.getClass());
//        commands.put("moveup", new Date().getTime());
//        commands.put("movedown", new Date().getTime());
//        commands.put("movenorth", new Date().getTime());
//        commands.put("movesouth", new Date().getTime());
//        commands.put("movewest", new Date().getTime());
//        commands.put("moveeast", new Date().getTime());
//        commands.put("sphere", new Date().getTime());
//        commands.put("status", new Date().getTime());
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
    }
}
