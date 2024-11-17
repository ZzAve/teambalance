# Simple shell Dockerfile for local development purposes

FROM node:20-alpine AS base

# Set working directory
WORKDIR /tmp

# Install dependencies
COPY package.json package-lock.json ./

#-------------------------------------------
# Use cache mount to speed up install of existing dependencies
RUN --mount=type=cache,target=/tmp/.npm \
  npm set cache /tmp/.npm && \
  npm ci
#-------------------------------------------

FROM node:20-alpine AS dev
WORKDIR /usr/src/app

COPY --from=base  /tmp/node_modules ./node_modules

CMD ["npm", "run", "start"]
