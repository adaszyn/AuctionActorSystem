package actors

import akka.actor.{Actor, Props}
import akka.actor.Actor.Receive
import messages.{AuctionSold, CreateAuction, RegisterAuction, StartAuction}

class Seller extends Actor {
  override def receive: Receive = {
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
