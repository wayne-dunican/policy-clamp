#-------------------------------------------------------------------------------
# ============LICENSE_START=======================================================
#  Copyright (C) 2022 Nordix Foundation.
# ================================================================================
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# SPDX-License-Identifier: Apache-2.0
# ============LICENSE_END=========================================================
#-------------------------------------------------------------------------------

#
# Docker file to build an image that runs the CLAMP ACM K8S Participant on Java 11 or better in OpenSuse
#
FROM opensuse/leap:15.3

LABEL maintainer="Policy Team"

ARG POLICY_LOGS=/var/log/onap/policy/clamp

ENV POLICY_LOGS=$POLICY_LOGS
ENV POLICY_HOME=/opt/app/policy/clamp
ENV LANG=en_US.UTF-8 LANGUAGE=en_US:en LC_ALL=en_US.UTF-8
ENV JAVA_HOME=/usr/lib64/jvm/java-11-openjdk-11

RUN zypper -n -q install --no-recommends gzip java-11-openjdk-headless netcat-openbsd tar wget && \
    zypper -n -q update && zypper -n -q clean --all && \
    groupadd --system policy && \
    useradd --system --shell /bin/sh -G policy policy && \
    mkdir -p /app $POLICY_LOGS $POLICY_HOME $POLICY_HOME/bin && \
    chown -R policy:policy /app $POLICY_HOME $POLICY_LOGS && \
    mkdir /packages

COPY /maven/lib/kubernetes-participant.tar.gz /packages

RUN tar xvfz /packages/kubernetes-participant.tar.gz --directory $POLICY_HOME && \
    rm /packages/kubernetes-participant.tar.gz

WORKDIR $POLICY_HOME
COPY kubernetes-participant.sh  bin/.
COPY /maven/policy-clamp-participant-impl-kubernetes.jar /app/app.jar

RUN chown -R policy:policy * && \
    chmod 755 bin/*.sh && \
    chown -R policy:policy /app && \
    wget https://get.helm.sh/helm-v3.5.2-linux-amd64.tar.gz && \
    tar xvf helm-v3.5.2-linux-amd64.tar.gz && \
    mv linux-amd64/helm /usr/local/bin && \
    rm -rf linux-amd64 && \
    rm helm-v3.5.2-linux-amd64.tar.gz && \
    wget https://storage.googleapis.com/kubernetes-release/release/v1.21.1/bin/linux/amd64/kubectl && \
    chmod +x kubectl && \
    mv kubectl /usr/local/bin/kubectl

EXPOSE 8083

USER policy
WORKDIR $POLICY_HOME/bin
ENTRYPOINT [ "./kubernetes-participant.sh" ]


