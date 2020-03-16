package be.reaktika.foodordering.api

import java.util.UUID

import akka.actor.typed.ActorRef
import play.api.libs.json.{Format, JsResult, JsSuccess, JsValue, Json, Reads, Writes}

object Commands {

  sealed trait FoodCartCommand
    extends FoodCartCommandSerializable

  case class CreateCartCommand(foodCartId: UUID, replyTo: ActorRef[CartCreated])
    extends FoodCartCommand

  case class SelectProductCommand(foodCartId: UUID, productId: UUID, quantity: Int, replyTo: ActorRef[Confirmation])
    extends FoodCartCommand

  case class DeselectProductCommand(foodCartId: UUID, productId: UUID, quantity: Int, replyTo: ActorRef[Confirmation])
    extends FoodCartCommand

  case class ConfirmOrderCommand(foodCartId: UUID, replyTo: ActorRef[Confirmation])
    extends FoodCartCommand

  /*
   * Return types for the commands
   *
   */
  final case class CartCreated(id: UUID)
  object CartCreated {
    implicit val format: Format[CartCreated] = Json.format
  }

  sealed trait Confirmation

  case object Confirmation {
    implicit val format: Format[Confirmation] = new Format[Confirmation] {
      override def reads(json: JsValue): JsResult[Confirmation] = {
        if ((json \ "reason").isDefined)
          Json.fromJson[Rejected](json)
        else
          Json.fromJson[Accepted](json)
      }

      override def writes(o: Confirmation): JsValue = {
        o match {
          case acc: Accepted => Json.toJson(acc)
          case rej: Rejected => Json.toJson(rej)
        }
      }
    }
  }

  sealed trait Accepted extends Confirmation

  case object Accepted extends Accepted {
    implicit val format: Format[Accepted] =
      Format(Reads(_ => JsSuccess(Accepted)), Writes(_ => Json.obj()))
  }

  case class Rejected(reason: String) extends Confirmation

  object Rejected {
    implicit val format: Format[Rejected] = Json.format
  }

}
