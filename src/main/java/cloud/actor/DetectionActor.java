package cloud.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import base.model.bean.BasicCommon;
import base.model.connect.bean.KafkaMsg;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ：LLH
 * @date ：Created in 2022/2/27 19:41
 * @description：监测Agent
 */
public class DetectionActor extends AbstractBehavior<BasicCommon> {
    private static Logger logger = Logger.getLogger(DeviceCloudActor.class.getName());

    private final ActorRef<BasicCommon> ref;

    public DetectionActor(ActorContext<BasicCommon> context) {
        super(context);
        logger.log(Level.INFO, "DetectionActor pre init...");
        this.ref = context.getSelf();

        logger.log(Level.INFO, "DetectionActor init...");
    }

    public static Behavior<BasicCommon> create() {
        return Behaviors.setup(context -> new DetectionActor(context));
    }

    @Override
    public Receive<BasicCommon> createReceive() {
        return newReceiveBuilder()
                .onMessage(KafkaMsg.class, this::handleKafkaMsg)
                .build();
    }

    private Behavior<BasicCommon> handleKafkaMsg(KafkaMsg msg) {
        System.out.println("detection " + msg);
        return this;
    }
}
