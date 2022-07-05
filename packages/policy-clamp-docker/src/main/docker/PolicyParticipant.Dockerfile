#-------------------------------------------------------------------------------
# ============LICENSE_START=======================================================
#  Copyright (C) 2021-2022 Nordix Foundation.
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
# Docker file to build an image that runs the CLAMP ACM Policy Framework Participant on Java 11 or better in alpine
#
FROM onap/policy-jre-alpine:2.4.3

LABEL maintainer="Policy Team"
LABEL org.opencontainers.image.title="Policy CLAMP ACM Policy Framework Participant"
LABEL org.opencontainers.image.description="Policy CLAMP ACM Policy Framework Participant image based on Alpine"
LABEL org.opencontainers.image.url="https://github.com/onap/policy-clamp"
LABEL org.opencontainers.image.vendor="ONAP Policy Team"
LABEL org.opencontainers.image.licenses="Apache-2.0"
LABEL org.opencontainers.image.created="${git.build.time}"
LABEL org.opencontainers.image.version="${git.build.version}"
LABEL org.opencontainers.image.revision="${git.commit.id.abbrev}"

ARG POLICY_LOGS=/var/log/onap/policy/pf-participant

ENV POLICY_LOGS=$POLICY_LOGS
ENV POLICY_HOME=$POLICY_HOME/clamp

RUN mkdir -p $POLICY_LOGS $POLICY_HOME $POLICY_HOME/bin && \
    chown -R policy:policy $POLICY_HOME $POLICY_LOGS && \
    mkdir /packages
COPY /maven/lib/policy-participant.tar.gz /packages

RUN tar xvfz /packages/policy-participant.tar.gz --directory $POLICY_HOME && \
    rm /packages/policy-participant.tar.gz

WORKDIR $POLICY_HOME
COPY policy-participant.sh  bin/.
COPY /maven/policy-clamp-participant-impl-policy.jar /app/app.jar

RUN chown -R policy:policy * && \
    chmod 755 bin/*.sh && \
    chown -R policy:policy /app

EXPOSE 8085

USER policy
WORKDIR $POLICY_HOME/bin
ENTRYPOINT [ "./policy-participant.sh" ]
