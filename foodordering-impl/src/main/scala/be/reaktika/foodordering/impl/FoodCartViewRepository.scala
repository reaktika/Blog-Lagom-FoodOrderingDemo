package be.reaktika.foodordering.impl

import java.util.UUID

import akka.Done
import akka.persistence.query.{NoOffset, Offset}
import be.reaktika.foodordering.api.Events._
import be.reaktika.foodordering.api.FoodCartView
import com.lightbend.lagom.scaladsl.persistence.AggregateEventTag

import scala.concurrent.Future

object FoodCartViewRepository {

  private var catalog = Map.empty[UUID, FoodCartView]

  def createTables(): Future[Done] =
    Future.successful(Done)

  def loadOffset(tag: AggregateEventTag[FoodCartEvent]): Future[Offset] =
    Future.successful(NoOffset)

  def handleEvent(event: FoodCartEvent, offset: Offset): Future[Done] = {
    event match {
      case FoodCartCreatedEvent(foodCartId) =>
        catalog = catalog + (foodCartId -> FoodCartView(foodCartId))
      case ProductSelectedEvent(foodCartId, productId, quantity) => {
        catalog = catalog.updatedWith(foodCartId){
          case Some(foodCartView) => Some(foodCartView.addProducts(productId, quantity))
          case None => None
        }
      }
      case ProductDeselectedEvent(foodCartId, productId, quantity) =>
        catalog = catalog.updatedWith(foodCartId){
          case Some(foodCartView) => Some(foodCartView.removeProducts(productId, quantity))
          case None => None
        }
      case OrderConfirmedEvent(foodCartId) =>
        catalog = catalog.updatedWith(foodCartId){
          case Some(foodCartView) => Some(foodCartView.confirmOrder())
          case None => None
        }
    }
    Future.successful(Done)
  }

  def getCatalog(foodCartId: UUID): Option[FoodCartView] = {
    catalog.get(foodCartId)
  }
}
