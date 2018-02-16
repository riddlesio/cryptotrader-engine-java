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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import io.riddles.cryptotrader.data.Candle;
import io.riddles.cryptotrader.data.Chart;
import io.riddles.cryptotrader.engine.CryptoTraderEngine;
import io.riddles.cryptotrader.game.move.CryptoTraderMove;
import io.riddles.cryptotrader.game.move.MoveType;
import io.riddles.cryptotrader.game.move.Order;
import io.riddles.javainterface.exception.InvalidMoveException;
import io.riddles.javainterface.game.state.AbstractPlayerState;

/**
 * io.riddles.cryptotrader.game.state.CryptoTraderPlayerState - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderPlayerState extends AbstractPlayerState<CryptoTraderMove> {

    private HashMap<String, BigDecimal> stacks;
    private double stackValue;

    public CryptoTraderPlayerState(int playerId, Date date, HashMap<String, Chart> charts) {
        super(playerId);
        initializeStacks(date, charts);
    }

    public CryptoTraderPlayerState(CryptoTraderPlayerState playerState) {
        super(playerState.getPlayerId());
        this.stacks = new HashMap<>();
        this.stacks.putAll(playerState.stacks);
        this.stackValue = playerState.stackValue;
    }

    // For now we are assuming each symbol has a USDT trading pair
    public void updateStacksValue(Date date, HashMap<String, Chart> charts) {
        BigDecimal value = BigDecimal.ZERO;

        for (Map.Entry<String, BigDecimal> entry : this.stacks.entrySet()) {
            String symbol = entry.getKey();
            BigDecimal amount = entry.getValue();

            if (symbol.equals("USDT")) {
                value = value.add(amount);
            } else {
                Chart chart = charts.get("USDT_" + symbol);
                Candle candle = chart.getChandleAt(date);

                value = value.add(amount.multiply(candle.getRate()));
            }
        }

        this.stackValue = value.setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    public String getStacksString() {
        return this.stacks.entrySet().stream()
                .map(entry -> {
                    BigDecimal amount = entry.getValue();
                    int scale = entry.getKey().equals("USDT") ? 2 : 8;

                    String value = amount.setScale(scale, RoundingMode.DOWN).toPlainString();

                    return String.format("%s:%s", entry.getKey(), value);
                })
                .collect(Collectors.joining(","));
    }

    public void updateStack(CryptoTraderMove move) throws InvalidMoveException {
        for (Order order : move.getOrders()) {
            String[] symbols = order.getPair().split("_");

            String minusSymbol;
            String plusSymbol;
            BigDecimal minusAmount;
            BigDecimal plusAmount;
            if (order.getType() == MoveType.BUY) {
                minusSymbol = symbols[0];
                plusSymbol = symbols[1];
                minusAmount = order.getAmount().multiply(order.getRate());
                plusAmount = order.getAmount();
            } else {
                minusSymbol = symbols[1];
                plusSymbol = symbols[0];
                minusAmount = order.getAmount();
                plusAmount = order.getAmount().multiply(order.getRate());
            }

            BigDecimal stack = this.stacks.get(minusSymbol).setScale(8, RoundingMode.HALF_UP);
            BigDecimal roundedAmount = minusAmount.setScale(8, RoundingMode.DOWN);
            if (stack.compareTo(roundedAmount) < 0) {
                throw new InvalidMoveException(
                        String.format(
                                "%s stack (%s, %s) is too small for this order (%s, %s)",
                                minusSymbol,
                                this.stacks.get(minusSymbol),
                                stack,
                                minusAmount,
                                roundedAmount
                        )
                );
            }

            updateStack(minusSymbol, minusAmount.negate());
            updateStack(plusSymbol, plusAmount);
        }
    }

    public HashMap<String, BigDecimal> getStacks() {
        return this.stacks;
    }

    public double getStackValue() {
        return this.stackValue;
    }

    private void updateStack(String symbol, BigDecimal delta) {
        this.stacks.put(symbol, this.stacks.get(symbol).add(delta));
    }

    private void initializeStacks(Date date, HashMap<String, Chart> charts) {
        this.stacks = new HashMap<>();

        BigDecimal initialStack = new BigDecimal(CryptoTraderEngine.configuration.getInt("initialStack"));

        for (String pair : charts.keySet()) {
            String[] split = pair.split("_");

            for (String symbol : split) {
                if (this.stacks.containsKey(symbol)) continue;

                if (symbol.equals("USDT")) {
                    this.stacks.put(symbol, initialStack);
                } else {
                    this.stacks.put(symbol, BigDecimal.ZERO);
                }
            }
        }

        if (!this.stacks.containsKey("USDT")) {
            throw new RuntimeException("USDT should be in at least one trading pair");
        }

        updateStacksValue(date, charts);
    }
}
