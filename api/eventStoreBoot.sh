docker run --name esdb-node \
-it --network starbornag-network -p 2113:2113 \
-p 1113:1113 eventstore/eventstore:23.10.2-alpha-arm64v8 \
--insecure \
--run-projections=All \
--enable-atompub-over-http=true
