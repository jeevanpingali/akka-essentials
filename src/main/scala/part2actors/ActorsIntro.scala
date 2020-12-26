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
    def receive: Receive = {
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

  object Person {
    def props(name: String) = Props(new Person(name))
  }
  class Person(name: String) extends Actor {
    override def receive: Receive = {
      case "hi" => println(s"Hi, my name is $name")
      case _ =>
    }
  }

  val person1 = actorSystem.actorOf(Props(new Person("Bob 1"))) // Not a best practice
  val person2 = actorSystem.actorOf(Person.props("Bob 2")) // Best practice

  person1 ! "hi"
  person2 ! "hi"

  Thread.sleep(1000 * 10)
  actorSystem.terminate()
}
