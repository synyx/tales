FROM gcr.io/distroless/static-debian12

USER 1000

COPY bin/tales-server /
COPY examples/ /Tales

VOLUME /Tales

EXPOSE 3000

ENTRYPOINT ["/tales-server"]
CMD ["-bind", "127.0.0.1:3000", "-projects", "/Tales"]
