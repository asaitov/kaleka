import Dependencies._

name := "kaleka"

version := "0.0.1-SNAPSHOT"

scalaVersion := "2.13.16"

libraryDependencies ++= akkaLibs ++ logLibs ++ parserLibs ++ configLibs ++ testLibs
