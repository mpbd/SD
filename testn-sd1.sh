#!/bin/bash
if [  $# -le 1 ] 
then 
		echo "usage: $0 -image <img> [ -test <num> ] [ -log OFF|ALL|FINE ]"
		exit 1
fi 

LOGS=$(pwd)/logs/
mkdir -p $LOGS

#update the images, in particular the client
docker pull smduarte/sd17-client:latest
docker pull $2

#execute the client with the given command line parameters
docker run -it -v $LOGS:/logs/ -v /var/run/docker.sock:/var/run/docker.sock smduarte/sd17-client:latest $*

#pull the logs to the host
echo "Container logs:" $LOGS
