package actors

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}
import akka.util.Timeout
import messages._


class AuctionMain extends Actor {
  override def receive: Receive = {
    case init:InitAuctionApp => {
      val numberOfBuyers = 10
      val numberOfAuctions = 30
      val auctionSearch = init.system.actorOf(Props[AuctionSearch], name="AuctionSearch")

      init.system.actorSelection("/user/" + "AuctionSearch") ! InitAuctionSearch


      val seller1 = init.system.actorOf(Props[Seller])
//      val seller2 = init.system.actorOf(Props[Seller])
//      val seller3 = init.system.actorOf(Props[Seller])
      val buyer1 = init.system.actorOf(Props(new Buyer()))
      val buyer2 = init.system.actorOf(Props(new Buyer()))
      val buyer3 = init.system.actorOf(Props(new Buyer()))
//      seller1 ! CreateAuction("Super auction 1")
//      seller1 ! CreateAuction("Foo bar")
//      seller1 ! CreateAuction("Lorem Ipsum")
//      buyer1 ! BidAuctionLoop("Lorem Ipsum")
//      buyer2 ! BidAuctionLoop("Foo bar")
//      buyer3 ! BidAuctionLoop("Super auction 1")



      // BID WITH RESPONSE TEST
      seller1 ! CreateAuction("Lorem Ipsum")
      buyer1 ! BidAuctionResponse("Lorem Ipsum", 1000.0)
      buyer2 ! BidAuctionResponse("Lorem Ipsum", 1000.0)
      buyer3 ! BidAuctionResponse("Lorem Ipsum", 1000.0)

    }
  }
}
