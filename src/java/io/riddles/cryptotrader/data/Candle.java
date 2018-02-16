/*
 *  Copyright 2018 riddles.io (developers@riddles.io)
 *
 *      Licensed under the Apache License, Version 2.0 (the "License");
 *      you may not use this file except in compliance with the License.
 *      You may obtain a copy of the License at
 *
 *          http://www.apache.org/licenses/LICENSE-2.0
 *
 *      Unless required by applicable law or agreed to in writing, software
 *      distributed under the License is distributed on an "AS IS" BASIS,
 *      WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *      See the License for the specific language governing permissions and
 *      limitations under the License.
 *
 *      For the full copyright and license information, please view the LICENSE
 *      file that was distributed with this source code.
 */

package io.riddles.cryptotrader.data;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;

/**
 * io.riddles.cryptotrader.data.Candle - Created on 9-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Candle {

    private String raw;
    private String pair;
    private Date date;
    private HashMap<String, BigDecimal> data;

    public Candle(String[] format, String input) {
        this.raw = input;
        this.data = new HashMap<>();
        String[] values = input.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");

        for (int i = 0; i < format.length; i++) {
            String key = format[i];
            String value = values[i];

            switch (key) {
                case "date":
                    this.date = new Date(Long.parseLong(value));
                    break;
                case "pair":
                    this.pair = value;
                    break;
                case "high":
                case "low":
                case "open":
                case "close":
                case "volume":
                    this.data.put(key, new BigDecimal(value));
                    break;
            }
        }
    }

    public String toString() {
        return this.raw;
    }

    public Date getDate() {
        return this.date;
    }

    public String getPair() {
        return this.pair;
    }

    public BigDecimal getRate() {
        return this.data.get("close");
    }

    public HashMap<String, BigDecimal> getData() {
        return this.data;
    }
}
