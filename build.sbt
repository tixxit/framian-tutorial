name := "framian-tutorial"

scalaVersion := "2.11.2"

resolvers += "Pellucid Bintray" at "http://dl.bintray.com/pellucid/maven"

libraryDependencies ++= Seq(
  "com.pellucid" %% "framian" % "0.3.1",
  "joda-time" % "joda-time" % "2.3",
  "org.joda" % "joda-convert" % "1.5"
)

initialCommands := """
  |import framian._
  |import framian.csv.Csv
  |import framian.tutorial._
  |import spire.implicits._
  |import org.joda.time.LocalDate
""".stripMargin('|')
