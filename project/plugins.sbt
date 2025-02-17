resolvers += Resolver.sonatypeRepo("releases")

addSbtPlugin("com.47deg"          % "sbt-org-policies" % "0.12.0-M3")
addSbtPlugin("com.47deg"          % "sbt-microsites"   % "0.9.7")
addSbtPlugin("com.eed3si9n"       % "sbt-buildinfo"    % "0.9.0")
addSbtPlugin("pl.project13.scala" % "sbt-jmh"          % "0.3.7")
addSbtPlugin("com.timushev.sbt"   % "sbt-updates"      % "0.5.0")
addSbtPlugin("org.scoverage"      % "sbt-scoverage"    % "1.6.0")

libraryDependencies += "org.scala-sbt" %% "scripted-plugin" % sbtVersion.value
