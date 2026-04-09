FROM ubuntu:22.04

ENV container=docker
ENV DEBIAN_FRONTEND=noninteractive

# Install systemd and tooling needed to install/debug the .deb package.
RUN apt-get update && apt-get install -y --no-install-recommends \
    systemd \
    systemd-sysv \
    dbus \
    ca-certificates \
    apt-utils \
    sudo \
    procps \
    iproute2 \
    iputils-ping \
    less \
    vim-tiny \
    && apt-get clean \
    && rm -rf /var/lib/apt/lists/*

VOLUME ["/sys/fs/cgroup"]

STOPSIGNAL SIGRTMIN+3
CMD ["/sbin/init"]
