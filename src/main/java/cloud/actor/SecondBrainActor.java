package cloud.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import base.model.bean.BasicCommon;
import cloud.bean.BidingMsg;
import cloud.bean.ProcessTime;
import cloud.bean.StartBiding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ：LLH
 * @date ：Created in 2022/3/15 22:12
 * @description：辅助brain
 */
public class SecondBrainActor extends AbstractBehavior<BasicCommon> {

    private static Logger logger = Logger.getLogger(SecondBrainActor.class.getName());

    private Map<String, ActorRef<BasicCommon>> mdResourceRefMaps = new HashMap<>();

    private final ActorRef<BasicCommon> ref;

    private List<ProcessTime> haveAssignedTimes;

    private List<String> tasks = new ArrayList<>();

    private Integer curRound;

    private String refName;

    public SecondBrainActor(ActorContext<BasicCommon> context, Map<String, ActorRef<BasicCommon>> mdResourceRefMaps,String processTime, String refName) {
        super(context);
        logger.log(Level.INFO, "SecondBrainActor pre init...");
        this.ref = context.getSelf();
        this.refName = refName;
        this.mdResourceRefMaps = mdResourceRefMaps;
        logger.log(Level.INFO, "SecondBrainActor init...");
        haveAssignedTimes = new ArrayList<>();
        startBiding(processTime);
    }


    public static Behavior<BasicCommon> create(Map<String, ActorRef<BasicCommon>> mdResourceRefMaps, String processTime,String refName) {
        return Behaviors.setup(context -> new SecondBrainActor(context, mdResourceRefMaps, processTime, refName));
    }

    @Override
    public Receive<BasicCommon> createReceive() {
        return newReceiveBuilder()
                .onMessage(StartBiding.class, this::handleBiding)
                .build();
    }

    private Behavior<BasicCommon> handleBiding(StartBiding bind) {

//        System.out.println("66666666666");
        BidingMsg bidingMsg = new BidingMsg();
        bidingMsg.setTask(tasks.get(bind.getRound()));
        bidingMsg.setNo(bind.getRound());
        bidingMsg.setStartTime(bind.getStartTime());
        bidingMsg.setSender(refName);
        for (Map.Entry<String, ActorRef<BasicCommon>> entry : mdResourceRefMaps.entrySet()) {
            entry.getValue().tell(bidingMsg);
        }
        return this;
    }

    private void startBiding(String task) {
//        String task = "3,6,5,2,1";
        String[] precessNumbers = task.split(",");

//        List<ProcessTime> processTimes = new ArrayList<>();
        for (int i=0; i<precessNumbers.length; i++) {

            tasks.add(precessNumbers[i]);
//            BidingMsg bidingMsg = new BidingMsg();
//            bidingMsg.setTask(precessNumbers[i]);
//            bidingMsg.setNo(i);
//            if (i == 0) {
//                bidingMsg.setStartTime(0);
//            } else {
//                bidingMsg.setStartTime(processTimes.get(i-1).getEndTime());
//            }
//
//            for (Map.Entry<String, ActorRef<BasicCommon>> entry : mdResourceRefMaps.entrySet()) {
//                entry.getValue().tell(bidingMsg);
//            }

//            try {
//                Thread.sleep(10000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

//            DealMsg dealMsg = new DealMsg();
//            dealMsg.setStartTime(processTimes.get(i).getStartTime());
//            dealMsg.setEndTime(processTimes.get(i).getEndTime());
//
//            mdResourceRefMaps.get(processTimes.get(i).getResourceName()).tell(dealMsg);
//
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }
//            System.out.println("OK  i am fine!!!");
        }
    }
}
