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

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.node._
import org.scalatest.{Matchers, WordSpec}
import sangria.marshalling.FromInput
import sangria.marshalling.testkit._

object Inputs {
  import com.wix.sangria.marshalling.jackson.JacksonConfiguration.marshalling._

  implicit def articleFromInput = jacksonFromInput[Article]
}

class JacksonSupportSpec extends WordSpec with Matchers with MarshallingBehaviour with InputHandlingBehaviour with ParsingBehaviour {

  import Inputs._
  import com.wix.sangria.marshalling.jackson.JacksonConfiguration.marshalling._

  "jackson integration" should {
    behave like `value (un)marshaller`(JacksonResultMarshaller)

    behave like `AST-based input unmarshaller`(jacksonJsonNodeFromInput[JsonNode])
    behave like `AST-based input marshaller`(JacksonResultMarshaller)

    behave like `case class input unmarshaller`
    behave like `case class input marshaller`(JacksonResultMarshaller)

    behave like `input parser` (ParseTestSubjects(
      complex = """{"a": [null, 123, [{"foo": "bar"}]], "b": {"c": true, "d": null}}""",
      simpleString = "\"bar\"",
      simpleInt = "12345",
      simpleNull = "null",
      list = "[\"bar\", 1, null, true, [1, 2, 3]]",
      syntaxError = List("[123, \"FOO\" \"BAR\"")
    ))
  }

  val compactJson = 
    """{"a":null,"b":[{"c":1234}]}""".stripMargin
  
  val prettyJson =
    """{
      |  "a" : null,
      |  "b" : [ {
      |    "c" : 1234
      |  } ]
      |}""".stripMargin
  
  val parsedJson = JacksonInputParser.parse(compactJson).get
  
  "JacksonInputUnmarshaller" should {
    "throw on invalid scalar values" in {
      assertThrows[IllegalStateException](JacksonInputUnmarshaller.getScalarValue(new ObjectNode(null)))
    }

    "throw on attempt to get variable names" in {
      assertThrows[IllegalArgumentException](JacksonInputUnmarshaller.getVariableName(new TextNode("$foo")))
    }

    "render compactly" in {
      val rendered: String = JacksonInputUnmarshaller.render(parsedJson)

      rendered should be (compactJson)
    }
  }

  "JacksonResultMarshaller" should {
    "render pretty" in {
      val rendered: String = JacksonResultMarshaller.renderPretty(parsedJson)

      rendered should be (prettyJson)     
    }

    "render compact" in {
      val rendered: String = JacksonResultMarshaller.renderCompact(parsedJson)

      rendered should be (compactJson)
    }
  }
}
