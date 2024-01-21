/*
 * Copyright 2024 ranSprd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kiar.collectorr.connector.http;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import org.slf4j.LoggerFactory;

/**
 *
 * @author ranSprd
 */
public class RestConsumer {

    private static final org.slf4j.Logger log = LoggerFactory.getLogger(RestConsumer.class);

    public static String readStringFromURL(String requestURL) throws IOException {
        try (Scanner scanner = new Scanner(new URL(requestURL).openStream(),
                StandardCharsets.UTF_8.toString())) {
            scanner.useDelimiter("\\A");
            return scanner.hasNext() ? scanner.next() : "";
        }
    }

    public void executeRequest() {
        try {
            String response = readStringFromURL("http://192.168.2.205/get_livedata_info");
            System.out.println("result :" +response);
        } catch (Exception ex) {
            log.error("problem reading endpoint: {}", ex.getMessage());
        }
    }

}
