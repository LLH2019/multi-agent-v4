package cloud.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import base.model.bean.BasicCommon;
import base.model.connect.bean.KafkaMsg;
import cloud.bean.*;
import cloud.global.GlobalAkkaPara;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ：LLH
 * @date ：Created in 2022/2/27 22:23
 * @description：制造资源Agent
 */
public class MdResourceActor extends AbstractBehavior<BasicCommon> {
    private static Logger logger = Logger.getLogger(DeviceCloudActor.class.getName());

    private final ActorRef<BasicCommon> ref;

    private final ActorRef<BasicCommon> brainRef;

    private final List<Integer> processTimes;

    private final List<Integer> processCosts;

    private final String name;

    private static int responseRounds = 0;

    private Map<String, int[]> exchangeMaps = new HashMap<>();

    private List<ProcessTime> haveAssignedTimes = new CopyOnWriteArrayList<>();

    public MdResourceActor(ActorContext<BasicCommon> context, String processTime,String processCost, String name, ActorRef<BasicCommon> brainRef) {
        super(context);
        logger.log(Level.INFO, "MdResourceActor pre init...");
        this.ref = context.getSelf();
        this.brainRef = brainRef;
        // 加工时间预处理
        String [] times = processTime.split(",");
        List<Integer> timeList = new ArrayList<>();
        for (String time : times) {
            timeList.add(Integer.parseInt(time));
        }
        this.processTimes = timeList;

        // 加工成本预处理
        String [] costs = processCost.split(",");
        List<Integer> costList = new ArrayList<>();
        for (String cost : costs) {
            costList.add(Integer.parseInt(cost));
        }
        this.processCosts = costList;

//        System.out.println(" processTime " + processTime);

        this.name = name;
        this.haveAssignedTimes = new CopyOnWriteArrayList<>();


        logger.log(Level.INFO, "MdResourceActor init...");
    }

    public static Behavior<BasicCommon> create(String processTime, String processCost,String name, ActorRef<BasicCommon> brainRef) {
        return Behaviors.setup(context -> new MdResourceActor(context, processTime, processCost, name, brainRef));
    }

    @Override
    public Receive<BasicCommon> createReceive() {
        return newReceiveBuilder()
                .onMessage(KafkaMsg.class, this::handleKafkaMsg)
                .onMessage(BidingMsg.class, this::handleBidingMsg)
                .onMessage(DealMsg.class, this::handleDealMsg)
                .onMessage(AskingExchange.class, this::handleAskingExchange)
                .onMessage(ResponseExchange.class, this::handleResponseExchange)
                .onMessage(DealExchange.class, this::handleDealExchange)
                .build();
    }

    private Behavior<BasicCommon> handleDealExchange(DealExchange msg) {
        ProcessTime time = new ProcessTime(msg.getStartTime(), msg.getEndTime());
        haveAssignedTimes.add(time);
//        System.out.print("dealing " + msg.getTaskName() + " " + msg.getResourceName());
        for (int i=0; i<haveAssignedTimes.size(); i++) {
            System.out.print("handle deal " + haveAssignedTimes.get(i).getStartTime() + " " + haveAssignedTimes.get(i).getEndTime());
        }
        System.out.println();

        Collections.sort(this.haveAssignedTimes,new Comparator<ProcessTime>() {
            public int compare(ProcessTime a, ProcessTime b) {
                return a.getStartTime() - b.getStartTime();
            }
        });

        return this;
    }

    private Behavior<BasicCommon> handleResponseExchange(ResponseExchange msg) {
        System.out.println("responseExchange..." + responseRounds);

        if (responseRounds == GlobalAkkaPara.resourceSize-2) {
            int minEndTime = Integer.MAX_VALUE;
            int minStartTime = 0;
            for (Map.Entry<String, int[]> entry : exchangeMaps.entrySet()) {
                if (entry.getValue()[1] < minEndTime) {
                    minEndTime = entry.getValue()[1];
                    minStartTime = entry.getValue()[0];
                }

            }
            if (minEndTime != Integer.MAX_VALUE) {
                DealExchange dealExchange = new DealExchange();
                dealExchange.setPreStartTime(msg.getPreStartTime());
                dealExchange.setPreEndTime(msg.getPreEndTime());
                dealExchange.setStartTime(minStartTime);
                dealExchange.setEndTime(minEndTime);

                for (int i=0; i<haveAssignedTimes.size(); i++) {
                    if (msg.getPreStartTime() == haveAssignedTimes.get(i).getStartTime() &&
                        msg.getPreEndTime() == haveAssignedTimes.get(i).getEndTime()) {
                        haveAssignedTimes.remove(i);
                        break;
                    }
                }
                GlobalAkkaPara.resourceActorRefMap.get(msg.getFromResourceName()).tell(dealExchange);

            }

            exchangeMaps.clear();
            responseRounds = 0;
            System.out.println("responseExchangelalala...");
            RepeatBiding repeatBiding = new RepeatBiding();
            brainRef.tell(repeatBiding);
        } else {
            responseRounds++;
            if (msg.getWaitProcessTimes().size() != 0) {
                ProcessTime time = msg.getWaitProcessTimes().get(0);
                exchangeMaps.put(msg.getFromResourceName(), new int[]{time.getStartTime(), time.getEndTime()});
            }
        }
        return this;
    }

