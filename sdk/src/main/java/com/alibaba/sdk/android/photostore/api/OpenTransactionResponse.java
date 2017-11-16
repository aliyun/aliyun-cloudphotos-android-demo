/**
 * Copyright (C) 2017 Alibaba Group Holding Limited
 */
package com.alibaba.sdk.android.photostore.api;

import com.alibaba.fastjson.annotation.JSONField;

public class OpenTransactionResponse extends BaseResponse {
    @JSONField(name = "RequestId")
    public String requestId;
    @JSONField(name = "Transaction")
    public Transaction data;

    public static class Transaction {
        @JSONField(name = "Head")
        public Head head;
        @JSONField(name = "Upload")
        public Upload upload;
    }

    public static class Head {
        @JSONField(name = "SessionId")
        public String sid;
        @JSONField(name = "Offset")
        public int offset;
        @JSONField(name = "Len")
        public int len;
    }

    public static class Upload {
        @JSONField(name = "OssEndpoint")
        public String ossEndpoint;
        @JSONField(name = "Bucket")
        public String bucket;
        @JSONField(name = "AccessKeyId")
        public String accessKeyId;
        @JSONField(name = "AccessKeySecret")
        public String accessKeySecret;
        @JSONField(name = "StsToken")
        public String stsToken;
        @JSONField(name = "FileId")
        public String fileId;
        @JSONField(name = "ObjectKey")
        public String objectKey;
        @JSONField(name = "SessionId")
        public String sid;
    }
}
