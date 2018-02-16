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

import java.util.Date;
import java.util.HashMap;

/**
 * io.riddles.cryptotrader.data.Chart - Created on 9-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class Chart {

    private HashMap<Long, Candle> candles;

    public Chart() {
        this.candles = new HashMap<>();
    }

    public void addCandle(Candle candle) {
        if (this.candles.containsKey(candle.getDate().getTime())) {
            throw new RuntimeException("Can't have two candles with the same timestamp");
        }

        this.candles.put(candle.getDate().getTime(), candle);
    }

    public Candle getChandleAt(Date date) {
        return this.candles.get(date.getTime());
    }

    public HashMap<Long, Candle> getCandles() {
        return this.candles;
    }
}
