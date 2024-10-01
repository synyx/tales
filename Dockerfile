FROM gcr.io/distroless/static-debian12

EXPOSE 3000

COPY bin/tales-server /

VOLUME /work

USER 1000

ENTRYPOINT ["/tales-server"]
CMD ["-projects", "/work"]