    private Behavior<BasicCommon> handleAskingExchange(AskingExchange msg) {

        int spendingTime = processTimes.get(Integer.parseInt(msg.getTask()));

        ResponseExchange responseExchange = new ResponseExchange();
        List<ProcessTime> waitProcessTimes = new ArrayList<>();
        int preTime = msg.getPreTime();
        // start
        int minStartTime = Integer.MAX_VALUE;
        int minEndTime = Integer.MAX_VALUE;

        if (haveAssignedTimes.size() == 0) {
            minStartTime = preTime;
            minEndTime = preTime+spendingTime;

//            ProcessTime time = new ProcessTime(preTime, preTime+spendingTime);
//            waitProcessTimes.add(time);
        } else {
            int waitStartTime = haveAssignedTimes.get(0).getEndTime();
            boolean tag = true;
            for (int i = 1; i < haveAssignedTimes.size(); i++) {
                int waitEndTime = haveAssignedTimes.get(i).getStartTime();
                if (waitEndTime > preTime && preTime >= waitStartTime && waitEndTime - preTime >= spendingTime) {
                    minStartTime = preTime;
                    minEndTime = preTime + spendingTime;
//                    ProcessTime time = new ProcessTime(startTime, startTime+spendingTime);
//                    waitProcessTimes.add(time);
                    tag = false;
                    break;
                }
                waitStartTime = haveAssignedTimes.get(i).getEndTime();
            }
            if (tag) {
                waitStartTime = Math.max(waitStartTime, preTime);
                minStartTime = waitStartTime;
                minEndTime = waitStartTime + spendingTime;
//                ProcessTime time = new ProcessTime(waitStartTime, waitStartTime+spendingTime);
//                waitProcessTimes.add(time);
            }
        }
        if (minEndTime < msg.getEndTime()) {
            ProcessTime time = new ProcessTime(minStartTime, minEndTime);
            waitProcessTimes.add(time);
        }

        System.out.print("1111111 ");
        for (int i=0; i<haveAssignedTimes.size(); i++) {
            System.out.print("assign " + haveAssignedTimes.get(i).getStartTime() + " " + haveAssignedTimes.get(i).getEndTime());
        }
//        System.out.println("process " + waitProcessTimes.get(0).getStartTime() + " " +waitProcessTimes.get(0).getEndTime());

        responseExchange.setNo(msg.getNo());
//        responseExchange.setTaskName(msg.getSender());
        responseExchange.setPreStartTime(msg.getStartTime());
        responseExchange.setPreEndTime(msg.getEndTime());
        responseExchange.setWaitProcessTimes(waitProcessTimes);
        responseExchange.setFromResourceName(name);

        GlobalAkkaPara.resourceActorRefMap.get(msg.getResourceName()).tell(responseExchange);
        return this;
    }

    private Behavior<BasicCommon> handleDealMsg(DealMsg msg) {
        ProcessTime time = new ProcessTime(msg.getStartTime(), msg.getEndTime());
        haveAssignedTimes.add(time);
        System.out.print("dealing " + msg.getTaskName() + " " + msg.getResourceName());
        for (int i=0; i<haveAssignedTimes.size(); i++) {
            System.out.print("handle deal " + haveAssignedTimes.get(i).getStartTime() + " " + haveAssignedTimes.get(i).getEndTime());
        }
        System.out.println();

        Collections.sort(this.haveAssignedTimes,new Comparator<ProcessTime>() {
            public int compare(ProcessTime a, ProcessTime b) {
                return a.getStartTime() - b.getStartTime();
            }
        });

        AskingExchange askingExchange = new AskingExchange();
        askingExchange.setResourceName(msg.getResourceName());
        askingExchange.setTaskName(msg.getTaskName());
        askingExchange.setPreTime(msg.getPreTime());
        askingExchange.setStartTime(msg.getStartTime());
        askingExchange.setEndTime(msg.getEndTime());
        askingExchange.setTask(msg.getTask());
        askingExchange.setNo(msg.getNo());

        for(Map.Entry<String, ActorRef<BasicCommon>> entry : GlobalAkkaPara.resourceActorRefMap.entrySet()) {
            if (!entry.getKey().equals(this.name)){
                entry.getValue().tell(askingExchange);
            }
        }

        return this;
    }

