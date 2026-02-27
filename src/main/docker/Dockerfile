FROM registry.access.redhat.com/ubi9/ubi-minimal:9.7-1771346502@sha256:c7d44146f826037f6873d99da479299b889473492d3c1ab8af86f08af04ec8a0

WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --chown=1001:root --chmod=0755 target/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application"]
