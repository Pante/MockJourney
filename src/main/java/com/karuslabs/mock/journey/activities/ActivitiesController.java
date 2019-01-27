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
package com.karuslabs.mock.journey.activities;

import com.karuslabs.mock.journey.*;
import com.karuslabs.mock.journey.activities.transaction.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static java.util.stream.Collectors.toMap;
import static org.springframework.web.bind.annotation.RequestMethod.*;


@CrossOrigin
@RestController
public class ActivitiesController extends Controller<Activities> {    
    
    protected Map<Integer, ActivitySupplier> suppliers = new ConcurrentHashMap<>();
    protected AtomicInteger counter = new AtomicInteger(55);
    
    
    public ActivitiesController() throws IOException {
        super("events.json", Activities.class);
        load(1);
        load(2);
    }
    
    
    @RequestMapping(path = "/activities", method = GET)
    public Activities view(@RequestParam("id") int id) throws IOException {
        return load(id);
    }
    
    
    @Override
    protected void patch(Activities activities) {
        var map = activities.data.stream().collect(toMap(activity -> activity.id, activity -> activity));
        
        for (var entry : suppliers.entrySet()) {
            var activity = map.get(entry.getKey());
            if (activity != null) {
                if (entry.getValue().link == null) {
                    entry.getValue().link = activity.attributes.imageUrl;
                }
                
                var edited = entry.getValue().get();
                edited.attributes.enrolStatus = activity.attributes.enrolStatus;
                
                activities.data.set(activities.data.indexOf(activity), edited);
                
            } else {
                activities.data.add(entry.getValue().get());
            }
        }
    }
    
    
    @RequestMapping(path = "/activities", method = POST)
    public ResponseEntity<String> create(@RequestParam("activity") String content, @RequestParam("file") MultipartFile file) throws IOException, InterruptedException {
        var creation = Main.MAPPER.readValue(content, Creation.class);
        var link = Main.imgur.upload(file.getBytes());
        var id = counter.incrementAndGet();
        
        var supplier = new ActivitySupplier(id, creation.activity, link);
        
        var stamp = lock.writeLock();
        try {
            suppliers.put(id, supplier);
            for (var activities: users.values()) {
                activities.data.add(supplier.get());
            }
            
            return new ResponseEntity<>(HttpStatus.OK);
            
        } finally {
            lock.unlock(stamp);
        }
    }
    
    @RequestMapping(path = "/activities", method = PATCH)
    public ResponseEntity<String> edit(@RequestParam("activity") String content, @RequestParam(name = "file", required = false) MultipartFile file) throws IOException, InterruptedException {
        var edition = Main.MAPPER.readValue(content, Edition.class);
        var stamp = lock.writeLock();
        try {
            String link = null;
            if (file != null) {
                link = Main.imgur.upload(file.getBytes());
            }

            var supplier = suppliers.get(edition.id);
            if (supplier == null) {
                suppliers.put(edition.id, new ActivitySupplier(edition.id, edition.activity, link));

            } else {
                if (link != null) {
                    supplier.link = link;
                }
                supplier.activity = edition.activity;
            }

            for (var activities : users.values()) {
                patch(activities);
            }

            return new ResponseEntity<>(HttpStatus.OK);
            
        } finally {
            lock.unlock(stamp);
        }
    }

    
    @RequestMapping(path = "/activities", method = DELETE)
    public ResponseEntity<String> delete(@RequestParam("id") int id) {
        var deleted = false;

        if (suppliers.remove(id) != null) {
            deleted = true;
        }

        for (var activities : users.values()) {
            for (var iterator = activities.data.iterator(); iterator.hasNext();) {
                var activity = iterator.next();
                if (activity.id == id) {
                    iterator.remove();
                    deleted = true;
                }
            }
        }

        return new ResponseEntity<>(deleted ? HttpStatus.OK : HttpStatus.UNPROCESSABLE_ENTITY);
    }
    
    
    @RequestMapping(path = "/student_transactions/enrol_activity", method = POST)
    public ResponseEntity<String> enrol(@RequestBody Enrollment enrollment) throws IOException {
        return update(enrollment, "Enrolled");
    }
    
    @RequestMapping(path = "/student_transactions/unenrol_activity", method = POST)
    public ResponseEntity<String> unenrol(@RequestBody Enrollment enrollment) throws IOException {
        return update(enrollment, "Unenrolled");
    }
    
        
    protected ResponseEntity<String> update(Enrollment enrollment, String status) throws IOException {
        var activities = load(enrollment.student);
        var stamp = lock.readLock();
        try {
            for (var activity : activities.data) {
                if (activity.id == enrollment.activity && !activity.attributes.enrolStatus.equals(status)) {
                    var original = activity.attributes.enrolStatus;
                    
                    while (activity.attributes.enrolStatus.equals(original)) {
                        var write = lock.tryConvertToWriteLock(stamp);
                        if (write != 0L) {
                            stamp = write;
                            activity.attributes.enrolStatus = status;
                            return new ResponseEntity(HttpStatus.OK);
                        
                        } else {
                            lock.unlockRead(stamp);
                            stamp = lock.writeLock();
                        }
                    }
                }
            }    
        
            return new ResponseEntity<>("Unable to change student enrollment status for activity", HttpStatus.UNPROCESSABLE_ENTITY);
            
        } finally {
            lock.unlock(stamp);
        }
    }
    
}
