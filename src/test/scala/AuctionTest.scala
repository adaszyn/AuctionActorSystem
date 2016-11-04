import actors.{Auction, AuctionSearch, Seller}
import akka.actor.{ActorRef, ActorSystem}
import akka.testkit.{TestActorRef, TestKit, TestProbe}
import messages._
import org.scalatest.{BeforeAndAfterAll, WordSpecLike}


class AuctionTest extends TestKit(ActorSystem("AuctionSpec")) with WordSpecLike with BeforeAndAfterAll {
  override def afterAll(): Unit = {
    system.terminate
  }
  "Auction" must {

    "start with 0.0 value" in {
      val seller = TestActorRef(new Seller())
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      assert (auction.underlyingActor.currentValue == 0.0)
    }

    "change currentValue to bid.value" in {
      val seller = TestActorRef(new Seller())
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      auction ! Bid(1000.0)
      assert (auction.underlyingActor.currentValue == 1000.0)
    }

    "not change currentValue when bid is lower" in {
      val seller = TestActorRef(new Seller())
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      auction ! Bid(1000.0)
      auction ! Bid(900.0)
      assert (auction.underlyingActor.currentValue == 1000.0)
    }

    "should respond when auction is lost" in {
      import scala.concurrent.duration._

      val buyer = TestProbe()
      val buyer2 = TestProbe()
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      buyer.send(auction, Bid(500))
      buyer2.send(auction, Bid(1000))
      buyer.expectMsg(500 millis, LostBid(1000.0))
    }
  }
}

