FROM maven:3.9.11-eclipse-temurin-21 AS java
RUN apt-get update && apt-get install -y --no-install-recommends python3 patch curl git unzip \
    && rm -rf /var/lib/apt/lists/* \
    && mkdir -p /maven/repository && chmod 1777 /maven/repository
WORKDIR /aula

FROM mcr.microsoft.com/playwright:v1.62.0-noble AS slides
WORKDIR /opt/slides
COPY package.json package-lock.json ./
RUN npm ci --no-audit --no-fund \
    && ln -s "$(node -p "require('playwright-core').chromium.executablePath()")" /usr/local/bin/chromium
ENV CHROME_PATH=/usr/local/bin/chromium
COPY scripts/verificar-slides.mjs /opt/slides/
WORKDIR /aula
