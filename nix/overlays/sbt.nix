self: super: {
  sbt = ( let version = "1.5.6"; in
    super.sbt.overrideAttrs (oldAttrs: {
      src = super.fetchurl {
        url = "https://github.com/sbt/sbt/releases/download/v${version}/sbt-${version}.tgz";
        sha256 = "PC0ndJkv4nNIdZMCtZhi3XRp/dRlXXx1yHvK8bAwIGg=";
      };
    })
  );
}
