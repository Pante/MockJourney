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
package com.karuslabs.mock.journey;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.karuslabs.mock.journey.profile.Profile;

import java.io.IOException;
import java.util.Map;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;


@SpringBootApplication
public class Main {

    public static final ObjectMapper MAPPER = new ObjectMapper();
    public static final Map<Integer, String> AWARDS = Map.of(1, "Responsibility", 2, "Respect", 3, "Resilience", 4, "Integrity", 5, "Compassion");
    
    public static Map<Integer, Profile> profiles;
    public static Imgur imgur = new Imgur("f63ed069bdd8272");
    
    
    public static void main(String[] args) throws IOException {
        SpringApplication.run(Main.class, args);
        profiles = Map.of(1, Main.MAPPER.readValue(Main.class.getClassLoader().getResourceAsStream("staff.json"), Profile.class), 
                          2, Main.MAPPER.readValue(Main.class.getClassLoader().getResourceAsStream("student.json"), Profile.class)
                        );
    }
    
}
