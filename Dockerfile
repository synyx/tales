FROM gcr.io/distroless/static-debian12

EXPOSE 3000

COPY bin/tales-server /
COPY examples/ /Tales

VOLUME /Tales

USER 1000

ENTRYPOINT ["/tales-server"]
CMD ["-bind", "127.0.0.1:3000", "-projects", "/Tales"]
