// parent project for «ML in Scala» book
// each chapter project is still a standalone SBT project
// running sbt in this directory will compile all chapters

name := "ml-in-scala"

version := "1.0"

scalaVersion := "2.11.7"

lazy val root = project.in(file("."))
  .aggregate(
    chapter01,
    chapter02,
    chapter03,
    chapter04,
    chapter05,
    chapter06,
    chapter07,
    chapter08,
    chapter09
  )

lazy val chapter01 = ProjectRef(file("chapter01"), "chapter01")
lazy val chapter02 = ProjectRef(file("chapter02"), "chapter02")
lazy val chapter03 = ProjectRef(file("chapter03"), "chapter03")
lazy val chapter04 = ProjectRef(file("chapter04"), "chapter04")
lazy val chapter05 = ProjectRef(file("chapter05"), "chapter05")
lazy val chapter06 = ProjectRef(file("chapter06"), "chapter06")
lazy val chapter07 = ProjectRef(file("chapter07"), "chapter07")
lazy val chapter08 = ProjectRef(file("chapter08"), "chapter08")
lazy val chapter09 = ProjectRef(file("chapter09"), "chapter09")
lazy val chapter10 = ProjectRef(file("chapter10"), "chapter10")
