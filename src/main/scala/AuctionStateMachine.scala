import actors.AuctionMain
import akka.actor.{Actor, ActorRef, ActorSystem, Cancellable, Inbox, Props}
import messages._

object AuctionApp extends App {
  println("Starting auction application")
  val system = ActorSystem("helloakka")
  val auctionMain = system.actorOf(Props[AuctionMain], name="auction")
  val inbox = Inbox.create(system)

  inbox.send(auctionMain, InitAuctionApp(system))
}
