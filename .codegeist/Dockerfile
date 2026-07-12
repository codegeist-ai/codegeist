# Dockerfile - project-local devcontainer extension for GPU tooling
#
# Why this exists:
# - `.devcontainer/initialize.sh` appends this file to the shared kit Dockerfile
#   when it writes `.devcontainer/Dockerfile.merged.gen`.
# - The shared kit owns generic development tools, including terminal-capture
#   rendering. This local extension adds NVIDIA user-space and container-runtime
#   packages so Docker GPU workloads such as Ollama can run from the inner daemon.
# - The NVIDIA user-space driver package version should match the host driver;
#   the kernel driver still runs on the host and GPU devices are exposed at
#   runtime through `.codegeist/compose.local.yml`.
#
# Inputs:
# - NVIDIA_DRIVER_VERSION defaults to the currently validated host driver package
#   version for this workstation.
# - NVIDIA_CONTAINER_TOOLKIT_VERSION selects the inner Docker GPU runtime support.
#
# Related files:
# - .codegeist/compose.local.yml
# - .devcontainer/README.md
# - .devcontainer/initialize.sh

USER root

ARG NVIDIA_DRIVER_VERSION=595.71.05-1
ARG NVIDIA_CONTAINER_TOOLKIT_VERSION=1.19.1-1

RUN set -eux; \
    curl -fsSL \
      https://developer.download.nvidia.com/compute/cuda/repos/debian12/x86_64/cuda-keyring_1.1-1_all.deb \
      -o /tmp/cuda-keyring.deb; \
    dpkg -i /tmp/cuda-keyring.deb; \
    rm -f /tmp/cuda-keyring.deb; \
    install -m 0755 -d /etc/apt/keyrings; \
    curl -fsSL https://nvidia.github.io/libnvidia-container/gpgkey \
      | gpg --dearmor -o /etc/apt/keyrings/nvidia-container-toolkit.gpg; \
    chmod a+r /etc/apt/keyrings/nvidia-container-toolkit.gpg; \
    printf '%s\n' \
      'deb [signed-by=/etc/apt/keyrings/nvidia-container-toolkit.gpg] https://nvidia.github.io/libnvidia-container/stable/deb/amd64 /' \
      > /etc/apt/sources.list.d/nvidia-container-toolkit.list; \
    apt-get update; \
    apt-get install -y --no-install-recommends \
      "nvidia-smi=${NVIDIA_DRIVER_VERSION}" \
      "libnvidia-gpucomp=${NVIDIA_DRIVER_VERSION}" \
      "libnvidia-ml1=${NVIDIA_DRIVER_VERSION}" \
      "libnvidia-pkcs11-openssl3=${NVIDIA_DRIVER_VERSION}" \
      "libnvidia-ptxjitcompiler1=${NVIDIA_DRIVER_VERSION}" \
      "libcuda1=${NVIDIA_DRIVER_VERSION}" \
      "nvidia-container-toolkit=${NVIDIA_CONTAINER_TOOLKIT_VERSION}"; \
    nvidia-ctk runtime configure --runtime=docker; \
    rm -rf /var/lib/apt/lists/*
