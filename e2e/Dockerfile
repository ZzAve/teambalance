# Simple shell Dockerfile for local development purposes

FROM node:20-alpine AS base

# Set working directory
WORKDIR /tmp

# Install dependencies
COPY package.json package-lock.json ./

# Use cache mount to speed up install of existing dependencies
RUN --mount=type=cache,target=/tmp/.npm \
  npm set cache /tmp/.npm && \
  npm ci

FROM node:20-bookworm AS dev

# not the best idea to run under root
USER root
WORKDIR /usr/src/app
RUN npm install @playwright/test@1.48.1 && \
  npx -y playwright@1.48.1 install --with-deps

COPY package.json package-lock.json ./
COPY --from=base /tmp/node_modules ./node_modules
COPY docker/entrypoint.sh ./

EXPOSE 9323
ENTRYPOINT ["sh","docker/entrypoint.sh"]
#CMD ["npm", "run", "start"]
