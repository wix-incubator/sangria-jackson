[Sangria](http://sangria-graphql.org/) [jackson](https://github.com/FasterXML/jackson) marshalling.



[![Build Status](https://travis-ci.org/wix-incubator/sangria-jackson.svg?branch=master)](https://travis-ci.org/wix-incubator/sangria-jackson)

SBT Configuration:

```scala
libraryDependencies += "com.wix" %% "sangria-jackson" % "0.1.0"
```

## Usage

Create marshalling support with your instance of `ObjectMapper`
 
```scala
package com.example

object Jackson {
  val marshalling = com.wix.sangria.marshalling.jackson.forObjectMapper(objectMapper)
}
```

Import `marshalling._` where you need to use it. `ToInput` instances are 
automatically derived for all types. `FromInput` instances have to be 
declared manually:

```scala
package com.example

import marshalling._

object FromInputs {
  implicit def articleFromInput = jacksonFromInput[Article]
}
```

> Note: It's up to you to make sure that the object mapper can serialize 
and deserialize values in the format defined in the graphql schema. This 
means that you probably need to use the [jackson-scala-module][https://github.com/FasterXML/jackson-module-scala] 
and annotate enum values inside case classes with [@JsonScalaEnumeration][https://github.com/FasterXML/jackson-module-scala/wiki/Enumerations]
annotations. 

## License

**sangria-json4s-jackson** is licensed under [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
