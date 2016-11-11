package actors

import akka.actor.{Actor, ActorRef}
import akka.actor.Actor.Receive
import messages._

import scala.collection.immutable.Map
import scala.concurrent.ExecutionContext.Implicits.global

object AuctionSearch {
  case class SendMessageToBuyer(buyerId: String, message: Any)
  case class SendMessageToSeller(buyerId: String, message: Any)
  case class NotFound()
  case class NoAuctionFound(name: String)
  case class NoBuyerFound(name: String)
  case class NoSellerFound(name: String)
}

class AuctionSearch extends Actor {
  import actors.AuctionSearch._

  var auctionMap: Map[String, ActorRef] = Map()
  var buyerMap: Map[String, ActorRef] = Map()
  var sellerMap: Map[String, ActorRef] = Map()
  override def receive: Receive = process()
  def process(): Receive = {
    case InitAuctionSearch =>
      println("Initializing auctionSearch actor")
    case registerAuction: RegisterAuction =>
      println("AuctionSearch registered auction: " + registerAuction.id)
      this.auctionMap = this.auctionMap.+(registerAuction.id -> registerAuction.auctionRef)
    case findAuction: FindAuction =>
      this.auctionMap.get(findAuction.id) match {
        case Some(actor: ActorRef) => sender ! actor
        case None => sender ! NoAuctionFound(findAuction.id)
      }
    case registerBuyer: RegisterBuyer =>
      println("Registering BUYER")
      this.buyerMap= this.buyerMap.+(registerBuyer.id -> registerBuyer.auctionRef)
    case findBuyer: FindBuyer =>
      this.buyerMap.get(findBuyer.id) match {
        case Some(actor: ActorRef) => sender ! actor
        case None => sender ! NoBuyerFound(findBuyer.id)
      }
    case registerSeller: RegisterSeller =>
      println("Registering SELLER")
      this.sellerMap= this.buyerMap.+(registerSeller.id -> registerSeller.auctionRef)
    case findSeller: FindSeller =>
      this.sellerMap.get(findSeller.id) match {
        case Some(actor: ActorRef) => sender ! actor
        case None => sender ! NoSellerFound(findSeller.id)
      }

    case SendMessageToBuyer(buyerId: String, message: Any) => {
      this.buyerMap.get(buyerId) match {
        case Some(actor: ActorRef) => actor ! message
        case None => sender ! NoBuyerFound
      }
    }
    case SendMessageToSeller(sellerId: String, message: Any) => {
      this.sellerMap.get(sellerId) match {
        case Some(actor: ActorRef) => actor ! message
        case None => sender ! NoSellerFound
      }
    }
  }
}
