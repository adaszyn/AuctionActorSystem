package actors

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import messages.{FindAuction, InitAuctionSearch, NoAuctionFound, RegisterAuction}

import scala.collection.immutable.Map
import scala.concurrent.ExecutionContext.Implicits.global


class AuctionSearch extends Actor {
  var map: Map[String, ActorRef] = Map()
  override def receive: Receive = process()
  def process(): Receive = {
    case registerAuction: RegisterAuction =>
      println("AuctionSearch registered auction: " + registerAuction.id)
      this.map = this.map.+(registerAuction.id -> registerAuction.auctionRef)
    case findAuction: FindAuction =>
      this.map.get(findAuction.id) match {
        case Some(actor: ActorRef) => sender ! actor
        case None => sender ! NoAuctionFound(findAuction.id)
      }
    case InitAuctionSearch =>
      println("Initializing auctionSearch actor")
  }
}
