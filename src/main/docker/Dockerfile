FROM registry.access.redhat.com/ubi9/ubi-micro:9.7@sha256:e9765516d74cafded50d8ef593331eeca2ef6eababdda118e5297898d99b7433

WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --chown=1001:root --chmod=0755 target/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application"]
