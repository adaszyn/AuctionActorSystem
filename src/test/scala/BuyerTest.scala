import actors.Auction.{GetCurrentValue, StartAuction}
import actors.{Auction, AuctionSearch, Buyer, Seller}
import akka.actor.{ActorRef, ActorSystem, Props}
import akka.pattern.ask
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import akka.util.Timeout
import messages._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


class BuyerTest extends TestKit(ActorSystem("BuyerSpec")) with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    system.terminate
  }
  "Buyer" must {
    "should respond when auction is lost" in {
      import scala.concurrent.duration._
      val TEST_BUYER_NAME = "TestBuyerName"
      val buyer = TestActorRef(new Buyer(TEST_BUYER_NAME))

      val buyer2 = TestProbe()
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      buyer ! BidAuctionResponseByActor(auction, 1000)
      buyer2.send(auction, Bid(999.5, TEST_BUYER_NAME))
      val probe = TestProbe()
      probe.send(auction, GetCurrentValue)
      probe.expectMsg(500 millis, 1000.0)
    }
  }
}

