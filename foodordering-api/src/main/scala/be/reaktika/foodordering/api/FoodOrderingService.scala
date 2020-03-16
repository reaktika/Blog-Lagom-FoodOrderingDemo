package be.reaktika.foodordering.api

import java.util.UUID

import akka.{Done, NotUsed}
import com.lightbend.lagom.scaladsl.api.{Descriptor, Service, ServiceCall}
import play.api.libs.json.{Format, Json}

object FoodOrderingService  {
  val TOPIC_NAME = "orders"
}

trait FoodOrderingService extends Service {

  def createCart(): ServiceCall[NotUsed, UUID]

  def selectItem(foodCartId: UUID, productId: UUID, quantity: Int): ServiceCall[NotUsed, Done]

  def deselectItem(foodCartId: UUID, productId: UUID, quantity: Int): ServiceCall[NotUsed, Done]

  def confirmCart(foodCartId: UUID): ServiceCall[NotUsed, Done]

  def getFoodCartItems(foodCartId: UUID): ServiceCall[NotUsed, FoodCartViewResult]

  override final def descriptor: Descriptor = {
    import Service._
    named("food-ordering")
      .withCalls(
        pathCall("/foodCart/create", createCart _),
        pathCall("/foodCart/:foodCartId/select/:productId/quantity/:quantity", selectItem _),
        pathCall("/foodCart/:foodCartId/deselect/:productId/quantity/:quantity", deselectItem _),
        pathCall("/foodCart/:foodCartId/confirm", confirmCart _),
        pathCall("/foodCart/:foodCartId", getFoodCartItems _)
      )
      .withAutoAcl(true)
  }
}

case class FoodCartViewResult(content: Option[FoodCartView])
object FoodCartViewResult {
  implicit val format: Format[FoodCartViewResult] = Json.format
}

case class FoodCartView(foodCartId: UUID, products: Map[UUID, Int] = Map.empty, confirmed: Boolean = false) {
  def addProducts(productId: UUID, amount: Int): FoodCartView = {
    products.get(productId) match {
      case Some(quantity) =>
        copy(products = products + (productId -> (quantity + amount)))
      case None =>
        copy(products = products + (productId -> amount))
    }
  }

  def removeProducts(productId: UUID, amount: Int): FoodCartView = {
    products.get(productId) match {
      case Some(quantity) if quantity - amount > 0 =>
        copy(products = products + (productId -> (quantity - amount)))
      case _ =>
        copy(products = products.removed(productId))
    }
  }

  def confirmOrder(): FoodCartView = {
    copy(confirmed = true)
  }
}
object FoodCartView {
  implicit val mapFormat: Format[Map[UUID, Int]] = Formats.mapFormat
  implicit val format: Format[FoodCartView] = Json.format
}
