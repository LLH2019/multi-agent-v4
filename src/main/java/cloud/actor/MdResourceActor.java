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

    private final List<int[]> processInfos = new ArrayList<>();

    private final List<Integer> processCosts;

    private final String name;

    private int responseRounds = 0;

    private int firstResponseRounds = 0;
    private int secondResponseRounds = 0;

    private Map<String, int[]> exchangeMaps = new HashMap<>();
    private Map<String, FrontResponseTwoTime> secondExchangeMaps = new HashMap<>();
    private Map<String, FrontResponseOneTime> firstExchangeMaps = new HashMap<>();

    private List<ProcessInfo> haveAssignedProcessInfos = new CopyOnWriteArrayList<>();

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

        this.name = name;
        this.haveAssignedProcessInfos = new CopyOnWriteArrayList<>();


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
                .onMessage(FrontAskOneTime.class, this::handleFrontAskOneTime)
                .onMessage(FrontResponseOneTime.class, this::handleResponseOneTime)
                .onMessage(FrontAskTwoTime.class, this::handleFrontAskTwoTime)
                .onMessage(FrontResponseTwoTime.class, this::handleFrontResponseTwoTime)
                .onMessage(FrontDealOneTime.class, this::handleFrontDealOneTime)
                .onMessage(FrontDealTwoTime.class, this::handleFrontDealTwoTime)
                .onMessage(AskFinalResourceInfo.class, this::handleAskFinalResourceInfo)
                .build();
    }

    private Behavior<BasicCommon> handleAskFinalResourceInfo(AskFinalResourceInfo msg) {
        ResponseFinalResourceInfo responseFinalResourceInfo = new ResponseFinalResourceInfo();
        responseFinalResourceInfo.setResourceName(name);
        responseFinalResourceInfo.setHaveAssignedProcessInfos(new ArrayList<>(haveAssignedProcessInfos));
        brainRef.tell(responseFinalResourceInfo);
        return this;
    }

    private Behavior<BasicCommon> handleFrontDealTwoTime(FrontDealTwoTime msg) {
        haveAssignedProcessInfos.add(msg.getProcessInfos().get(3));
        Collections.sort(haveAssignedProcessInfos, new Comparator<ProcessInfo>() {
            @Override
            public int compare(ProcessInfo o1, ProcessInfo o2) {
                return o1.getEndTime()-o2.getEndTime();
            }
        });
//
//        for(ProcessInfo processInfo : haveAssignedProcessInfos) {
//            System.out.println("RRR " + name +" " + processInfo.getStartTime() + " " + processInfo.getEndTime());
//        }

        RepeatBiding repeatBiding = new RepeatBiding();
        repeatBiding.setResourceName(name);
        repeatBiding.setProcessInfos(haveAssignedProcessInfos);
        brainRef.tell(repeatBiding);

        return this;
    }

    private Behavior<BasicCommon> handleFrontDealOneTime(FrontDealOneTime msg) {
//        System.out.println("handleFrontDealOneTime " + msg.getProcessInfos().size());

        for (int i=0; i<haveAssignedProcessInfos.size(); i++) {
            if (msg.getProcessInfos().get(1).getEndTime().equals(haveAssignedProcessInfos.get(i).getEndTime())) {
                haveAssignedProcessInfos.remove(i);
                break;
            }
        }
//        haveAssignedProcessInfos.remove(msg.getProcessInfos().get(1));
        haveAssignedProcessInfos.add(msg.getProcessInfos().get(2));
        Collections.sort(haveAssignedProcessInfos, new Comparator<ProcessInfo>() {
            @Override
            public int compare(ProcessInfo o1, ProcessInfo o2) {
                return o1.getEndTime()-o2.getEndTime();
            }
        });
        FrontDealTwoTime frontDealTwoTime = new FrontDealTwoTime();
        List<ProcessInfo> twoProcessInfos = new ArrayList<>(msg.getProcessInfos());
        frontDealTwoTime.setProcessInfos(twoProcessInfos);
        GlobalAkkaPara.resourceActorRefMap.get(msg.getProcessInfos().get(3).getResourceName()).tell(frontDealTwoTime);
        return this;
    }

    private Behavior<BasicCommon> handleFrontResponseTwoTime(FrontResponseTwoTime msg) {

        if (secondResponseRounds == GlobalAkkaPara.resourceSize-2) {
            if (msg.getIsSuccess()) {
                secondExchangeMaps.put(msg.getTwoResource(), msg);
            }
            int minEndTime = Integer.MAX_VALUE;
            String twoResourceName= "";
            for (Map.Entry<String, FrontResponseTwoTime> entry : secondExchangeMaps.entrySet()) {
                int endTime = Math.max(entry.getValue().getProcessInfos().get(2).getEndTime(), entry.getValue().getProcessInfos().get(3).getEndTime());
                if (endTime < minEndTime) {
                    minEndTime = endTime;
                    twoResourceName = entry.getKey();
//                    processInfos = entry.getValue();
                }
            }
            if (minEndTime != Integer.MAX_VALUE) {
                FrontResponseOneTime frontResponseOneTime = new FrontResponseOneTime();
                FrontResponseTwoTime selectedResponseTwoTime = secondExchangeMaps.get(twoResourceName);

                frontResponseOneTime.setIsSuccess(true);
                frontResponseOneTime.setTwoResourceName(twoResourceName);
                frontResponseOneTime.setProcessInfos(selectedResponseTwoTime.getProcessInfos());
//                frontResponseOneTime.setFromResourceName(name);
                frontResponseOneTime.setZeroResource(selectedResponseTwoTime.getZeroResource());
                frontResponseOneTime.setOneResource(selectedResponseTwoTime.getOneResource());
                frontResponseOneTime.setTwoResource(selectedResponseTwoTime.getTwoResource());
                GlobalAkkaPara.resourceActorRefMap.get(selectedResponseTwoTime.getZeroResource()).tell(frontResponseOneTime);

            } else {
                FrontResponseOneTime frontResponseOneTime = new FrontResponseOneTime();
                frontResponseOneTime.setIsSuccess(false);
//                frontResponseOneTime.setFromResourceName(name);
                frontResponseOneTime.setZeroResource(msg.getZeroResource());
                frontResponseOneTime.setOneResource(msg.getOneResource());
                frontResponseOneTime.setTwoResource(msg.getTwoResource());
                frontResponseOneTime.setProcessInfos(msg.getProcessInfos());
                GlobalAkkaPara.resourceActorRefMap.get(msg.getZeroResource()).tell(frontResponseOneTime);
            }
            secondExchangeMaps.clear();
            secondResponseRounds = 0;
        } else {
            secondResponseRounds++;
            if (msg.getIsSuccess()) {
                secondExchangeMaps.put(msg.getTwoResource(), msg);
            }
        }

        return this;
    }

    private Behavior<BasicCommon> handleResponseOneTime(FrontResponseOneTime msg) {

        if (firstResponseRounds == GlobalAkkaPara.resourceSize-2) {
            if (msg.getIsSuccess()) {
                firstExchangeMaps.put(msg.getFromResourceName(), msg);
            }

            if (firstExchangeMaps.size() == 0) {
//                System.out.println("handleResponseOneTime " + msg.getProcessInfos() + msg);
                haveAssignedProcessInfos.add(msg.getProcessInfos().get(0));
                RepeatBiding repeatBiding = new RepeatBiding();
                repeatBiding.setResourceName(name);
                repeatBiding.setProcessInfos(haveAssignedProcessInfos);
                brainRef.tell(repeatBiding);
            } else {
                int minEndTime = Integer.MAX_VALUE;
                String resourceName = "";
                for (Map.Entry<String, FrontResponseOneTime> entry : firstExchangeMaps.entrySet()) {
                    int endTime = Math.min(entry.getValue().getProcessInfos().get(2).getEndTime(), entry.getValue().getProcessInfos().get(3).getEndTime());
                    if (endTime < minEndTime) {
                        minEndTime = endTime;
                        resourceName = entry.getKey();
                    }
                }
                if (minEndTime != Integer.MAX_VALUE) {
                    FrontDealOneTime frontDealOneTime = new FrontDealOneTime();
//                    System.out.println("handleResponseOneTime " + msg.getProcessInfos().size());

                    FrontResponseOneTime selectedFrontResponseOneTime = firstExchangeMaps.get(resourceName);
                    List<ProcessInfo> oneProcessInfos = new ArrayList<>(selectedFrontResponseOneTime.getProcessInfos());
                    frontDealOneTime.setProcessInfos(oneProcessInfos);
                    frontDealOneTime.setZeroResource(selectedFrontResponseOneTime.getZeroResource());
                    frontDealOneTime.setOneResource(selectedFrontResponseOneTime.getOneResource());
                    frontDealOneTime.setTwoResource(selectedFrontResponseOneTime.getTwoResource());
                    GlobalAkkaPara.resourceActorRefMap.get(selectedFrontResponseOneTime.getOneResource()).tell(frontDealOneTime);

                } else {
                    System.out.println("0000000000");
                }
            }

            firstExchangeMaps.clear();
            firstResponseRounds = 0;
        } else {
            firstResponseRounds++;
            if (msg.getIsSuccess()) {
                firstExchangeMaps.put(msg.getFromResourceName(), msg);
            }
        }

        return this;
    }

    private Behavior<BasicCommon> handleFrontAskTwoTime(FrontAskTwoTime msg) {
        ProcessInfo curLastProcess = msg.getProcessInfos().get(msg.getProcessInfos().size()-1);
        ProcessInfo curFrontProcess = msg.getProcessInfos().get(0);
        int spendingTime = processTimes.get(Integer.parseInt(curLastProcess.getTaskProcessNum()));
        int preTime = curLastProcess.getPreTime();
        int minStartTime = Integer.MAX_VALUE;
        int minEndTime = Integer.MAX_VALUE;

        if (haveAssignedProcessInfos.size() == 0) {
            minStartTime = preTime;
            minEndTime = preTime+spendingTime;
        } else {
            int waitStartTime = haveAssignedProcessInfos.get(0).getEndTime();
            boolean tag = true;
            for (int i = 1; i < haveAssignedProcessInfos.size(); i++) {
                int waitEndTime = haveAssignedProcessInfos.get(i).getStartTime();
                if (waitEndTime > preTime && preTime >= waitStartTime && waitEndTime - preTime >= spendingTime) {
                    minStartTime = preTime;
                    minEndTime = preTime + spendingTime;
                    tag = false;
                    break;
                }
                waitStartTime = haveAssignedProcessInfos.get(i).getEndTime();
            }
            if (tag) {
                waitStartTime = Math.max(waitStartTime, preTime);
                minStartTime = waitStartTime;
                minEndTime = waitStartTime + spendingTime;
            }
        }
        if (minEndTime < curFrontProcess.getEndTime()) {
            ProcessInfo finishCurLastProcess = new ProcessInfo(minStartTime, minEndTime);
            finishCurLastProcess.setPreTime(curLastProcess.getPreTime());
            finishCurLastProcess.setTaskName(curLastProcess.getTaskName());
            finishCurLastProcess.setTaskProcessNum(curFrontProcess.getTaskProcessNum());
            finishCurLastProcess.setNo(curLastProcess.getNo());
            finishCurLastProcess.setResourceName(name);
            finishCurLastProcess.setStartTime(minStartTime);
            finishCurLastProcess.setEndTime(minEndTime);

            // 可以反馈接收
            FrontResponseTwoTime frontResponseTwoTime = new FrontResponseTwoTime();
            frontResponseTwoTime.setIsSuccess(true);
//            frontResponseTwoTime.setFromResourceName(name);
            frontResponseTwoTime.setZeroResource(msg.getZeroResource());
            frontResponseTwoTime.setOneResource(msg.getOneResource());
            frontResponseTwoTime.setTwoResource(name);

            List<ProcessInfo> processInfos = new ArrayList<>(msg.getProcessInfos());
            processInfos.add(finishCurLastProcess);
            frontResponseTwoTime.setProcessInfos(processInfos);
            GlobalAkkaPara.resourceActorRefMap.get(msg.getOneResource()).tell(frontResponseTwoTime);
        } else {

            FrontResponseTwoTime frontResponseTwoTime = new FrontResponseTwoTime();
            frontResponseTwoTime.setIsSuccess(false);
            frontResponseTwoTime.setProcessInfos(msg.getProcessInfos());
            frontResponseTwoTime.setZeroResource(msg.getZeroResource());
            frontResponseTwoTime.setOneResource(msg.getOneResource());
            frontResponseTwoTime.setTwoResource(name);
            List<ProcessInfo> processInfos = new ArrayList<>(msg.getProcessInfos());
            frontResponseTwoTime.setProcessInfos(processInfos);
            GlobalAkkaPara.resourceActorRefMap.get(msg.getOneResource()).tell(frontResponseTwoTime);
            // 直接回复
        }
        return this;
    }

    private Behavior<BasicCommon> handleFrontAskOneTime(FrontAskOneTime msg) {
//        System.out.println("handleFrontAskOneTime " + msg.getProcessInfos());
        if (haveAssignedProcessInfos.size() == 0) {
            FrontResponseOneTime frontResponseOneTime = new FrontResponseOneTime();
            frontResponseOneTime.setIsSuccess(false);
            frontResponseOneTime.setZeroResource(msg.getZeroResource());
            frontResponseOneTime.setOneResource(name);
            List<ProcessInfo> lists = new ArrayList<>(msg.getProcessInfos());
            frontResponseOneTime.setProcessInfos(lists);
            GlobalAkkaPara.resourceActorRefMap.get(msg.getZeroResource()).tell(frontResponseOneTime);
        } else {
            ProcessInfo curLastProcess = haveAssignedProcessInfos.get(haveAssignedProcessInfos.size() - 1);
            haveAssignedProcessInfos.remove(curLastProcess);
            ProcessInfo frontProcess = msg.getProcessInfos().get(0);
            int spendingTime = processTimes.get(Integer.parseInt(frontProcess.getTaskProcessNum()));

            int preTime = frontProcess.getPreTime();
            int minStartTime = Integer.MAX_VALUE;
            int minEndTime = Integer.MAX_VALUE;

            if (haveAssignedProcessInfos.size() == 0) {
                minStartTime = preTime;
                minEndTime = preTime + spendingTime;

            } else {
                int waitStartTime = haveAssignedProcessInfos.get(0).getEndTime();
                boolean tag = true;
                for (int i = 1; i < haveAssignedProcessInfos.size(); i++) {
                    int waitEndTime = haveAssignedProcessInfos.get(i).getStartTime();
                    if (waitEndTime > preTime && preTime >= waitStartTime && waitEndTime - preTime >= spendingTime) {
                        minStartTime = preTime;
                        minEndTime = preTime + spendingTime;

                        tag = false;
                        break;
                    }
                    waitStartTime = haveAssignedProcessInfos.get(i).getEndTime();
                }
                if (tag) {
                    waitStartTime = Math.max(waitStartTime, preTime);
                    minStartTime = waitStartTime;
                    minEndTime = waitStartTime + spendingTime;
                }
            }
            if (minEndTime < frontProcess.getEndTime()) {
                ProcessInfo curFrontProcess = new ProcessInfo(minStartTime, minEndTime);
                curFrontProcess.setPreTime(frontProcess.getPreTime());
                curFrontProcess.setTaskName(frontProcess.getTaskName());
                curFrontProcess.setNo(frontProcess.getNo());
                curFrontProcess.setTaskProcessNum(frontProcess.getTaskProcessNum());
                curFrontProcess.setResourceName(name);
                curFrontProcess.setStartTime(minStartTime);
                curFrontProcess.setEndTime(minEndTime);

                // 询问别人
                FrontAskTwoTime frontAskTwoTime = new FrontAskTwoTime();
                frontAskTwoTime.setFromResourceName(name);

                List<ProcessInfo> processInfos = new ArrayList<>();
                processInfos.add(msg.getProcessInfos().get(0));
                processInfos.add(curFrontProcess);
                processInfos.add(curLastProcess);
                frontAskTwoTime.setProcessInfos(processInfos);
                frontAskTwoTime.setZeroResource(msg.getZeroResource());
                frontAskTwoTime.setOneResource(name);
                for (Map.Entry<String, ActorRef<BasicCommon>> entry : GlobalAkkaPara.resourceActorRefMap.entrySet()) {
                    if (!entry.getKey().equals(this.name)) {
                        entry.getValue().tell(frontAskTwoTime);
                    }
                }
            } else {
                FrontResponseOneTime frontResponseOneTime = new FrontResponseOneTime();
                frontResponseOneTime.setIsSuccess(false);
                frontResponseOneTime.setZeroResource(msg.getZeroResource());
                frontResponseOneTime.setOneResource(name);
                List<ProcessInfo> list = new ArrayList<>(msg.getProcessInfos());
                frontResponseOneTime.setProcessInfos(list);
                GlobalAkkaPara.resourceActorRefMap.get(msg.getZeroResource()).tell(frontResponseOneTime);
                // 直接回复
            }
            haveAssignedProcessInfos.add(curLastProcess);


        }

        return this;
    }

    private Behavior<BasicCommon> handleDealExchange(DealExchange msg) {
        ProcessInfo time = new ProcessInfo(msg.getStartTime(), msg.getEndTime());
        haveAssignedProcessInfos.add(time);
//        System.out.print("dealing " + msg.getTaskName() + " " + msg.getResourceName());
        for (int i = 0; i< haveAssignedProcessInfos.size(); i++) {
            System.out.print("handle deal " + haveAssignedProcessInfos.get(i).getStartTime() + " " + haveAssignedProcessInfos.get(i).getEndTime());
        }
        System.out.println();

        Collections.sort(this.haveAssignedProcessInfos,new Comparator<ProcessInfo>() {
            public int compare(ProcessInfo a, ProcessInfo b) {
                return a.getStartTime() - b.getStartTime();
            }
        });

        return this;
    }

    private Behavior<BasicCommon> handleResponseExchange(ResponseExchange msg) {
//        System.out.println("responseExchange..." + responseRounds);

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

                for (int i = 0; i< haveAssignedProcessInfos.size(); i++) {
                    if (msg.getPreStartTime() == haveAssignedProcessInfos.get(i).getStartTime() &&
                        msg.getPreEndTime() == haveAssignedProcessInfos.get(i).getEndTime()) {
                        haveAssignedProcessInfos.remove(i);
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
                ProcessInfo time = msg.getWaitProcessTimes().get(0);
                exchangeMaps.put(msg.getFromResourceName(), new int[]{time.getStartTime(), time.getEndTime()});
            }
        }
        return this;
    }

    private Behavior<BasicCommon> handleAskingExchange(AskingExchange msg) {

        int spendingTime = processTimes.get(Integer.parseInt(msg.getTask()));

        ResponseExchange responseExchange = new ResponseExchange();
        List<ProcessInfo> waitProcessTimes = new ArrayList<>();
        int preTime = msg.getPreTime();
        // start
        int minStartTime = Integer.MAX_VALUE;
        int minEndTime = Integer.MAX_VALUE;

        if (haveAssignedProcessInfos.size() == 0) {
            minStartTime = preTime;
            minEndTime = preTime+spendingTime;

//            ProcessTime time = new ProcessTime(preTime, preTime+spendingTime);
//            waitProcessTimes.add(time);
        } else {
            int waitStartTime = haveAssignedProcessInfos.get(0).getEndTime();
            boolean tag = true;
            for (int i = 1; i < haveAssignedProcessInfos.size(); i++) {
                int waitEndTime = haveAssignedProcessInfos.get(i).getStartTime();
                if (waitEndTime > preTime && preTime >= waitStartTime && waitEndTime - preTime >= spendingTime) {
                    minStartTime = preTime;
                    minEndTime = preTime + spendingTime;
//                    ProcessTime time = new ProcessTime(startTime, startTime+spendingTime);
//                    waitProcessTimes.add(time);
                    tag = false;
                    break;
                }
                waitStartTime = haveAssignedProcessInfos.get(i).getEndTime();
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
            ProcessInfo time = new ProcessInfo(minStartTime, minEndTime);

            waitProcessTimes.add(time);
        }

//        System.out.print("1111111 ");
//        for (int i = 0; i< haveAssignedProcessInfos.size(); i++) {
//            System.out.print("assign " + haveAssignedProcessInfos.get(i).getStartTime() + " " + haveAssignedProcessInfos.get(i).getEndTime());
//        }
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
//        System.out.println("handleDealMsg " + msg);
        FrontAskOneTime frontAskOneTime = new FrontAskOneTime();
        ProcessInfo processInfo = msg.getProposeProcessInfo();

        List<ProcessInfo> processInfos = new ArrayList<>();
        processInfos.add(processInfo);
        frontAskOneTime.setFromResourceName(name);
        frontAskOneTime.setZeroResource(name);
//        frontAskOneTime.setProcessInfo(processInfo);
        frontAskOneTime.setProcessInfos(processInfos);

        for(Map.Entry<String, ActorRef<BasicCommon>> entry : GlobalAkkaPara.resourceActorRefMap.entrySet()) {
            if (!entry.getKey().equals(this.name)){
                entry.getValue().tell(frontAskOneTime);
            }
        }
        return this;
    }

    private Behavior<BasicCommon> handleBidingMsg(BidingMsg msg) {
        int spendingTime = processTimes.get(Integer.parseInt(msg.getTaskProcessNum()));
        ProposeMsg proposeMsg = new ProposeMsg();
        int preTime = msg.getPreTime();
        if (haveAssignedProcessInfos.size() == 0) {
            ProcessInfo proposeProcessInfo = new ProcessInfo(preTime, preTime+spendingTime);
            proposeProcessInfo.setTaskName(msg.getTaskName());
            proposeProcessInfo.setNo(msg.getNo());
            proposeProcessInfo.setPreTime(msg.getPreTime());
            proposeProcessInfo.setResourceName(name);
            proposeProcessInfo.setTaskProcessNum(msg.getTaskProcessNum());
            proposeProcessInfo.setTaskProcessNum(msg.getTaskProcessNum());
            proposeMsg.setProposeProcessInfo(proposeProcessInfo);
        } else {
            int waitStartTime = haveAssignedProcessInfos.get(0).getEndTime();
            boolean tag = true;
            for (int i = 1; i < haveAssignedProcessInfos.size(); i++) {
                int waitEndTime = haveAssignedProcessInfos.get(i).getStartTime();
                if (waitEndTime > preTime && preTime >= waitStartTime && waitEndTime - preTime >= spendingTime) {
                    ProcessInfo proposeProcessInfo = new ProcessInfo(preTime, preTime+spendingTime);
                    proposeProcessInfo.setTaskProcessNum(msg.getTaskProcessNum());
                    proposeProcessInfo.setTaskName(msg.getTaskName());
                    proposeProcessInfo.setResourceName(name);
                    proposeProcessInfo.setNo(msg.getNo());
                    proposeProcessInfo.setPreTime(msg.getPreTime());
                    proposeMsg.setProposeProcessInfo(proposeProcessInfo);
                    tag = false;
                    break;
                }
                waitStartTime = haveAssignedProcessInfos.get(i).getEndTime();
            }
            if (tag) {
                waitStartTime = Math.max(waitStartTime, preTime);
                ProcessInfo proposeProcessInfo = new ProcessInfo(waitStartTime, waitStartTime+spendingTime);
                proposeProcessInfo.setTaskProcessNum(msg.getTaskProcessNum());
                proposeProcessInfo.setTaskName(msg.getTaskName());
                proposeProcessInfo.setResourceName(name);
                proposeProcessInfo.setNo(msg.getNo());
                proposeProcessInfo.setPreTime(msg.getPreTime());
                proposeMsg.setProposeProcessInfo(proposeProcessInfo);
            }
        }

//        for (int i = 0; i< haveAssignedProcessInfos.size(); i++) {
//            System.out.print("assign " + haveAssignedProcessInfos.get(i).getStartTime() + " " + haveAssignedProcessInfos.get(i).getEndTime());
//        }

        proposeMsg.setNo(msg.getNo());
        proposeMsg.setTaskProcessNum(msg.getTaskProcessNum());
        proposeMsg.setTaskName(msg.getTaskName());
        proposeMsg.setResourceName(name);

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

