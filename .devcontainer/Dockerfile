# [Choice] Java version: 11, 15
ARG VARIANT=11
# Use standard Microsoft devcontainer base image with Java
FROM mcr.microsoft.com/devcontainers/java:${VARIANT}

# [Option] Install Maven
ARG INSTALL_MAVEN="false"
ARG MAVEN_VERSION=""
# [Option] Install Gradle
ARG INSTALL_GRADLE="false"
ARG GRADLE_VERSION=""
RUN if [ "${INSTALL_MAVEN}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install maven \"${MAVEN_VERSION}\""; fi \
    && if [ "${INSTALL_GRADLE}" = "true" ]; then su vscode -c "umask 0002 && . /usr/local/sdkman/bin/sdkman-init.sh && sdk install gradle \"${GRADLE_VERSION}\""; fi

# [Option] Install Node.js
# Default to Node 18 LTS; set INSTALL_NODE to "false" to skip during build.
ARG INSTALL_NODE="true"
ARG NODE_VERSION="20.x"
RUN if [ "${INSTALL_NODE}" = "true" ]; then \
    export DEBIAN_FRONTEND=noninteractive && \
    apt-get update && apt-get install -y --no-install-recommends curl ca-certificates gnupg dirmngr && \
    curl -fsSL "https://deb.nodesource.com/setup_${NODE_VERSION}" | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    # Install npx for compatibility (install under /usr/local for global access)
    npm install -g npx@latest || true && \
    # verify installation
    node --version && npm --version && (command -v npx || echo "npx not found") || echo "node installation failed" && \
    apt-get clean && rm -rf /var/lib/apt/lists/*; \
    fi

# [Option] Install sbt
# Install sbt using the official Debian repository method
ARG INSTALL_SBT="true"
RUN if [ "${INSTALL_SBT}" = "true" ]; then \
    curl -fsSL "https://keyserver.ubuntu.com/pks/lookup?op=get&search=0x2EE0EA64E40A89B84B2DF73499E82A75642AC823" | gpg --dearmor -o /usr/share/keyrings/sbt-archive-keyring.gpg && \
    echo "deb [signed-by=/usr/share/keyrings/sbt-archive-keyring.gpg] https://repo.scala-sbt.org/scalasbt/debian all main" > /etc/apt/sources.list.d/sbt.list && \
    apt-get update && \
    apt-get install -y sbt && \
    which sbt && sbt --version || echo "sbt installation failed"; \
    fi

# [Optional] Uncomment this section to install additional OS packages.
# RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
#     && apt-get -y install --no-install-recommends <your-package-list-here>

# [Optional] Uncomment this line to install global node packages.
# RUN su vscode -c "source /usr/local/share/nvm/nvm.sh && npm install -g <your-package-here>" 2>&1
