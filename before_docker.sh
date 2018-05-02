#!/bin/bash
###################
# This script should be run if Elasticsearch doesn't start
###################
sudo sysctl -w vm.max_map_count=262144
