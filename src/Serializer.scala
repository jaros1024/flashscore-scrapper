import com.google.gson.{Gson, GsonBuilder, JsonElement, JsonSerializationContext, JsonSerializer}
import data.Match
import java.lang.reflect.Type

import scala.collection.mutable.ListBuffer

object Serializer {
  lazy val gson: Gson = new GsonBuilder()
    .registerTypeHierarchyAdapter(classOf[ListBuffer[Match]], new MatchListSerializer)
    .create()

  class MatchListSerializer extends JsonSerializer[ListBuffer[Match]] {
    override def serialize(src: ListBuffer[Match], typeOfSrc: Type, context: JsonSerializationContext): JsonElement = {
      import scala.collection.JavaConverters._
      context.serialize(src.toList.asJava)
    }
  }
}
