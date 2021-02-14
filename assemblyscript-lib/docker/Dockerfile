FROM node:lts-alpine3.13

RUN addgroup -S gradle && adduser -S gradle -G gradle
RUN mkdir /data
COPY src /data
RUN chown -R gradle:gradle /data
RUN chmod -R u+rw /data

USER gradle

