package cloud.global;

import akka.actor.typed.ActorRef;
import akka.actor.typed.ActorSystem;
import akka.actor.typed.javadsl.Behaviors;
import base.model.bean.BasicCommon;

import java.util.*;

/**
 * @author ：LLH
 * @date ：Created in 2021/5/9 21:51
 * @description：全局actorRef
 */
public class GlobalAkkaPara {
    public final static Map<String, ActorRef<BasicCommon>> globalActorRefMap = new HashMap<>();
    public final static Map<String, ActorRef<BasicCommon>> resourceActorRefMap = new HashMap<>();
    public final static ActorSystem<Void> system = ActorSystem.create(Behaviors.empty(), "bootstrap");
    public final static List<String> podList = new ArrayList<>(Arrays.asList("pod-111"));

    public static int taskNum = 1;
    public static int rapid =1;

    public static int taskSize = 20;
    public static int resourceSize = 20;

    public static String dataPath = "D:\\Coding\\JavaProject\\multi-agent4\\data\\";
}
