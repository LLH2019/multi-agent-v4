package cloud.actor;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import base.model.bean.BasicCommon;
import base.model.bean.DeviceModel;
import cloud.bean.*;
import cloud.global.GlobalAkkaPara;
import cloud.util.ReadTxt;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author ：LLH
 * @date ：Created in 2021/4/19 11:01
 * @description：最高控制的actor
 */
public class BrainControlActor extends AbstractBehavior<BasicCommon> {

    private static Logger logger = Logger.getLogger(BrainControlActor.class.getName());
//    private final ActorRef<BasicCommon> kafkaConnectInActorRef;
    private final Map<String, ActorRef<BasicCommon>> cloudControlRefMaps = new HashMap<>();
    private final Map<String, ActorRef<BasicCommon>> mdResourceRefMaps = new HashMap<>();
    private final Map<String, ActorRef<BasicCommon>> mdTaskRefMaps = new HashMap<>();
    private final ActorSystem<?> system;
    private final Map<String, List<String>> taskMaps = new HashMap<>();

    private final ActorRef<BasicCommon> ref;

    private Map<String, List<ProcessTime>> processTimeMaps = new HashMap<>();

    private final  Map<String, ActorRef<BasicCommon>> secondBrainRefMaps = new HashMap<>();

    private Map<String, Integer> haveSendResourceMaps = new HashMap<>();

    private int resourceNum;

    private Map<String, Integer> taskProcessNum = new HashMap<>();

    private static int roundTime = 0;
    private static int curTaskNo = 0;

    public BrainControlActor(ActorContext<BasicCommon> context) {
        super(context);
        this.system = GlobalAkkaPara.system;
//        this.kafkaConnectInActorRef = GlobalAkkaPara.globalActorRefMap.get(GlobalActorRefName.CLOUD_KAFKA_CONNECT_IN_ACTOR);
//        init();
        this.ref = context.getSelf();
        ReadTxt readTxt =  new ReadTxt();
        List<List<String>> taskAndResources = readTxt.readTxtData(GlobalAkkaPara.taskNum, GlobalAkkaPara.rapid);

        setupResourceActor(taskAndResources.get(1), taskAndResources.get(2));
        setupSecondBrainActor(taskAndResources.get(0));

//        startBiding();
    }



//    private void startBiding() {
//        String task = "3,6,5,2,1";
//        String[] precessNumbers = task.split(",");
////        List<ProcessTime> processTimes = new ArrayList<>();
//        for (int i=0; i<precessNumbers.length; i++) {
//            BidingMsg bidingMsg = new BidingMsg();
//            bidingMsg.setTask(precessNumbers[i]);
//            bidingMsg.setNo(i);
//            if (processTimes.size() == 0) {
//                bidingMsg.setStartTime(0);
//            } else {
//                bidingMsg.setStartTime(processTimes.get(i-1).getEndTime());
//            }
//
//            for (Map.Entry<String, ActorRef<BasicCommon>> entry : mdResourceRefMaps.entrySet()) {
//                entry.getValue().tell(bidingMsg);
//            }
//
////            DealMsg dealMsg = new DealMsg();
////            dealMsg.setStartTime(processTimes.get(i).getStartTime());
////            dealMsg.setEndTime(processTimes.get(i).getEndTime());
////
////            mdResourceRefMaps.get(processTimes.get(i).getResourceName()).tell(dealMsg);
////
////            try {
////                Thread.sleep(2000);
////            } catch (InterruptedException e) {
////                e.printStackTrace();
////            }
////            System.out.println("OK  i am fine!!!");
//        }
//    }

