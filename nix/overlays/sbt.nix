self: super: {
  sbt = ( let version = "1.6.0"; in
    super.sbt.overrideAttrs (oldAttrs: {
      src = super.fetchurl {
        url = "https://github.com/sbt/sbt/releases/download/v${version}/sbt-${version}.tgz";
        sha256 = "Y52xcPZRDQ+IX/QAVGRODshbuDb5oJA45r9OT2Jxusw=";
      };
    })
  );
}
