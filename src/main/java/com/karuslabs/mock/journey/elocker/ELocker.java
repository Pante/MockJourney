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

import com.fasterxml.jackson.annotation.JsonProperty;
import com.karuslabs.mock.journey.Main;
import com.karuslabs.mock.journey.mail.Mail;

import java.io.*;
import java.net.URI;
import java.net.http.*;
import java.nio.charset.Charset;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.stream.Collectors.toMap;


public class ELocker {
    
    private Set<Integer> ids;
    private HttpClient client;
    private AtomicInteger counter;
    private String mobile;
    private String password;
    private String token;
    
    
    public ELocker(String mobile, String password) {
        this.ids = ConcurrentHashMap.newKeySet();
        this.client = HttpClient.newHttpClient();
        this.counter = new AtomicInteger(5);
        this.mobile = mobile;
        this.password = password;
        this.token = null;
    }
    
    
    public void login() throws IOException, InterruptedException {
            var post = HttpRequest.newBuilder()
                                 .uri(URI.create("http://api-lck.ict.np.edu.sg/collectorLogin"))
                                 .header("Content-Type", "application/json")
                                 .POST(HttpRequest.BodyPublishers.ofString(Main.MAPPER.writeValueAsString(new Credentials(mobile, password)))).build();

        var response = client.send(post, HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        token = Main.MAPPER.readTree(response.body()).get("data").get("user_token").asText();
    }
    
    private static class Credentials {
        @JsonProperty("mobile_number")
        public String mobile;
        @JsonProperty("password")
        public String password;
        
        
        public Credentials(String mobile, String password) {
            this.mobile = mobile;
            this.password = password;
        }
        
    }
    
    
    public Map<Integer, Mail> poll() throws IOException, InterruptedException {
        if (token == null) {
            login();
        }

        var response = client.send(post(), HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        if (response.statusCode() != 200) {
            login();
            response = client.send(post(), HttpResponse.BodyHandlers.ofString(Charset.forName("UTF-8")));
        }
        
        var transactions = Main.MAPPER.readValue(response.body(), Transactions.class);
        return transactions.data.transactions.stream()
                .filter(transaction -> transaction.status == 1 && transaction.recipientEmail.equalsIgnoreCase("ict-fintechdemo1@connect.np.edu.sg"))
                .filter(transaction -> ids.add(transaction.id))
                .collect(toMap(a -> counter.incrementAndGet(), transaction -> new Mail(counter.get(),"You have a reward pending collection. Kindly proceed to eLocker @ 31-05 for collection by " + transaction.dateOverdued 
                           + ". Your OTP is: " + transaction.otpNumber)));
    }
    
    private HttpRequest post() throws IOException {        
        return HttpRequest.newBuilder()
                                 .uri(URI.create("http://api-lck.ict.np.edu.sg/transactions"))
                                 .header("user_token", token)
                                 .header("Content-Type", "application/json")
                                 .POST(HttpRequest.BodyPublishers.ofString(Main.MAPPER.writeValueAsString(new Post(1, 50, "id", "dsc")))).build();
    }
    
    private static class Post {
        
        @JsonProperty("page")
        public int page;
        @JsonProperty("limit")
        public int limit;
        @JsonProperty("order_col")
        public String col;
        @JsonProperty("order_sort")
        public String sort;
        
        
        public Post(int page, int limit, String col, String sort) {
            this.page = page;
            this.limit = limit;
            this.col = col;
            this.sort = sort;
        }
        
    }

}
