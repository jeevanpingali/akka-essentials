package part2actors

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import part2actors.ChildActors.CreditCard.{AttachedToAccount, CheckStatus}
import part2actors.ChildActors.Parent.{CreateChild, TellChild}

object ChildActors extends App {
  // Actors can create other actors

  object Parent {
    case class CreateChild(name: String)
    case class TellChild(message: String)
  }

  class Parent extends Actor {
    import Parent._
    override def receive: Receive = {
      case CreateChild(name) =>
        println(s"${self.path} creating child")
        // how to create a new actor inside the receive
        val childRef = context.actorOf(Props[Child], name)
        context.become(withChild(childRef))
    }

    def withChild(childRef: ActorRef): Receive = {
      case TellChild(message) => childRef forward message
    }
  }

  class Child extends Actor {
    override def receive: Receive = {
      case message => println(s"${self.path}: I got: $message")
    }
  }

  val system = ActorSystem("ParentChildDemo")

  val parent = system.actorOf(Props[Parent], "parent")
  parent ! CreateChild("child")
  parent ! TellChild("Hey kid!")

  /**
   * Actor selection
   */

  val childSelection = system.actorSelection("/user/parent/child")
  childSelection ! "I found you"

  /**
   * NEVER PASS MUTABLE ACTOR STATE, OR THE "THIS" REFERENCE TO CHILD ACTORS
   *
   * NEVER IN YOUR LIFE :)
   */

  object NaiveBankAccount {
    case class Deposit(amount: Double)
    case class Withdraw(amount: Double)
    case object InitializeAccount
  }

  class NaiveBankAccount extends Actor {
    import NaiveBankAccount._
    import CreditCard._

    var amount = 0.0d
    override def receive: Receive = {
      case InitializeAccount =>
        val creditCardRef = context.actorOf(Props[CreditCard], "card")
        creditCardRef ! AttachedToAccount(this) // !!
      case Deposit(funds) => deposit(funds)
      case Withdraw(funds) => withdraw(funds)
    }

    def deposit(funds: Double) = {
      println(s"${self.path} depositing $funds on top of $amount")
      amount += funds
    }

    def withdraw(funds: Double) = {
      println(s"${self.path} withdrawing $funds from $amount")
      amount -= funds
    }
  }

  object CreditCard {
    case class AttachedToAccount(bankAccount: NaiveBankAccount) // this is questionable
    case object CheckStatus
  }
  class CreditCard extends Actor {

    def attachedTo(account: NaiveBankAccount): Receive = {
      case CheckStatus =>
        println(s"${self.path} your message has been processed")
        account.withdraw(1.0)
    }

    override def receive: Receive = {
      case AttachedToAccount(account) => context.become(attachedTo(account))
    }
  }

  import NaiveBankAccount._
  import CreditCard._

  val bankAccount = system.actorOf(Props[NaiveBankAccount], "account")
  bankAccount ! InitializeAccount
  bankAccount ! Deposit(100)

  Thread.sleep(500)

  val creditCard = system.actorSelection("/user/account/card")
  creditCard ! CheckStatus

  system.terminate()
}
