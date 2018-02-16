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

import java.util.HashMap;

import io.riddles.cryptotrader.data.Chart;
import io.riddles.cryptotrader.game.processor.CryptoTraderProcessor;
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

            JSONObject stateJson = serializer.traverseToJson(state);
            if (stateJson != null) {
                states.put(stateJson);
            }
        }

        game.put("charts", visitCharts(processor.getCharts()));
        game.put("states", states);

        return game.toString();
    }

    private JSONObject visitCharts(HashMap<String, Chart> charts) {
        JSONObject chartsObject = new JSONObject();

        charts.forEach((pair, chart) -> {
            JSONArray candles = new JSONArray();

            chart.getCandles().forEach((timestamp, candle) -> {
                JSONObject candleObject = new JSONObject();
                candleObject.put("timestamp", timestamp);

                candle.getData().forEach((key, value) -> {
                    if (!key.equals("pair")) {
                        candleObject.put(key, value);
                    }
                });

                candles.put(candleObject);
            });

            chartsObject.put(pair, candles);
        });

        return chartsObject;
    }
}
