{ jdk }:

let
  pinned = import ./pinned.nix;
  config = import ./config.nix { inherit jdk; };
  overlays = [
    (import ./overlays/sbt.nix)
  ];
  pkgs = import pinned.nixpkgs { inherit config; inherit overlays; };
in
  pkgs
