package part2actors

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import com.typesafe.config.ConfigFactory

object IntroAkkaConfig extends App {

  class SimpleLoggingActor extends Actor with ActorLogging {
    override def receive: Receive = {
      case message => log.info(message.toString)
    }
  }

  /**
   * 1 - inline configuration
   */

  val configString =
    """
      |akka {
      | loglevel = "INFo"
      |}
      |""".stripMargin

  val config = ConfigFactory.parseString(configString)
  val system = ActorSystem("ConfigDemo", ConfigFactory.load(config))
  val actor = system.actorOf(Props[SimpleLoggingActor])

  actor ! "A Message to remember"

  system.terminate()

  /**
   * Default configuration - application.conf
   */

  val system2 = ActorSystem("DefaultConfigDemo")
  val actor2 = system2.actorOf(Props[SimpleLoggingActor])
  actor2 ! "Remember me"
  system2.terminate()

  /**
   * 3 - separate configuraion in same file using multiple namespaces
   */
  val specialConfig = ConfigFactory.load().getConfig("mySpecialConfig")
  val specialConfigSystem = ActorSystem("SpecialConfigDemo", specialConfig)
  val specialConfigActor = specialConfigSystem.actorOf(Props[SimpleLoggingActor])
  specialConfigActor ! "Remember me, I'm special"
  specialConfigSystem.terminate()

  /**
   * 4 - separate config in another file
   */

  val separateConfig = ConfigFactory.load("secretFolder/secretConfiguration.conf")
  println(s"Separate Config log level: ${separateConfig.getString("akka.loglevel")}")

  /**
   * 5 - different file formats
   * can use JSON or properties files
   */
  val jsonConfig = ConfigFactory.load("json/jsonConfig.json")
  println(s"Separate JSON Config log level: ${jsonConfig.getString("akka.loglevel")} ${jsonConfig.getString("aJSONProperty")}")

  val propsConfig = ConfigFactory.load("props/propsConfiguration.properties")
  println(s"Separate PROPS Config log level: ${propsConfig.getString("my.simpleConfiguration")}")
}
