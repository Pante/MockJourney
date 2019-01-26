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
package com.karuslabs.mock.journey.mc;

import com.karuslabs.mock.journey.Main;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.web.bind.annotation.RequestMethod.*;


@RestController
public class MCController {
    
    private Map<String, Boolean> links = new ConcurrentHashMap<>();
    
    
    @RequestMapping(path = "/medical_certificates", method = GET)
    public Map<String, Boolean> view() {
        return links;
    }
    
    
    @RequestMapping(path = "/medical_certificates", method = POST)
    public ResponseEntity<String> post(@RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        links.put(Main.imgur.upload(file.getBytes()), false);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    
    @RequestMapping(path = "/medical_certificates/approve", method = PATCH)
    public ResponseEntity<String> approve(Approval approval) throws IOException, InterruptedException {
        links.put(approval.link, true);
        
        return new ResponseEntity<>(HttpStatus.OK);
    }
    
    public static class Approval {
        
        public String link;
        
    }
    
}
