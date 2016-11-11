import actors.Auction.StartAuction
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
      val seller = TestActorRef(new Seller("TestSeller"))
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      assert (auction.underlyingActor.state.currentValue == 0.0)
    }

    "change currentValue to bid.value" in {
      val seller = TestActorRef(new Seller("TestSeller"))
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      auction ! Bid(1000.0, "TestSeller")
      assert (auction.underlyingActor.state.currentValue == 1000.0)
    }

    "not change currentValue when bid is lower" in {
      val TEST_SELLER_NAME = "TestSeller"
      val seller = TestActorRef(new Seller(TEST_SELLER_NAME))
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      auction ! Bid(1000.0, TEST_SELLER_NAME)
      auction ! Bid(900.0, TEST_SELLER_NAME)
      assert (auction.underlyingActor.state.currentValue == 1000.0)
    }

    "should respond when auction is lost" in {
      import scala.concurrent.duration._
      val TEST_BUYER_NAME = "TestBuyer"
      val buyer = TestProbe()
      val buyer2 = TestProbe()
      val auction = TestActorRef(new Auction("TestAuction"))
      auction ! StartAuction
      buyer.send(auction, Bid(500, TEST_BUYER_NAME))
      buyer2.send(auction, Bid(1000, TEST_BUYER_NAME))
      buyer.expectMsg(500 millis, LostBid(1000.0))
    }
  }
}

