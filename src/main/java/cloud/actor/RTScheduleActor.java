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
 * @date ：Created in 2022/2/27 17:47
 * @description：实时调度Agent
 */
public class RTScheduleActor extends AbstractBehavior<BasicCommon> {
    private static Logger logger = Logger.getLogger(DeviceCloudActor.class.getName());

    private final ActorRef<BasicCommon> ref;

    public RTScheduleActor(ActorContext<BasicCommon> context) {
        super(context);
        logger.log(Level.INFO, "RTScheduleActor pre init...");
        this.ref = context.getSelf();

        logger.log(Level.INFO, "RTScheduleActor init...");
    }

    public static Behavior<BasicCommon> create() {
        return Behaviors.setup(context -> new RTScheduleActor(context));
    }

    @Override
    public Receive<BasicCommon> createReceive() {
        return newReceiveBuilder()
                .onMessage(KafkaMsg.class, this::handleKafkaMsg)
                .build();
    }

    private Behavior<BasicCommon> handleKafkaMsg(KafkaMsg msg) {
        System.out.println("reSchedule " + msg);
        return this;
    }
}
