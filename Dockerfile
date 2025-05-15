FROM gcr.io/distroless/static-debian12

ARG UID=1000

USER $UID

COPY bin/tales-server /
COPY --chown=$UID examples/ /Tales

VOLUME /Tales

EXPOSE 3000

ENTRYPOINT ["/tales-server"]
CMD ["-bind", "0.0.0.0:3000", "-projects", "/Tales"]
