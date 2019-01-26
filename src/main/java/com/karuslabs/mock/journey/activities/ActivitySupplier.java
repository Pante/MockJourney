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

import com.karuslabs.mock.journey.Main;
import com.karuslabs.mock.journey.activities.transaction.ActivityModification;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Supplier;


public class ActivitySupplier implements Supplier<Activity> {
    
    public int id;
    public ActivityModification activity;
    public String link;
    
    
    public ActivitySupplier(int id, ActivityModification modification, String link) {
        this.id = id;
        this.activity = modification;
        this.link = link;
    }

    
    @Override
    public Activity get() {
        var activity = new Activity();
        activity.id = id;
        activity.type = "activities";
        activity.attributes = new Attributes();
        activity.attributes.title = this.activity.title;
        activity.attributes.description = this.activity.description;
        activity.attributes.eventDatetime = this.activity.eventDatetime;
        activity.attributes.staff = this.activity.staffId;
        activity.attributes.points = this.activity.points;
        activity.attributes.mentorGroups = this.activity.mentorGroups;
        activity.attributes.updatedAt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
        activity.attributes.imageUrl = link;
        for (var entry: this.activity.awardType.getAdditionalProperties().entrySet()) {
            var award = new ActivityAward();
            award.id = Integer.parseInt(entry.getKey());
            award.awardName = Main.AWARDS.get(award.id);
            award.toAward = (int) entry.getValue();
            activity.attributes.activityAwards.add(award);
        }

        return activity;
    }
    
}
