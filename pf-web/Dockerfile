FROM node:16-alpine

RUN apk update && apk add bash curl busybox-extras openjdk14
RUN curl https://raw.githubusercontent.com/technomancy/leiningen/stable/bin/lein > /usr/local/bin/lein
RUN chmod a+x /usr/local/bin/lein

COPY ./ /src
WORKDIR /src
RUN npm install
RUN npm run release

CMD ["npx", "shadow-cljs", "watch", "app"]
