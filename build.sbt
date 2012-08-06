sbtPlugin := true

name := "sbt-autodeps"

organization := "org.greenrd"

version := "0.1"

scalacOptions in Compile += "-deprecation"

libraryDependencies ++= Seq("commons-collections" % "commons-collections" % "4.0-SNAPSHOT",
                    "com.jsuereth" %% "scala-arm" % "1.2",
                    "javax.transaction" % "jta" % "1.1")

resolvers += "Apache Snapshots" at "https://repository.apache.org/content/repositories/snapshots/"