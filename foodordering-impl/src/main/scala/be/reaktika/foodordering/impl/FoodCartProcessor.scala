package be.reaktika.foodordering.impl

import akka.persistence.query.Offset
import akka.{Done, NotUsed}
import akka.stream.scaladsl.Flow
import be.reaktika.foodordering.api.Events.FoodCartEvent
import com.lightbend.lagom.scaladsl.persistence.ReadSideProcessor.ReadSideHandler
import com.lightbend.lagom.scaladsl.persistence.{AggregateEventTag, EventStreamElement, ReadSideProcessor}

import scala.concurrent.{ExecutionContext, Future}

class FoodCartProcessor()(implicit ec: ExecutionContext)
  extends ReadSideProcessor[FoodCartEvent]{

  override def buildHandler(): ReadSideProcessor.ReadSideHandler[FoodCartEvent] = {
    new ReadSideHandler[FoodCartEvent] {
      override def globalPrepare(): Future[Done] =
        FoodCartViewRepository.createTables()

      override def prepare(tag: AggregateEventTag[FoodCartEvent]): Future[Offset] =
        FoodCartViewRepository.loadOffset(tag)

      override def handle(): Flow[EventStreamElement[FoodCartEvent], Done, NotUsed] =
        Flow[EventStreamElement[FoodCartEvent]]
          .mapAsync(1) { eventElement =>
            FoodCartViewRepository.handleEvent(eventElement.event, eventElement.offset)
          }
    }
  }

  override def aggregateTags: Set[AggregateEventTag[FoodCartEvent]] =
    Set(FoodCartEvent.Tag)
}
