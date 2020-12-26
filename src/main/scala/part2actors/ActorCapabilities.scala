package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case message: String => println(s"[$self] I have received $message")
      case number: Int => println(s"I have received a number: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received something special: ${contents}")
      case SendMessageToYourself(content) => self ! content
    }
  }

  val system = ActorSystem("ActorCapabilitiesDemo")
  val simpleActor = system.actorOf(Props[SimpleActor], "simpleActor")

  simpleActor ! "hello, actor"

  // 1 - messages can be of any type
  // a) message must be immutable
  // b) messages must be serializable
  // in practice, use case classes and case objects
  simpleActor ! 42

  case class SpecialMessage(contents: String)
  simpleActor ! SpecialMessage("Some special content")

  // 2 - actors have information about their context and about themselves
  // context.self === "this" in OOP

  case class SendMessageToYourself(content: String)
  simpleActor ! SendMessageToYourself("I am an actor and I'm proud of it")


  system.terminate()
}