    private Behavior<BasicCommon> handleBidingMsg(BidingMsg msg) {
        int spendingTime = processTimes.get(Integer.parseInt(msg.getTask()));
        ProposeMsg proposeMsg = new ProposeMsg();
        List<ProcessTime> waitProcessTimes = new ArrayList<>();

//        Collections.sort(waitProcessTimes,new Comparator<ProcessTime>() {
//            public int compare(ProcessTime a, ProcessTime b) {
//                return a.getStartTime() - b.getStartTime();
//            }
//        });


//        for (int i=0; i<haveAssignedTimes.size(); i++) {
//            System.out.println("in func " + msg.getTask() + " " + msg.getSender() + " "
//                   + msg.getNo() + " " + i + " " + msg.getStartTime() + " "+
//                    " " + haveAssignedTimes.get(i).getStartTime() + " " + haveAssignedTimes.get(i).getEndTime());
//        }

        int startTime = msg.getStartTime();


        // start

        if (haveAssignedTimes.size() == 0) {
            ProcessTime time = new ProcessTime(startTime, startTime+spendingTime);
//            System.out.println("777777777 " + startTime + " " + (startTime+spendingTime));

            waitProcessTimes.add(time);
        } else {

            int waitStartTime = haveAssignedTimes.get(0).getEndTime();

//            System.out.println("9999999");

            boolean tag = true;
            for (int i = 1; i < haveAssignedTimes.size(); i++) {
                int waitEndTime = haveAssignedTimes.get(i).getStartTime();
                if (waitEndTime > startTime && startTime >= waitStartTime && waitEndTime - startTime >= spendingTime) {
                    ProcessTime time = new ProcessTime(startTime, startTime+spendingTime);
                    waitProcessTimes.add(time);
                    tag = false;
                    break;
                }
                waitStartTime = haveAssignedTimes.get(i).getEndTime();
            }
            if (tag) {

                waitStartTime = Math.max(waitStartTime, startTime);
                ProcessTime time = new ProcessTime(waitStartTime, waitStartTime+spendingTime);

//                System.out.println("000000000 " + waitStartTime + " " + (waitStartTime+spendingTime));
                waitProcessTimes.add(time);
            }
        }

        // end


//        if (haveAssignedTimes.size() > 0) {
//            int firstTime = haveAssignedTimes.get(0).getStartTime();
//            if (firstTime > spendingTime) {
//                ProcessTime first = new ProcessTime(0, firstTime);
//                waitProcessTimes.add(first);
//            }
//            int waitStartTime = haveAssignedTimes.get(0).getEndTime();
//
//            for (int i=1; i<haveAssignedTimes.size(); i++) {
//                int waitEndTime = haveAssignedTimes.get(i).getStartTime();
//                if (waitEndTime-waitStartTime >= spendingTime) {
//                    ProcessTime curProcess = new ProcessTime(waitStartTime, waitEndTime);
//                    waitProcessTimes.add(curProcess);
//                }
//                waitStartTime = haveAssignedTimes.get(i).getEndTime();
//            }
//            ProcessTime end = new ProcessTime(waitStartTime, 9999);
//            waitProcessTimes.add(end);
//        }

        System.out.print("1111111 ");
        for (int i=0; i<haveAssignedTimes.size(); i++) {
            System.out.print("assign " + haveAssignedTimes.get(i).getStartTime() + " " + haveAssignedTimes.get(i).getEndTime());
        }
        System.out.println("process " + waitProcessTimes.get(0).getStartTime() + " " +waitProcessTimes.get(0).getEndTime());

        proposeMsg.setNo(msg.getNo());
        proposeMsg.setTaskName(msg.getSender());
        proposeMsg.setWaitProcessTimes(waitProcessTimes);
        proposeMsg.setResourceName(name);

//        System.out.println("mdResource biding msg!!!" + this.brainRef);
        this.brainRef.tell(proposeMsg);
        return this;
    }

    private Behavior<BasicCommon> handleKafkaMsg(KafkaMsg msg) {
        if (msg.getKey().equals("biding")) {

        }


        System.out.println("MdResource " + msg);
        return this;
    }
}

