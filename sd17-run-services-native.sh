#!/bin/bash

#update the images, in particular the client
docker pull smduarte/sd17-services:latest

#execute the servers each in its container
docker run -v /var/run/docker.sock:/var/run/docker.sock smduarte/sd17-services:latest