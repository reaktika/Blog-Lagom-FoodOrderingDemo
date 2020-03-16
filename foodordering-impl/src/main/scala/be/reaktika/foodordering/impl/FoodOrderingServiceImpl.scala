package be.reaktika.foodordering.impl

import java.util.UUID

import akka.Done
import akka.NotUsed
import akka.cluster.sharding.typed.scaladsl.ClusterSharding
import akka.cluster.sharding.typed.scaladsl.EntityRef
import com.lightbend.lagom.scaladsl.api.ServiceCall
import com.lightbend.lagom.scaladsl.persistence.PersistentEntityRegistry

import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration._
import akka.util.Timeout
import be.reaktika.foodordering.api.Commands._
import be.reaktika.foodordering.api.{FoodCartViewResult, FoodOrderingService}
import com.lightbend.lagom.scaladsl.api.transport.BadRequest

class FoodOrderingServiceImpl(
  clusterSharding: ClusterSharding,
  persistentEntityRegistry: PersistentEntityRegistry
)(implicit ec: ExecutionContext)
  extends FoodOrderingService {

  private def aggregateRef(id: UUID): EntityRef[FoodCartCommand] =
    clusterSharding.entityRefFor(FoodCartState.typeKey, id.toString)

  implicit val timeout: Timeout = Timeout(5.seconds)

  override def createCart(): ServiceCall[NotUsed, UUID] = ServiceCall {
    _ =>
      val newFoodCartId = UUID.randomUUID()
      aggregateRef(newFoodCartId)
        .ask[CartCreated](replyTo => CreateCartCommand(newFoodCartId, replyTo))
        .map(_.id)
  }

  override def selectItem(foodCartId: UUID, productId: UUID, quantity: Int): ServiceCall[NotUsed, Done]= ServiceCall { _ =>
    aggregateRef(foodCartId)
      .ask[Confirmation](
        replyTo => SelectProductCommand(foodCartId, productId, quantity, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't select the specified products.")
      }
  }

  override def deselectItem(foodCartId: UUID, productId: UUID, quantity: Int): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    aggregateRef(foodCartId)
      .ask[Confirmation](
        replyTo => DeselectProductCommand(foodCartId, productId, quantity, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't deselect the specified products.")
      }
  }

  override def confirmCart(foodCartId: UUID): ServiceCall[NotUsed, Done] = ServiceCall { _ =>
    aggregateRef(foodCartId)
      .ask[Confirmation](
        replyTo => ConfirmOrderCommand(foodCartId, replyTo)
      )
      .map {
        case Accepted => Done
        case _        => throw BadRequest("Can't confirm the specified products.")
      }
  }

  override def getFoodCartItems(foodCartId: UUID): ServiceCall[NotUsed, FoodCartViewResult] = { _ =>
    val content = FoodCartViewRepository.getCatalog(foodCartId)
    Future.successful(FoodCartViewResult(content))
  }

}
