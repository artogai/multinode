{
  nixpkgs =
    let
      commit = "b0bf5f888d377dd2f36d90340df6dc9f035aaada";
      checksum = "0l123s468r9h706grqkzf0x077r4hy6zcz529xxfiqxsh1ddx5xb";
    in
      builtins.fetchTarball {
        url = "https://github.com/NixOS/nixpkgs/archive/${commit}.tar.gz";
        sha256 = checksum;
      };
}
