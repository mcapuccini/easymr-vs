FROM ubuntu:14.04
MAINTAINER marco.capuccini@it.uu.se

# Install OEDocking
COPY OEDocking-3.2.0.2-Ubuntu-14.04-x64.tar.gz /
RUN tar xzvf /OEDocking-3.2.0.2-Ubuntu-14.04-x64.tar.gz
RUN ln -s /openeye/bin/* /bin
RUN rm /OEDocking-3.2.0.2-Ubuntu-14.04-x64.tar.gz

# Install SSH (to make OEDocking happy)
RUN apt-get update && apt-get install -y openssh-client

# Add license
RUN mkdir /var/openeye
ADD oe_license.txt /var/openeye
ENV OE_LICENSE /var/openeye/oe_license.txt

# Add receptor
ADD hiv1_protease.oeb /var/openeye
