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

object YamlInputs {
  import com.wix.sangria.marshalling.jackson.JacksonYamlConfiguration.marshalling._

  implicit def articleFromInput = jacksonFromInput[Article]
}

class JacksonYamlSupportSpec extends WordSpec with Matchers with MarshallingBehaviour with InputHandlingBehaviour with ParsingBehaviour {

  import YamlInputs._
  import com.wix.sangria.marshalling.jackson.JacksonYamlConfiguration.marshalling._

  "jackson yaml integration" should {
    behave like `value (un)marshaller`(JacksonResultMarshaller)

    behave like `AST-based input unmarshaller`(jacksonJsonNodeFromInput[JsonNode])
    behave like `AST-based input marshaller`(JacksonResultMarshaller)

    behave like `case class input unmarshaller`
    behave like `case class input marshaller`(JacksonResultMarshaller)

    behave like `input parser` (ParseTestSubjects(
      complex = """---
                  |a:
                  |- 
                  |- 123
                  |- - foo: bar
                  |b:
                  |  c: true
                  |  d: 
                  |""".stripMargin,
      simpleString = "bar",
      simpleInt = "12345",
      simpleNull = "null",
      list = """---
               |- bar
               |- 1
               |- 
               |- true
               |- - 1
               |  - 2
               |  - 3
               |""".stripMargin,
      syntaxError = List("foo: \"an escaped \\' single quote\"")
    ))
  }
}
