package messages

import akka.actor.{ActorRef, ActorSystem, Cancellable}

case class InitAuctionApp(system: ActorSystem)

case class SayHello(value:String)
case class Bid(value:Double, buyerId: String)
case class LostBid(value:Double)
case class BidAuction(auctionRef:ActorRef)
case class BidAuctionLoop(name: String)
case class BidAuctionResponse (name: String, maximumValue: Double)
case class BidAuctionResponseByActor (actor: ActorRef, maximumValue: Double)


case class DeleteAuction()
case class RelistAuction()
case class TimerTick(cancellable: Cancellable)


// Creating auctions
case class CreateAuction(name: String)


// Auction Search creation and lifecycle
case class InitAuctionSearch()
case class RegisterAuction(auctionRef: ActorRef, id: String)
case class RegisterBuyer(auctionRef: ActorRef, id: String)
case class RegisterSeller(auctionRef: ActorRef, id: String)
case class FindAuction(id: String)
case class FindSeller(id: String)
case class FindBuyer(id: String)


case class Identify()
case class Identity(actorRef: ActorRef)


case class RegisterSelf()
case class AuctionSold(value: Double, auctionName: String)