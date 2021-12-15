{ jdk }:

{
  packageOverrides = p: {
    coursier = p.coursier.override { jre = p.${jdk}; };
    sbt = p.sbt.override { jre = p.${jdk}; };
    bloop = p.bloop.override { jre = p.${jdk}; };
  };
}
