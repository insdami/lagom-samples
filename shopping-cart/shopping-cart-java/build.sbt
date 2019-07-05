import com.lightbend.lagom.core.LagomVersion

organization in ThisBuild := "com.example"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.8"

// Update the version generated by sbt-dynver to remove any + characters, since these are illegal in docker tags
version in ThisBuild ~= (_.replace('+', '-'))
dynver in ThisBuild ~= (_.replace('+', '-'))

lazy val `shopping-cart-java` = (project in file("."))
  .aggregate(`shopping-cart-api`, `shopping-cart`, `inventory-api`, inventory)

lazy val `shopping-cart-api` = (project in file("shopping-cart-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi,
      lombok
    )
  )

lazy val `shopping-cart` = (project in file("shopping-cart"))
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(dockerSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslPersistenceJdbc,
      lagomJavadslKafkaBroker,
      lagomLogback,
      lagomJavadslTestKit,
      lombok,
      postgresDriver,
      hamcrestLibrary,
      lagomJavadslAkkaDiscovery,
      akkaDiscoveryKubernetesApi
    )
  )
  .settings(lagomForkedTestSettings: _*)
  .dependsOn(`shopping-cart-api`)

lazy val `inventory-api` = (project in file("inventory-api"))
  .settings(common)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslApi
    )
  )

lazy val inventory = (project in file("inventory"))
  .enablePlugins(LagomJava)
  .settings(common)
  .settings(dockerSettings)
  .settings(
    libraryDependencies ++= Seq(
      lagomJavadslKafkaClient,
      lagomLogback,
      lagomJavadslTestKit,
      lagomJavadslAkkaDiscovery
    )
  )
  .dependsOn(`shopping-cart-api`, `inventory-api`)

val lombok = "org.projectlombok" % "lombok" % "1.18.6"
val postgresDriver = "org.postgresql" % "postgresql" % "42.2.5"
val hamcrestLibrary = "org.hamcrest" % "hamcrest-library" % "2.1" % Test

val akkaManagementVersion = "1.0.0"
val akkaDiscoveryKubernetesApi = "com.lightbend.akka.discovery" %% "akka-discovery-kubernetes-api" % akkaManagementVersion
val lagomJavadslAkkaDiscovery = "com.lightbend.lagom" %% "lagom-javadsl-akka-discovery-service-locator" % LagomVersion.current

def common = Seq(
  javacOptions in Compile := Seq("-g", "-encoding", "UTF-8", "-Xlint:unchecked", "-Xlint:deprecation", "-parameters", "-Werror")
)

def dockerSettings = Seq(
  dockerUpdateLatest := true,
  dockerBaseImage := getDockerBaseImage(),
  dockerUsername := sys.props.get("docker.username"),
  dockerRepository := sys.props.get("docker.registry")
)

def getDockerBaseImage(): String = sys.props.get("java.version") match {
  case Some(v) if v.startsWith("11") => "adoptopenjdk/openjdk11"
  case _ => "adoptopenjdk/openjdk8"
}

lagomCassandraEnabled in ThisBuild := false
