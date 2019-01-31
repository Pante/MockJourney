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
package com.karuslabs.mock.journey.mail;

import com.karuslabs.mock.journey.Controller;
import com.karuslabs.mock.journey.Main;

import java.io.IOException;
import java.util.*;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@CrossOrigin
@RestController
public class MailController extends Controller<Map<Integer, Mail>> {
    
    private static final Object LOCK = new Object();
    
    
    public MailController() throws IOException {
        super("mail.json", null);
        synchronized (LOCK) {
            load(1);
            load(2);
        }
    }
    
    
    @Override
    protected Map<Integer, Mail> deserialize() throws IOException {
        var notifications = Arrays.asList(Main.MAPPER.readValue(getClass().getClassLoader().getResourceAsStream(name), Mail[].class));
        return notifications.stream().collect(toMap(mail -> mail.id, mail -> mail));
    }

    
    @RequestMapping(path = "/mail", method = GET)
    public List<Mail> view(@RequestParam("id") int id) throws IOException, InterruptedException {
        synchronized (LOCK) {
            var mails = users.get(id);
            if (Main.enable) {
                mails.putAll(Main.locker.poll());
            }
            
            var sent = new ArrayList<>(mails.values());
            for (var mail: sent) {
                if (mail.status.equalsIgnoreCase("new")) {
                    var copy = new Mail(mail);
                    copy.status = "Displayed";
                    mails.put(mail.id, copy);
                }
            }
            
            return sent;      
        }
    }
    
    
    @RequestMapping(path = "/mail", method = PATCH)
    public ResponseEntity<String> read(@RequestBody Body body) throws IOException, InterruptedException {
        synchronized (LOCK) {
            var mail = users.get(body.id);
            mail.get(body.mail).status = "read";
        
            return new ResponseEntity<>(HttpStatus.OK);          
        }
    }
    
    
    public static class Body {
        
        public int id;
        public int mail;
        
    }
    
}
