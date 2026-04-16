FROM mongo:latest
RUN mkdir /config
WORKDIR /config
COPY init.sh .
RUN chmod +x /config/init.sh
CMD [ "bash", "/config/init.sh"]