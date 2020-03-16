package be.reaktika.foodordering.api

import java.util.UUID

import com.lightbend.lagom.scaladsl.persistence.{AggregateEvent, AggregateEventTag}
import play.api.libs.json.{Format, Json}

object Events {

  sealed trait FoodCartEvent extends AggregateEvent[FoodCartEvent] {
    override def aggregateTag: AggregateEventTag[FoodCartEvent] = FoodCartEvent.Tag
  }
  object FoodCartEvent {
    val Tag: AggregateEventTag[FoodCartEvent] = AggregateEventTag[FoodCartEvent]
  }

  case class FoodCartCreatedEvent(foodCartId: UUID) extends FoodCartEvent
  object FoodCartCreatedEvent {
    implicit val format: Format[FoodCartCreatedEvent] = Json.format
  }

  case class ProductSelectedEvent(foodCartId: UUID, productId: UUID, quantity: Int) extends FoodCartEvent
  object ProductSelectedEvent {
    implicit val format: Format[ProductSelectedEvent] = Json.format
  }

  case class ProductDeselectedEvent(foodCartId: UUID, productId: UUID, quantity: Int) extends FoodCartEvent
  object ProductDeselectedEvent {
    implicit val format: Format[ProductDeselectedEvent] = Json.format
  }

  case class OrderConfirmedEvent(foodCartId: UUID) extends FoodCartEvent
  object OrderConfirmedEvent {
    implicit val format: Format[OrderConfirmedEvent] = Json.format
  }
}
