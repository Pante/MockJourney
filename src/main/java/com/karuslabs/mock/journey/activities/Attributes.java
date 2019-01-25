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

import com.fasterxml.jackson.annotation.*;

import java.util.List;


@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
    "title",
    "description",
    "event-datetime",
    "points",
    "category",
    "category-desc",
    "staff",
    "enrol-status",
    "updated-at",
    "activity-awards",
    "mentor-groups",
    "image-url"
})
public class Attributes {

    @JsonProperty("title")
    public String title;
    @JsonProperty("description")
    public String description;
    @JsonProperty("event-datetime")
    public String eventDatetime;
    @JsonProperty("points")
    public int points;
    @JsonProperty("category")
    public String category;
    @JsonProperty("category-desc")
    public String categoryDesc;
    @JsonProperty("staff")
    public String staff;
    @JsonProperty("enrol-status")
    public String enrolStatus;
    @JsonProperty("updated-at")
    public String updatedAt;
    @JsonProperty("activity-awards")
    public List<ActivityAward> activityAwards = null;
    @JsonProperty("mentor-groups")
    public List<Integer> mentorGroups = null;
    @JsonProperty("image-url")
    public String imageUrl;

}
