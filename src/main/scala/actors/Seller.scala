package actors

import actors.Auction.StartAuction
import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive
import messages._

class Seller(var id: String) extends Actor {
  override def receive: Receive = {
    case RegisterSelf => {
      context.actorSelection("/user/" + "AuctionSearch") ! RegisterSeller(self, this.id)
    }
    case message: CreateAuction => {
      val newAuction = context.actorOf(Props(new Auction(message.name)))
      newAuction ! StartAuction
      context.actorSelection("/user/" + "AuctionSearch") ! RegisterAuction(newAuction, message.name)
      println("Seller created auction")
    }
    case auctionSold: AuctionSold =>
      println("Super! auction sold for " + auctionSold.value.toString)
  }
}
