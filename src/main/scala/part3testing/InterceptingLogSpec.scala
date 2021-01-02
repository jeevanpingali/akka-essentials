package part3testing

import akka.actor.{Actor, ActorLogging, ActorSystem, Props}
import akka.event.DefaultLoggingFilter
import akka.io.Inet.SO.ReceiveBufferSize
import akka.testkit.{EventFilter, ImplicitSender, TestKit}
import com.typesafe.config.ConfigFactory
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}

class InterceptingLogSpec extends TestKit(ActorSystem("InterceptingLogSpec", ConfigFactory.load("interceptingLogMessages")))
  with ImplicitSender
  with WordSpecLike
  with BeforeAndAfterAll {

  override protected def afterAll(): Unit = {
    TestKit.shutdownActorSystem(system)
  }

  import InterceptingLogSpec._
  val item = "Rock the JVM Akka course"
  val creditCard = "1234-1234-1234-1234"
  "A checkout flow" should {
    "correctly log the dispatch of an order" in {
      EventFilter.info(pattern = s"Order [0-9]+ for item $item has been dispatched.") intercept {
        // our test code
        val checkoutRef = system.actorOf(Props[CheckoutActor])
        checkoutRef ! Checkout(item, creditCard)
      }
    }
  }

}

object InterceptingLogSpec {
  case class Checkout(item: String, creditCard: String)
  case class AuthorizeCard(creditCard: String)
  case object PaymentAccepted
  case object PaymentDenied
  case class DispatchOrder(item: String)
  case object OrderConfirmed
  class CheckoutActor extends Actor {
    private val paymentManager = context.actorOf(Props[PaymentManager])
    private val fullfillmentManager = context.actorOf(Props[FulfillmentManager])

    override def receive: Receive = awaitingCheckout

    def waitingCheckout: Receive = {
      case Checkout(item, creditCard) =>
        paymentManager ! AuthorizeCard(creditCard)
        context.become(pendingPayment(item))
    }

    def awaitingCheckout: Receive = {
      case Checkout(item, creditCard) =>
        paymentManager ! AuthorizeCard(creditCard)
        context.become(pendingPayment(item))
    }

    def pendingPayment(item: String): Receive = {
      case PaymentAccepted =>
        fullfillmentManager ! DispatchOrder(item)
        context.become(pendingFulfillment(item))
      case PaymentDenied => // TODO
    }

    def pendingFulfillment(item: String): Receive = {
      case OrderConfirmed => context.become(awaitingCheckout)
    }
  }

  class PaymentManager extends Actor {
    override def receive: Receive = {
      case AuthorizeCard(creditCard) =>
        if(creditCard.startsWith("0")) sender() ! PaymentDenied
        else sender() ! PaymentAccepted
    }
  }

  class FulfillmentManager extends Actor with ActorLogging {
    var orderId = 0
    override def receive: Receive = {
      case DispatchOrder(item: String) =>
        orderId += 1
        log.info(s"Order $orderId for item $item has been dispatched.")
        sender() ! OrderConfirmed
    }
  }
}


