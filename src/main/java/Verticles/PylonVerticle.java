package Verticles;

import Model.Position;
import io.vertx.core.Future;
import org.apache.logging.log4j.LogManager;

/**
 * Created by SegFault on 18/01/2017.
 */
public class PylonVerticle extends AgentVerticle {
    public PylonVerticle(String owner_, String id_, Position position_) {
        super(owner_, id_, position_);
        logger = LogManager.getLogger(this.getClass());
//        commands.put("transfer", new Date().getTime());
//        commands.put("status", new Date().getTime());
    }

    @Override
    public void start(Future<Void> startFuture) throws Exception {
        super.start(startFuture);
    }
}
