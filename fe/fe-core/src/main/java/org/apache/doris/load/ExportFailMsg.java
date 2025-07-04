// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package org.apache.doris.load;

import org.apache.doris.common.io.Text;
import org.apache.doris.common.io.Writable;

import com.google.gson.annotations.SerializedName;

import java.io.DataOutput;
import java.io.IOException;

public class ExportFailMsg implements Writable {
    public enum CancelType {
        USER_CANCEL,
        SUBMIT_FAIL,
        RUN_FAIL,
        TIMEOUT,
        UNKNOWN
    }

    @SerializedName("cancelType")
    private CancelType cancelType;
    @SerializedName("msg")
    private String msg;

    public ExportFailMsg(CancelType cancelType, String msg) {
        this.cancelType = cancelType;
        this.msg = msg;
    }

    public CancelType getCancelType() {
        return cancelType;
    }

    public String getMsg() {
        return msg;
    }

    @Override
    public String toString() {
        return "ExportFailMsg [cancelType=" + cancelType + ", msg=" + msg + "]";
    }

    @Override
    public void write(DataOutput out) throws IOException {
        Text.writeString(out, cancelType.name());
        Text.writeString(out, msg);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }

        if (!(obj instanceof ExportFailMsg)) {
            return false;
        }

        ExportFailMsg failMsg = (ExportFailMsg) obj;

        return cancelType.equals(failMsg.cancelType) && msg.equals(failMsg.msg);
    }
}
