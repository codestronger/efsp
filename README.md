# EfileProxyServer

A separate server that sits between ECF 4.0 EFMs and docassemble, and can create court filings.

TODO(brycew): more details to come!

## Setup

This server is setup using docker. To run, follow the following steps:

These instructions are written for Linux (specifically Ubuntu 20.04), steps for other platforms should be similar.

1. [Install docker](https://docs.docker.com/engine/install/).
2. Download this repository: In a terminal, you can run `git clone https://github.com/SuffolkLITLab/EfileProxyServer`
3. In the same terminal change directories to where you downloaded the server: `cd EfileProxyServer`, and then build a docker image for this server. `docker build . -t efspjava`
4. Run the server image that you just built with `docker run efspjava`.