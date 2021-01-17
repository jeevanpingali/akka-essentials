package par5infra

import akka.actor.{Actor, ActorLogging, ActorSystem, Props, Terminated}
import akka.routing.{ActorRefRoutee, Broadcast, FromConfig, RoundRobinGroup, Router, SmallestMailboxRoutingLogic}
import com.typesafe.config.ConfigFactory

import scala.concurrent.duration._

object Routers extends App {

  /**
   * Method 1 : Manual router
   */
  class Master extends Actor {
    private val slaves = for (_ <- 1 to 5) yield {
      val slave = context.actorOf(Props[Slave])
      context.watch(slave)

      ActorRefRoutee(slave)
    }

    private val router = Router(SmallestMailboxRoutingLogic(), slaves)

    override def receive: Receive = {
      case Terminated(ref) =>
        router.removeRoutee(ref)
        val newSlave = context.actorOf(Props[Slave])
        context.watch(newSlave)
        router.addRoutee(newSlave)
      case message =>
        router route(message, sender())
    }
  }

  class Slave extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  val system = ActorSystem("RouterDemo", ConfigFactory.load().getConfig("routersDemo"))

  //  val master = system.actorOf(Props[Master])

  /*
    for(i <- 1 to 10) {
      master ! s"[$i] Hello from the world!"
    }
  */

  /**
   * Method 2 - Pool router
   */
  // 2.1 programmatically
  /*
    val poolAMaster = system.actorOf(RoundRobinPool(5).props(Props[Slave]), "simplePoolMaster")
    for(i <- 1 to 10) {
      poolAMaster ! s"[$i] Hello from the world!"
    }
  */

  // 2.2 from configuration
  /*
    val poolMaster2 = system.actorOf(FromConfig.props(Props[Slave]), "poolMater2")
    for(i <- 1 to 10) {
      poolMaster2 ! s"[$i] Hello from the world!"
    }
  */

  /**
   * Method 3 - router with actors created elsewhere
   * GROUP router
   */
  // .. in another part of my application
  val slaveList = (1 to 5).map(i => system.actorOf(Props[Slave], s"slave_$i")).toList
  val slavePaths = slaveList.map(slaveRef => slaveRef.path.toString)

  // 3.1 in the code
  /*
    val groupMaster = system.actorOf(RoundRobinGroup(slavePaths).props())
    for(i <- 1 to 10) {
      groupMaster ! s"[$i] Hello from the world!"
    }
  */
  // 3.2 from configuration
  val groupMaster2 = system.actorOf(FromConfig.props(), "groupMater2")
  for(i <- 1 to 10) {
    groupMaster2 ! s"[$i] Hello from the world!"
  }

  /**
   * Special messages
   */
  groupMaster2 ! Broadcast("hello, everyone")



  import system.dispatcher

  system.scheduler.scheduleOnce(10 seconds) {
    system.terminate()
  }
}
