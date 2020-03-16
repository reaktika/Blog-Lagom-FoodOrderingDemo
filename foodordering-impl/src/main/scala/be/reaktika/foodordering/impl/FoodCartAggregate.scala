package be.reaktika.foodordering.impl

import play.api.libs.json.Json
import play.api.libs.json.Format
import java.util.UUID

import akka.actor.typed.Behavior
import akka.cluster.sharding.typed.scaladsl._
import akka.persistence.typed.PersistenceId
import akka.persistence.typed.scaladsl.Effect
import akka.persistence.typed.scaladsl.EventSourcedBehavior
import akka.persistence.typed.scaladsl.ReplyEffect
import be.reaktika.foodordering.api.Commands._
import be.reaktika.foodordering.api.Events._
import be.reaktika.foodordering.api.Formats
import com.lightbend.lagom.scaladsl.persistence.AkkaTaggerAdapter
import com.lightbend.lagom.scaladsl.playjson.JsonSerializer
import com.lightbend.lagom.scaladsl.playjson.JsonSerializerRegistry

import scala.collection.immutable.Seq

object FoodCartBehavior {

  def create(entityContext: EntityContext[FoodCartCommand]): Behavior[FoodCartCommand] = {
    val persistenceId: PersistenceId = PersistenceId(entityContext.entityTypeKey.name, entityContext.entityId)

    create(persistenceId)
      .withTagger(
        AkkaTaggerAdapter.fromLagom(entityContext, FoodCartEvent.Tag)
      )
  }

  private[impl] def create(persistenceId: PersistenceId) = EventSourcedBehavior
      .withEnforcedReplies[FoodCartCommand, FoodCartEvent, FoodCartState](
        persistenceId = persistenceId,
        emptyState = FoodCartState.initial,
        commandHandler = (state, cmd) => state.applyCommand(cmd),
        eventHandler = (state, evt) => state.applyEvent(evt)
      )
}

case class FoodCartState(products: Map[UUID, Int], confirmed: Boolean = false) {
  def applyCommand(cmd: FoodCartCommand): ReplyEffect[FoodCartEvent, FoodCartState] =
    cmd match {
      case x: CreateCartCommand => onCreation(x)
      case x: SelectProductCommand => selectProduct(x)
      case x: DeselectProductCommand => deselectProduct(x)
      case x: ConfirmOrderCommand => confirmOrder(x)
    }

  private def onCreation(cmd: CreateCartCommand): ReplyEffect[FoodCartEvent, FoodCartState] =
    Effect
      .persist(FoodCartCreatedEvent(cmd.foodCartId))
      .thenReply(cmd.replyTo) { _ =>
        CartCreated(cmd.foodCartId)
      }

  private def selectProduct(cmd: SelectProductCommand): ReplyEffect[FoodCartEvent, FoodCartState] =
    Effect
      .persist(ProductSelectedEvent(cmd.foodCartId, cmd.productId, cmd.quantity))
      .thenReply(cmd.replyTo) { _ =>
        Accepted
      }

  private def deselectProduct(cmd: DeselectProductCommand): ReplyEffect[FoodCartEvent, FoodCartState] =
    Effect
      .persist(ProductDeselectedEvent(cmd.foodCartId, cmd.productId, cmd.quantity))
      .thenReply(cmd.replyTo) { _ =>
        Accepted
      }

  private def confirmOrder(cmd: ConfirmOrderCommand): ReplyEffect[FoodCartEvent, FoodCartState] = {
    Effect
      .persist(OrderConfirmedEvent(cmd.foodCartId))
      .thenReply(cmd.replyTo) { _ =>
        Accepted
      }
  }

  def applyEvent(evt: FoodCartEvent): FoodCartState = {
    evt match {
      case FoodCartCreatedEvent(_) => this
      case ProductSelectedEvent(_, productId, quantity) => updateProducts(productId, quantity)
      case ProductDeselectedEvent(_, productId, quantity) => deselectProducts(productId, quantity)
      case OrderConfirmedEvent(_) => confirmOrder()
    }
  }

  private def updateProducts(productId: UUID, quantity: Int): FoodCartState = {
    val newProductsMap = products.updatedWith(productId) {
      case Some(value) => Some(value + quantity)
      case _ => Some(quantity)
    }
    copy(products = newProductsMap)
  }

  private def deselectProducts(productId: UUID, quantity: Int): FoodCartState = {
    val newProductsMap = products.updatedWith(productId) {
      case Some(value) if value - quantity > 0 => Some(value - quantity)
      case _ => Some(0)
    }
    copy(products = newProductsMap)
  }

  private def confirmOrder(): FoodCartState = {
    copy(confirmed = true)
  }
}

object FoodCartState {
  def initial: FoodCartState = FoodCartState(products = Map.empty[UUID, Int], confirmed = false)

  val typeKey = EntityTypeKey[FoodCartCommand]("FoodCartAggregate")

  implicit val mapFormat = Formats.mapFormat
  implicit val format: Format[FoodCartState] = Json.format
}

object FoodCartSerializerRegistry extends JsonSerializerRegistry {
  override def serializers: Seq[JsonSerializer[_]] = Seq(
    // state and events can use play-json, but commands should use jackson because of ActorRef[T] (see application.conf)
    JsonSerializer[FoodCartCreatedEvent],
    JsonSerializer[ProductSelectedEvent],
    JsonSerializer[ProductDeselectedEvent],
    JsonSerializer[OrderConfirmedEvent],
    JsonSerializer[FoodCartState],
    // the replies use play-json as well
    JsonSerializer[CartCreated],
    JsonSerializer[Confirmation],
    JsonSerializer[Accepted],
    JsonSerializer[Rejected]
  )
}
