/*
 * The MIT License
 *
 * Copyright 2019 Matthias.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.karuslabs.mock.journey.elocker;

import java.util.HashMap;
import java.util.Map;
import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "id",
    "transaction_id",
    "deposit_id",
    "recipient_email",
    "collector_id",
    "return_id",
    "end_collect_id",
    "locker_id",
    "locker_deposit_id",
    "locker_return_id",
    "terminal_id",
    "otp_number",
    "date_deposit",
    "date_overdued",
    "date_collected",
    "date_return",
    "date_end_collect",
    "logs",
    "status",
    "del_flag",
    "is_send",
    "is_send_reminder",
    "created_at",
    "updated_at"
})
public class Transaction {

    @JsonProperty("id")
    public int id;
    @JsonProperty("transaction_id")
    public String transactionId;
    @JsonProperty("deposit_id")
    public int depositId;
    @JsonProperty("recipient_email")
    public String recipientEmail;
    @JsonProperty("collector_id")
    public Object collectorId;
    @JsonProperty("return_id")
    public Object returnId;
    @JsonProperty("end_collect_id")
    public Object endCollectId;
    @JsonProperty("locker_id")
    public int lockerId;
    @JsonProperty("locker_deposit_id")
    public int lockerDepositId;
    @JsonProperty("locker_return_id")
    public Object lockerReturnId;
    @JsonProperty("terminal_id")
    public int terminalId;
    @JsonProperty("otp_number")
    public String otpNumber;
    @JsonProperty("date_deposit")
    public String dateDeposit;
    @JsonProperty("date_overdued")
    public String dateOverdued;
    @JsonProperty("date_collected")
    public String dateCollected;
    @JsonProperty("date_return")
    public Object dateReturn;
    @JsonProperty("date_end_collect")
    public Object dateEndCollect;
    @JsonProperty("logs")
    public String logs;
    @JsonProperty("status")
    public int status;
    @JsonProperty("del_flag")
    public int delFlag;
    @JsonProperty("is_send")
    public int isSend;
    @JsonProperty("is_send_reminder")
    public int isSendReminder;
    @JsonProperty("created_at")
    public String createdAt;
    @JsonProperty("updated_at")
    public String updatedAt;
    @JsonIgnore
    private Map<String, Object> additionalProperties = new HashMap<String, Object>();

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