    private void setupSecondBrainActor(List<String> tasks) {
        List<String> processTasks = tasks;
//                = new ArrayList<>();
//        processTasks.add("3,6,5,2,1");
//        processTasks.add("3,6,2,5,1");
//        processTasks.add("0,1,2");
//        processTasks.add("3,4,5,6");
//        processTasks.add("7,8,9");
//        processTasks.add("10,11,12");
//        processTasks.add("13,14,15,16");
//        processTasks.add("17,18,19");
//        processTasks.add("20,21,22");
//        processTasks.add("23,24,25,26");

        List<Integer> processNums = new ArrayList<>();
        for (String task : processTasks) {
            processNums.add(task.split(",").length);
        }

        for (int i=0; i<processTasks.size(); i++) {
            String refName = "secondBrain"+i;
            haveSendResourceMaps.put(refName,0);
            List<ProcessTime> times = new ArrayList<>();
            processTimeMaps.put(refName, times);
            ActorRef<BasicCommon> secondBrainRef = getContext().spawn(SecondBrainActor.create(mdResourceRefMaps, processTasks.get(i),refName), refName);
            secondBrainRefMaps.put(refName, secondBrainRef);

            taskProcessNum.put(refName, processNums.get(i));

            taskMaps.put(refName, new ArrayList<>());
            String[] strs = processTasks.get(i).split(",");
            for (int j=0; j<strs.length; j++) {
                taskMaps.get(refName).add(strs[j]);
            }
//            StartBiding startBiding = new StartBiding();
//            startBiding.setRound(0);
//            startBiding.setStartTime(0);
//            secondBrainRef.tell(startBiding);
        }
        StartBiding startBiding = new StartBiding();
        startBiding.setRound(0);
        startBiding.setStartTime(0);
        secondBrainRefMaps.get("secondBrain0").tell(startBiding);
    }

    private void setupResourceActor(List<String> resources, List<String> cost) {
        List<String> processTimes = resources;

        List<String> processCost = cost;

//                new ArrayList<>();
//        processTimes.add("3,5,7,9,999,45,67,4");
//        processTimes.add("5,7,4,6,3,7,3,999");
//        processTimes.add("999,23,45,4,5,6,2,5");
//        processTimes.add("4,999,5,6,3,23,5,7");
//        processTimes.add("66,45,34,2,5,6,7,9");

//        processTimes.add("5,10,9999,5,9999,9999,10,10,9999,1,3,12,4,3,10,9999,11,6,11,10,5,9999,9999,2,7,9,9");
//        processTimes.add("3,9999,10,7,8,10,8,9999,10,4,1,11,6,6,9999,9,9,7,9999,5,4,9,8,8,4,9,9999");
//        processTimes.add("5,5,9999,3,5,9999,9,9999,6,5,6,7,2,7,7,8,9999,1,9,9,2,9999,9,5,7,9999,3");
//        processTimes.add("3,8,5,9,2,5,6,7,4,6,5,8,10,8,4,7,6,4,9,10,6,9,3,9,8,8,7");
//        processTimes.add("3,3,6,8,6,6,4,6,8,9999,9,10,3,9,9,4,7,6,9,11,7,11,8,9999,9,5,1");
//        processTimes.add("9999,9,2,9999,7,4,7,5,9,10,7,5,9,9999,8,2,5,9,7,9999,9999,9,6,4,9999,6,5");
//        processTimes.add("10,9,4,9,10,1,9999,2,10,9999,8,6,5,10,6,7,3,9999,8,10,10,10,9999,9999,10,7,8");
//        processTimes.add("9,6,5,9999,9,7,9999,4,9999,7,4,9,7,9999,9999,9999,6,10,4,9999,9999,5,10,10,9999,1,9999");

        this.resourceNum = processTimes.size();

        for (int i=0; i<processTimes.size(); i++) {
            String refName = "Resource" + i;
            ActorRef<BasicCommon> mdResourceRef = getContext().spawn(MdResourceActor.create(processTimes.get(i), processCost.get(i), refName, this.ref), refName);
            mdResourceRefMaps.put(refName, mdResourceRef);
            GlobalAkkaPara.resourceActorRefMap.put(refName, mdResourceRef);
        }
    }

    private void init() {
        ActorRef<BasicCommon> rtScheduleRef = getContext().spawn(RTScheduleActor.create(), "rtSchedule");
        cloudControlRefMaps.put("rtSchedule", rtScheduleRef);

        ActorRef<BasicCommon> detectionRef = getContext().spawn(RTScheduleActor.create(), "detection");
        cloudControlRefMaps.put("detection", detectionRef);

        ActorRef<BasicCommon> mdTaskPoolRef = getContext().spawn(RTScheduleActor.create(), "mdTaskPool");
        cloudControlRefMaps.put("mdTaskPool", mdTaskPoolRef);
    }

