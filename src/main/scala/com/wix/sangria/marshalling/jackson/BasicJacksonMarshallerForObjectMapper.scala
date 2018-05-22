/*
 *    Copyright 2018 Wix.com
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 *
 */

package com.wix.sangria.marshalling.jackson

import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.databind.{JsonMappingException, JsonNode, ObjectMapper}
import sangria.marshalling._

import scala.collection.JavaConverters._
import scala.util.Try

class BasicJacksonMarshallerForObjectMapper(objectMapper: ObjectMapper) {
  private val nodeFactory = objectMapper.getNodeFactory
  private val defaultWriter = objectMapper.writer
  private val prettyPrintingWriter = objectMapper.writerWithDefaultPrettyPrinter()

  implicit object JacksonResultMarshaller extends ResultMarshaller {
    override type Node = JsonNode
    type MapBuilder = ObjectNode

    override def emptyMapNode(keys: Seq[String]) = objectMapper.createObjectNode()

    override def addMapNodeElem(builder: MapBuilder, key: String, value: Node, optional: Boolean) = {
      builder.set(key, value)
      builder
    }

    override def mapNode(builder: MapBuilder) = builder

    override def mapNode(keyValues: Seq[(String, Node)]) = {
      objectMapper.createObjectNode().setAll(keyValues.toMap.asJava)
    }

    override def arrayNode(values: Vector[Node]) = {
      objectMapper.createArrayNode().addAll(values.asJava)
    }

    override def optionalArrayNodeValue(value: Option[Node]) = value match {
      case Some(v) => v
      case None => nullNode
    }

    override def scalarNode(value: Any, typeName: String, info: Set[ScalarValueInfo]) = value match {
      case v: String => nodeFactory.textNode(v)
      case v: Boolean => nodeFactory.booleanNode(v)
      case v: Int => nodeFactory.numberNode(v)
      case v: Long => nodeFactory.numberNode(v)
      case v: Float => nodeFactory.numberNode(v)
      case v: Double => nodeFactory.numberNode(v)
      case v: BigInt => nodeFactory.numberNode(v.underlying)
      case v: BigDecimal => nodeFactory.numberNode(v.underlying)
      case v => throw new IllegalArgumentException("Unsupported scalar value: " + v)
    }

    override def enumNode(value: String, typeName: String) = nodeFactory.textNode(value)

    override def nullNode = objectMapper.getNodeFactory.nullNode()

    override def renderCompact(node: Node) = {
      defaultWriter.writeValueAsString(node)
    }

    override def renderPretty(node: Node) = prettyPrintingWriter.writeValueAsString(node)
  }

  implicit object JacksonMarshallerForType extends ResultMarshallerForType[JsonNode] {
    val marshaller = JacksonResultMarshaller
  }

  implicit object JacksonInputUnmarshaller extends InputUnmarshaller[JsonNode] {
    override def getRootMapValue(node: JsonNode, key: String) = Option(node.findValue(key))

    override def isMapNode(node: JsonNode) = node.isObject

    override def getMapValue(node: JsonNode, key: String) = getRootMapValue(node, key)

    override def getMapKeys(node: JsonNode) = node.fieldNames().asScala.toSeq

    override def isListNode(node: JsonNode) = node.isArray

    override def getListValue(node: JsonNode) = node.elements().asScala.toSeq

    override def isDefined(node: JsonNode) = node != null && !node.isNull

    override def getScalarValue(node: JsonNode) = node match {
      case s if s.isTextual => s.textValue
      case s if s.isBoolean => s.booleanValue
      case s if (s.isInt || s.isLong || s.isBigInteger) && s.canConvertToInt => s.intValue()
      case s if s.isBigDecimal => BigDecimal(s.decimalValue())
      case s if s.isNumber => s.doubleValue()
      case _ => throw new IllegalStateException(s"$node is not a scalar value")
    }

    override def getScalaScalarValue(node: JsonNode) = node match {
      case s if s.isTextual => s.textValue
      case s if s.isBoolean => s.booleanValue
      case s if s.isInt => s.intValue
      case s if s.isLong => s.longValue
      case s if s.isFloat => s.floatValue
      case s if s.isDouble => s.doubleValue
      case s if s.isBigInteger => BigInt(s.bigIntegerValue())
      case s if s.isBigDecimal => BigDecimal(s.decimalValue())
      case _ => throw new IllegalStateException(s"$node is not a scalar value")
    }

    override def isEnumNode(node: JsonNode) = node.isTextual

    override def isScalarNode(node: JsonNode) = isDefined(node) && node.isValueNode

    override def isVariableNode(node: JsonNode) = false

    override def getVariableName(node: JsonNode) = throw new IllegalArgumentException("variables are not supported")

    override def render(node: JsonNode) = defaultWriter.writeValueAsString(node)
  }

  class JacksonToInput[T] extends ToInput[T, JsonNode] {
    override def toInput(value: T): (JsonNode, InputUnmarshaller[JsonNode]) = (objectMapper.valueToTree(value), JacksonInputUnmarshaller)
  }

  private object JacksonJsonNodeToInput extends ToInput[JsonNode, JsonNode] {
    override def toInput(value: JsonNode): (JsonNode, InputUnmarshaller[JsonNode]) = (value, JacksonInputUnmarshaller)
  }

  class JacksonFromInput[T: Manifest] extends FromInput[T] {
    override val marshaller = JacksonResultMarshaller

    override def fromResult(node: JsonNode) = fromTree(node)
  }

  implicit def jacksonJsonNodeToInput[T <: JsonNode]: ToInput[T, JsonNode] =
    JacksonJsonNodeToInput.asInstanceOf[ToInput[T, JsonNode]]

  private object JacksonJsonNodeFromInput extends FromInput[JsonNode] {
    override val marshaller = JacksonResultMarshaller

    override def fromResult(node: JsonNode): JsonNode = node
  }

  def jacksonToInput[T]: ToInput[T, JsonNode] =
    new JacksonToInput[T]

  implicit def jacksonJsonNodeFromInput[T <: JsonNode]: FromInput[T] =
    JacksonJsonNodeFromInput.asInstanceOf[FromInput[T]]


  private def validate(isValid: => Boolean, validationMessage: => String): Unit = {
    if (!isValid) throw InputParsingError(Vector(validationMessage))
  }

  private def fromTree[T](node: JsonNode)(implicit mf: Manifest[T]) = {
    try {
      objectMapper.treeToValue(node, mf.runtimeClass).asInstanceOf[T]
    } catch {
      case e: JsonMappingException => throw InputParsingError(Vector(s"Failed to deserialize an instance of ${mf.toString}: ${e.getMessage}"))
    }
  }

  def jacksonFromInput[T: Manifest]: FromInput[T] =
    new JacksonFromInput[T]

  implicit object JacksonInputParser extends InputParser[JsonNode] {
    override def parse(str: String) = Try(objectMapper.readTree(str))
  }


}

trait ToInputDerivation {
  self: BasicJacksonMarshallerForObjectMapper =>

  implicit def deriveJacksonToInput[T]: ToInput[T, JsonNode] =
    self.jacksonToInput[T]

}
