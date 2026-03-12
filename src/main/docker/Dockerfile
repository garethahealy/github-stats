FROM registry.access.redhat.com/ubi9/ubi-minimal:9.7-1773204619@sha256:69f5c9886ecb19b23e88275a5cd904c47dd982dfa370fbbd0c356d7b1047ef68

WORKDIR /work/

RUN chown 1001 /work \
    && chmod "g+rwX" /work \
    && chown 1001:root /work

COPY --chown=1001:root --chmod=0755 target/*-runner /work/application

EXPOSE 8080
USER 1001

ENTRYPOINT ["./application"]