    @Override
    public Receive<BasicCommon> createReceive() {
        return newReceiveBuilder()
                .onMessage(DeviceModel.class, this::onHandleDeviceLink)
                .onMessage(ProposeMsg.class, this::handleProcessMsg)
                .onMessage(RepeatBiding.class, this::handleRepeatBiding)
//                .onMessage(KafkaMsg.class, this::handleKafkaMsg)
                .build();
    }

    private Behavior<BasicCommon> handleRepeatBiding(RepeatBiding msg) {
        System.out.println("repeatingBiding...");

        String taskName = "secondBrain" + curTaskNo;
        System.out.println("taskName " + taskName);
        List<ProcessTime> processTimes = processTimeMaps.get(taskName);
        if(curTaskNo<GlobalAkkaPara.taskNum*GlobalAkkaPara.taskSize) {
            if (roundTime == 0) {
                StartBiding startBiding = new StartBiding();
                startBiding.setRound(0);
                startBiding.setStartTime(0);
                secondBrainRefMaps.get(taskName).tell(startBiding);
            } else {
                StartBiding startBiding = new StartBiding();
                startBiding.setRound(roundTime);
                System.out.println("roundTime " + roundTime);
                System.out.println("processTimes " + processTimes.size());
                startBiding.setStartTime(processTimes.get(roundTime - 1).getEndTime());
//                System.out.println("888888888 "+ round + " " + processTimes.get(round).getEndTime() + " " + msg.getTaskName());
                secondBrainRefMaps.get(taskName).tell(startBiding);
            }
        }
        return this;
    }

