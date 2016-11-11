package actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import messages._


class AuctionMain extends Actor {
  override def receive: Receive = {
    case init:InitAuctionApp => {
      val testAuctionName = "Lorem Ipsum"
      val auctionSearch = init.system.actorOf(Props[AuctionSearch], name="AuctionSearch")

      init.system.actorSelection("/user/" + "AuctionSearch") ! InitAuctionSearch

      val seller = init.system.actorOf(Props(new Seller("Waclaw")))
      seller ! RegisterSelf
      val buyer = init.system.actorOf(Props(new Buyer("Janusz")))
      buyer ! RegisterSelf


      // BID WITH RESPONSE TEST
      seller ! CreateAuction(testAuctionName)
      buyer ! BidAuctionLoop(testAuctionName)

    }
  }
}
