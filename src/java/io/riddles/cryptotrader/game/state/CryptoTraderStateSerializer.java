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

package io.riddles.cryptotrader.game.state;

import org.json.JSONArray;
import org.json.JSONObject;

import java.math.RoundingMode;

import io.riddles.cryptotrader.game.move.CryptoTraderMove;
import io.riddles.cryptotrader.game.move.Order;
import io.riddles.javainterface.game.state.AbstractStateSerializer;

/**
 * io.riddles.cryptotrader.game.state.CryptoTraderStateSerializer - Created on 14-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderStateSerializer extends AbstractStateSerializer<CryptoTraderState> {

    @Override
    public String traverseToString(CryptoTraderState state) {
        throw new RuntimeException("Not implemented");
    }

    @Override
    public JSONObject traverseToJson(CryptoTraderState state) {
        return visitState(state);
    }

    private JSONObject visitState(CryptoTraderState state) {
        CryptoTraderPlayerState playerState = state.getPlayerStates().get(0);
        CryptoTraderMove move = playerState.getMove();

        JSONObject stateObject = new JSONObject();

        stateObject.put("timestamp", state.getDate().getTime());
        stateObject.put("value", playerState.getStackValue());
        stateObject.put("stacks", visitStacks(playerState));

        if (move.isInvalid()) {
            stateObject.put("exception", move.getException().getMessage());
        }

        if (!move.getOrders().isEmpty()) {
            stateObject.put("orders", visitOrders(move));
        }

        return stateObject;
    }

    private JSONObject visitStacks(CryptoTraderPlayerState playerState) {
        JSONObject stacks = new JSONObject();

        playerState.getStacks().forEach((symbol, amount) ->
                stacks.put(symbol, amount.setScale(8, RoundingMode.HALF_UP).doubleValue()));

        return stacks;
    }

    private JSONArray visitOrders(CryptoTraderMove move) {
        JSONArray orders = new JSONArray();

        for (Order order : move.getOrders()) {
            JSONObject orderObject = new JSONObject();
            double amount = order.getAmount().setScale(8, RoundingMode.HALF_UP).doubleValue();

            orderObject.put("pair", order.getPair());
            orderObject.put("type", order.getType());
            orderObject.put("amount", amount);

            orders.put(orderObject);
        }

        return orders;
    }
}
