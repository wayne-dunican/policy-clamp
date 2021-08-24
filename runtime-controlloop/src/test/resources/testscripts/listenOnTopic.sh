#! /bin/bash
#  ============LICENSE_START=======================================================
#  Copyright (C) 2021 Nordix Foundation.
#  ================================================================================
#  Licensed under the Apache License, Version 2.0 (the "License");
#  you may not use this file except in compliance with the License.
#  You may obtain a copy of the License at
#
#      http://www.apache.org/licenses/LICENSE-2.0
#
#  Unless required by applicable law or agreed to in writing, software
#  distributed under the License is distributed on an "AS IS" BASIS,
#  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#  See the License for the specific language governing permissions and
#  limitations under the License.
#
#  SPDX-License-Identifier: Apache-2.0
#  ============LICENSE_END=========================================================

if [ $# -ne 1 ]
then
    echo invalid parameters $*, specify a single parameter as the topic to listen on
    exit 1
fi

while true
do
    curl "http://localhost:3904/events/$1/TEST/1?timeout=60000"
    echo ""
done

