FROM node:22-alpine AS builder

WORKDIR /app

COPY . .

RUN npm ci

RUN npx nx run web:build

FROM caddy:2.10-alpine

WORKDIR /app

COPY --from=builder /app/apps/web/dist /app/html
COPY Caddyfile /app/Caddyfile

EXPOSE 8080

CMD ["caddy", "run", "--config", "/app/Caddyfile"]