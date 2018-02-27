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

package io.riddles.cryptotrader.game;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.riddles.cryptotrader.data.Candle;
import io.riddles.cryptotrader.data.Chart;
import io.riddles.cryptotrader.engine.CryptoTraderEngine;
import io.riddles.cryptotrader.game.processor.CryptoTraderProcessor;
import io.riddles.cryptotrader.game.state.CryptoTraderPlayerState;
import io.riddles.cryptotrader.game.state.CryptoTraderState;
import io.riddles.cryptotrader.game.state.CryptoTraderStateSerializer;
import io.riddles.javainterface.game.AbstractGameSerializer;

/**
 * io.riddles.cryptotrader.game.CryptoTraderSeriazlier - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderSerializer extends AbstractGameSerializer<CryptoTraderProcessor, CryptoTraderState> {

    @Override
    public String traverseToString(CryptoTraderProcessor processor, CryptoTraderState initialState) {
        CryptoTraderStateSerializer serializer = new CryptoTraderStateSerializer();

        JSONObject game = new JSONObject();
        game = addDefaultJSON(initialState, game, processor);

        JSONArray states = new JSONArray();
        CryptoTraderState state = initialState;
        while (state.hasNextState()) {
            state = (CryptoTraderState) state.getNextState();
            states.put(serializer.traverseToJson(state));
        }

        game.put("charts", visitCharts(processor.getCharts()));
        game.put("states", states);

        return game.toString();
    }

    private JSONObject visitCharts(HashMap<String, Chart> charts) {
        JSONObject chartsObject = new JSONObject();

        Date firstDate = getFirstDate(charts);
        int interval = CryptoTraderEngine.configuration.getInt("candleInterval");

        charts.forEach((pair, chart) -> {
            JSONArray candles = new JSONArray();
            Date date = firstDate;
            Candle candle = chart.getChandleAt(date);

            while (candle != null) {
                JSONObject candleObject = new JSONObject();
                candleObject.put("timestamp", date.getTime());

                candle.getData().forEach((key, value) -> {
                    if (key.equals("pair")) return;

                    if (pair.contains("USDT")) {
                        value = value.setScale(2, RoundingMode.HALF_UP);
                    }

                    candleObject.put(key, value.doubleValue());
                });

                candles.put(candleObject);

                date = new Date(date.getTime() + interval);
                candle = chart.getChandleAt(date);
            }

            chartsObject.put(pair, candles);
        });

        return chartsObject;
    }

    private Date getFirstDate(HashMap<String, Chart> charts) {
        Chart chart = new ArrayList<>(charts.values()).get(0);

        return chart.getCandles().values().stream()
                .map(Candle::getDate)
                .min(Date::compareTo)
                .orElseThrow(() -> new RuntimeException("Can't find first candle date"));
    }
}
