{
  description = "VexRiscv build tools";

  inputs = {
    nixpkgs.url = github:NixOS/nixpkgs/nixos-unstable;
    flake-utils.url = github:numtide/flake-utils;
  };

  outputs = {
    self,
    nixpkgs,
    flake-utils,
  }:
    flake-utils.lib.eachDefaultSystem (system: let
      pkgs = import nixpkgs {inherit system;};
    in rec {
      formatter = pkgs.alejandra;

      packages.default = pkgs.writeScriptBin "build" ''
        ${pkgs.sbt}/bin/sbt "runMain ee.kivikakk.ava.GenAva"
      '';

      devShells.default = pkgs.mkShell {
        buildInputs = with pkgs; [
          sbt
          verilator
          metals
        ];
      };
    });
}
