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

import com.karuslabs.mock.journey.Main;
import com.karuslabs.mock.journey.notifiacations.Notification;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.Charset;
import java.util.List;

import org.apache.http.entity.mime.MultipartEntityBuilder;

import static java.util.stream.Collectors.toList;


public class ELocker {
    
    private HttpClient client;
    private String mobile;
    private String password;
    private String token;
    
    
    public ELocker(String mobile, String password) {
        this.client = HttpClient.newHttpClient();
        this.mobile = mobile;
        this.password = password;
        this.token = null;
    }
    
    
    public void login() throws IOException, InterruptedException {
        byte[] content;
        try (var stream = new ByteArrayOutputStream()) {
            MultipartEntityBuilder.create().addTextBody("mobile_number", mobile).addTextBody("password", password).build().writeTo(stream);
            content = stream.toByteArray();
        }
        
        var post = HttpRequest.newBuilder()
                                 .uri(URI.create("http://api-lck.ict.np.edu.sg/collectorLogin"))
                                 .header("Content-Type", "multipart/form-data")
                                 .POST(HttpRequest.BodyPublishers.ofByteArray(content)).build();

        var response = client.send(post, HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));        
        token = Main.MAPPER.readTree(response.body()).get("data").get("user_token").asText();
    }
    
    
    public List<Notification> poll() throws IOException, InterruptedException {
        if (token == null) {
            login();
        }

        var response = client.send(post(), HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        if (response.statusCode() != 200) {
            login();
            response = client.send(post(), HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        }
        
        var transactions = Main.MAPPER.readValue(response.body(), Transactions.class);
        return transactions.data.transactions.stream().filter(transaction -> transaction.status == 1 && transaction.recipientEmail.equalsIgnoreCase("ict-fintechdemo1@connect.np.edu.sg")).map(transaction ->
            new Notification("You have a reward pending collection. Kindly proceed to eLocker @ 31-05 for collection by " + transaction.dateOverdued 
                           + ". Your OTP is: " + transaction.otpNumber)
        ).collect(toList());
    }
    
    private HttpRequest post() throws IOException {
        byte[] content;
        try (var stream = new ByteArrayOutputStream()) {
            MultipartEntityBuilder.create()
                                  .addTextBody("page", "1")
                                  .addTextBody("limit", "50")
                                  .addTextBody("order_col", "id")
                                  .addTextBody("order_sort", "dsc")
                                  .build().writeTo(stream);
            content = stream.toByteArray();
        }
        
        return HttpRequest.newBuilder()
                                 .uri(URI.create("http://api-lck.ict.np.edu.sg/transactions"))
                                 .header("Content-Type", "multipart/form-data")
                                 .POST(HttpRequest.BodyPublishers.ofByteArray(content)).build();
    }

}