    private Behavior<BasicCommon> handleProcessMsg(ProposeMsg msg) {

//        System.out.println("proposeMsg "  + msg.getTaskName() + " " + msg.getResourceName() + " " + msg.getNo() + " " +msg.getWaitProcessTimes().get(0).getStartTime() +
//                " " + msg.getWaitProcessTimes().get(0).getEndTime() + " " );


//        System.out.println("brain processMsg!!!");
        ProcessTime processTime = msg.getWaitProcessTimes().get(0);
        System.out.println("taskNama11 " + msg.getTaskName());
        List<ProcessTime> processTimes = processTimeMaps.get(msg.getTaskName());
        if (processTimes.size()-1 <=msg.getNo()) {
           ProcessTime time = new ProcessTime(processTime.getStartTime(), processTime.getEndTime());
           time.setResourceName(msg.getResourceName());
           processTimes.add(time);
        } else {
            ProcessTime curTime = processTimes.get(msg.getNo());
            if (curTime.getEndTime() > processTime.getEndTime()) {
                curTime.setStartTime(processTime.getStartTime());
                curTime.setEndTime(processTime.getEndTime());
                curTime.setResourceName(msg.getResourceName());
            }
        }
        processTimeMaps.put(msg.getTaskName(), processTimes);
        int haveSendResource = haveSendResourceMaps.get(msg.getTaskName());
        haveSendResource++;
        haveSendResourceMaps.put(msg.getTaskName(), haveSendResource);
        int round = haveSendResource/resourceNum;
        roundTime  =round;
        if (haveSendResource%resourceNum==0 ) {
            DealMsg dealMsg = new DealMsg();
            if (round == 1) {
                dealMsg.setPreTime(0);
            } else {
                dealMsg.setPreTime(processTimes.get(round-2).getEndTime());
            }
            dealMsg.setTaskName(msg.getTaskName());
            System.out.println("--- " + processTimes.size());

            dealMsg.setResourceName(processTimes.get(round-1).getResourceName());
            dealMsg.setStartTime(processTimes.get(round-1).getStartTime());
            dealMsg.setEndTime(processTimes.get(round-1).getEndTime());
            dealMsg.setTask(taskMaps.get(msg.getTaskName()).get(round-1));
            dealMsg.setNo(round-1);
            mdResourceRefMaps.get(processTimes.get(round-1).getResourceName()).tell(dealMsg);
            if (round<taskProcessNum.get(msg.getTaskName())) {


//
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//                System.out.println("22222222222");
//                StartBiding startBiding = new StartBiding();
//                startBiding.setRound(round);
//                startBiding.setStartTime(processTimes.get(round-1).getEndTime());
////                System.out.println("888888888 "+ round + " " + processTimes.get(round).getEndTime() + " " + msg.getTaskName());
//                secondBrainRefMaps.get(msg.getTaskName()).tell(startBiding);
            } else {
                roundTime= 0;
                curTaskNo++;
//                StartBiding startBiding = new StartBiding();
//                startBiding.setRound(0);
//                startBiding.setStartTime(0);
//                int curResourceNum = Integer.parseInt(msg.getTaskName().substring(11)) +1;
//                if (curResourceNum<secondBrainRefMaps.size()) {
//                    secondBrainRefMaps.get("secondBrain"+curResourceNum).tell(startBiding);
//                }
            }

            try {
                File writeName = new File(GlobalAkkaPara.dataPath +"output"+ GlobalAkkaPara.resourceSize + "-" + (GlobalAkkaPara.taskSize*GlobalAkkaPara.taskNum) +"-" +GlobalAkkaPara. rapid +".txt"); // 相对路径，如果没有则要建立一个新的output.txt文件
                if (!writeName.exists()) {
                    writeName.createNewFile(); // 创建新文件,有同名的文件的话直接覆盖
                }
                FileOutputStream fos = new FileOutputStream(writeName,true);
                OutputStreamWriter osw = new OutputStreamWriter(fos);
                BufferedWriter out = new BufferedWriter(osw);

                if (round == taskProcessNum.get(msg.getTaskName())) {
                    for (int i = 0; i < taskProcessNum.get(msg.getTaskName()); i++) {
                        String str = "round " + i + " " + msg.getTaskName() + " " + processTimes.get(i).getStartTime() + " "
                                + processTimes.get(i).getEndTime() + " " + processTimes.get(i).getResourceName();
                        System.out.println(str);
                        out.write(str);
                        out.newLine();
                    }
                }
                out.flush(); // 把缓存区内容压入文件
            } catch (IOException e) {
                e.printStackTrace();
            }

//            System.out.println("i  am fine !!!!");
        }


//        System.out.println("brain processMsg!!!");

        return this;
    }

//    private Behavior<BasicCommon> handleKafkaMsg(KafkaMsg msg) {
//        System.out.println("444444444" + msg);
//        System.out.println("topic is " + msg.getTopic());
//        if ("cloud.rt".equals(msg.getTopic())) {
//            cloudControlRefMaps.get("rtSchedule").tell(msg);
//        } else if ("cloud.de".equals(msg.getTopic())) {
//            cloudControlRefMaps.get("detection").tell(msg);
//        } else if ("cloud.po".equals(msg.getTopic())) {
//            cloudControlRefMaps.get("mdTaskPool").tell(msg);
//        } else if ("cloud.init.task".equals(msg.getTopic())) {
////            ActorRef<BasicCommon>  ref = getContext().spawn();
////            cloudControlRefMaps.put(realName, ref);
//        } else if ("cloud.dis.task".equals(msg.getTopic())) {
////            ActorRef<BasicCommon>  ref = getContext().spawn();
////            cloudControlRefMaps.put(realName, ref);
//        } else if ("cloud.init.resource".equals(msg.getTopic())) {
//            System.out.println("6666 " +msg);
//            String refName = msg.getKey()+msg.getValue();
//            ActorRef<BasicCommon>  ref = getContext().spawn(MdResourceActor.create(), msg.getKey()+msg.getValue());
//            mdResourceRefMaps.put(refName, ref);
//        } else if ("cloud.dis.resource".equals(msg.getTopic())) {
//            System.out.println("7777 " + msg);
//            String refName = msg.getKey()+msg.getValue();
//            mdResourceRefMaps.remove(refName);
//        } else if ("cloud.re".equals(msg.getTopic())) {
//            System.out.println("8888 " + msg);
//        }
//
//        return this;
//    }

    private Behavior<BasicCommon> onHandleDeviceLink(DeviceModel model) {
        logger.log(Level.INFO, "BrainControlActor handle device link..." + model);

        String realName = model.getModel().getName() + "-" +  model.getModel().getNo();
        logger.log(Level.INFO, "BrainControlActor spawn device..." + realName + model);
        ActorRef<BasicCommon>  ref = getContext().spawn(DeviceCloudActor.create(model), realName);
        cloudControlRefMaps.put(realName, ref);
        return this;
    }

    public static Behavior<BasicCommon> create() {
        return Behaviors.setup(context -> new BrainControlActor(context));
    }

}
