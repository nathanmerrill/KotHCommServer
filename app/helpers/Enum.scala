package helpers

import io.ebean.annotation.EnumValue
import play.api.data.format.Formatter
import play.api.data.{FormError, Forms, Mapping}

object Enum {

  def enumContains[T <: Enum[T]](enumType: Class[T]): Mapping[T] = Forms.of[T](enumBinder(enumType))

  def enumBinder[T <: Enum[T]](enumType: Class[T]): Formatter[T] = new Formatter[T] {

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], T] = {
      data.get(key) match {
        case None => Left(Seq(FormError("error.invalidEnum", "Value required", Nil)))
        case Some(value) =>
          enumType.getEnumConstants.find { c =>
            value.equals(getEnumValue(enumType, c))
          } match {
            case Some(f) => Right(f)
            case None => Left(Seq(FormError("error.invalidEnum", "Invalid value for " + enumType.getSimpleName + ": " + value, Nil)))
          }
      }
    }

    override def unbind(key: String, value: T): Map[String, String] = {
      Map(key -> getEnumValue(enumType, value))
    }
  }


  def getEnumValue[T <: Enum[T]](enumType: Class[T], value: Any): String = {
    val name = value.toString
    enumType.getField(name).getAnnotation(classOf[EnumValue]) match {
      case null => name
      case p => p.value()
    }
  }

  def enumValues[T <: Enum[T]](enumType: Class[T]): Seq[(String, String)] = {
    enumType.getFields
      .map(field => {
        val value = getEnumValue(enumType, field.getName)
        (value, value)
      })
  }
}
