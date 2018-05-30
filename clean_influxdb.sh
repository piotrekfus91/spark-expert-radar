#!/bin/bash

docker exec influxdb influx --execute 'show measurements' --database=serdb | xargs -I{} docker exec influxdb influx --database=serdb --execute 'drop measurement "{}"'
