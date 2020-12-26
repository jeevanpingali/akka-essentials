package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}

object ActorCapabilities extends App {
  class SimpleActor extends Actor {
    override def receive: Receive = {
      case "Hi" => sender() ! "Hello, there!"
      case message: String => println(s"[$self] I have received $message")
      case number: Int => println(s"I have received a number: $number")
      case SpecialMessage(contents) => println(s"[simple actor] I have received something special: ${contents}")
      case SendMessageToYourself(content) =>
        self ! content
      case SayHiTo(ref) => ref ! "Hi"
      case WirelessPhoneMessage(content, ref) => ref forward (content + "s")
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

  // 3 - actors can reply to messages
  val alice = system.actorOf(Props[SimpleActor], "alice")
  val bob = system.actorOf(Props[SimpleActor], "bob")

  case class SayHiTo(ref: ActorRef)

  alice ! SayHiTo(bob)

  // 4 - dead letters
  alice ! "Hi"

  // 5 - forwarding messages
  case class WirelessPhoneMessage(content: String, ref: ActorRef)

  alice ! WirelessPhoneMessage("Hi", bob)

  system.terminate()
}
