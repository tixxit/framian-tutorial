name := "framian-tutorial"

scalaVersion := "2.11.2"

resolvers += "Pellucid Bintray" at "http://dl.bintray.com/pellucid/maven"

libraryDependencies += "com.pellucid" %% "framian" % "0.3.1"

initialCommands := """
  |import framian._
  |import framian.csv.Csv
  |import framian.tutorial._
  |import spire.implicits._
""".stripMargin('|')
