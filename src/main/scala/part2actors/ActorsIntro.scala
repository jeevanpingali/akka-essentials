package part2actors

import akka.actor.{Actor, ActorSystem, Props}

object ActorsIntro extends App {
  // Part 1 - actor systems
  val actorSystem = ActorSystem("firstActorSystem")
  println(actorSystem.name)

  // part2 - create actors
  // word count actor
  class WorkCountActor extends Actor {
    var totalWords: Int = 0

    // behavior
    def receive: PartialFunction[Any, Unit] = {
      case message: String =>
        println(s"[word counter] I have received: $message")
        totalWords += message.split(" ").length
      case msg => println(s"[word counter] I cannot understand ${msg.toString}")
    }
  }

  // part3 - instantiate our actor
  val wordCounter = actorSystem.actorOf(Props[WorkCountActor], "wordCounter")
  val anotherWordCounter = actorSystem.actorOf(Props[WorkCountActor], "anotherWordCounter")

  // part4 - communicate!
  wordCounter ! "I am learning Akka and it's pretty damn cool!" // "tell"
  anotherWordCounter ! "A different message"
  // asynchronous
}
