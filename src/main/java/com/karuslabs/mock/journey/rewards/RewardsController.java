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
package com.karuslabs.mock.journey.rewards;

import com.karuslabs.mock.journey.rewards.transaction.*;
import com.karuslabs.mock.journey.*;

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
public class RewardsController extends Controller<Rewards> {    
    
    protected Map<Integer, RewardSupplier> suppliers = new ConcurrentHashMap<>();
    protected AtomicInteger counter = new AtomicInteger(30);
    
    
    public RewardsController() throws IOException {
        super("rewards.json", Rewards.class);
        load(1);
        load(2);
    }
    
    
    @RequestMapping(path = "/reward_catelogues", method = GET)
    public Rewards view(@RequestParam("id") int id) throws IOException {
        return load(id);
    }
    
    
    @Override
    protected void patch(Rewards rewards) {
        var map = rewards.data.stream().collect(toMap(activity -> activity.id, activity -> activity));
        
        for (var entry : suppliers.entrySet()) {
            var activity = map.get(entry.getKey());
            if (activity != null) {
                if (entry.getValue().link == null) {
                    entry.getValue().link = activity.attributes.imageUrl;
                }
                
                var edited = entry.getValue().get();
                edited.attributes.redeemStatus = activity.attributes.redeemStatus;
                
                rewards.data.set(rewards.data.indexOf(activity), edited);
                
            } else {
                rewards.data.add(entry.getValue().get());
            }
        }
    }
    
    
    @RequestMapping(path = "/reward_catelogues", method = POST)
    public ResponseEntity<String> create(@RequestParam("reward") String content, @RequestParam(name = "file", required = false) MultipartFile file) throws IOException, InterruptedException {
        var creation = Main.MAPPER.readValue(content, Creation.class);
        var link = Main.imgur.upload(file.getBytes());
        var id = counter.incrementAndGet();
        
        var supplier = new RewardSupplier(id, creation.reward, link);
        
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
    
    
    @RequestMapping(path = "/reward_catelogues", method = PATCH)
    public ResponseEntity<String> edit(@RequestParam("reward") String content, @RequestParam(name = "file", required = false) MultipartFile file) throws IOException, InterruptedException {
        var edition = Main.MAPPER.readValue(content, Edition.class);
        var stamp = lock.writeLock();
        try {
            String link = null;
            if (file != null) {
                link = Main.imgur.upload(file.getBytes());
            }

            var supplier = suppliers.get(edition.id);
            if (supplier == null) {
                suppliers.put(edition.id, new RewardSupplier(edition.id, edition.reward, link));

            } else {
                if (link != null) {
                    supplier.link = link;
                }
                supplier.reward = edition.reward;
            }

            for (var activities : users.values()) {
                patch(activities);
            }

            return new ResponseEntity<>(HttpStatus.OK);
            
        } finally {
            lock.unlock(stamp);
        }
    }

    
    @RequestMapping(path = "/reward_catelogues", method = DELETE)
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
    
    
    @RequestMapping(path = "/reward_transactions", method = POST)
    public ResponseEntity<String> redeem(@RequestBody Redemption redemption) throws IOException {
        var rewards = load(redemption.student).data.stream().collect(toMap(reward -> reward.id, reward -> reward));
        var profile = Main.profiles.get(redemption.student);
        
        var stamp = lock.writeLock();
        try {
            var cost = 0;
            var redeemed = new ArrayList<Reward>();
            for (var item : redemption.items) {
                var reward = rewards.get(item.id);
                if (reward != null) {
                    cost += reward.attributes.points * item.quantity;
                    redeemed.add(reward);
                }
            }
            
            if (profile.data.attributes.redemptionBalance < cost) {
                return new ResponseEntity<>("Insufficent Bytes", HttpStatus.UNPROCESSABLE_ENTITY);
            }
            
            profile.data.attributes.redemptionBalance -= cost;
            for (var reward : redeemed) {
                reward.attributes.redeemStatus = "Pending";
            }    
            
            return new ResponseEntity<>(HttpStatus.OK);
            
            
        } finally {
            lock.unlock(stamp);
        }
    }
    
}
