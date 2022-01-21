# Multinode
Sample project to experiment with project organization, build and devops tools.

## Architecture
Project consists of two microservices:

**loader** loads users' games from Lichess API and writes them to Kafka.<br>
**api** reads games from Kafka and serves them via Grpc API

**model** contains proto models and shared code

## Features
* Multi stack build - **api** is implemented using ZIO stack, **loader** using Cats Effect.
<br>Grpc service definitions are stored in **model**, but compiled in subprojects to
allow microservice authors to choose GRPC implementation.
<br> See ProtoModule, GrpcModule, ZioGrpcModule auto plugins for implementation details.

## Run

todo


