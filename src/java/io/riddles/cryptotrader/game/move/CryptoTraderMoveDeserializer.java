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

package io.riddles.cryptotrader.game.move;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import io.riddles.cryptotrader.data.Chart;
import io.riddles.javainterface.exception.InvalidInputException;
import io.riddles.javainterface.serialize.Deserializer;

/**
 * io.riddles.cryptotrader.game.move.CryptoTraderMoveDeserializer - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderMoveDeserializer implements Deserializer<CryptoTraderMove> {

    private HashMap<String, Chart> charts;

    public CryptoTraderMoveDeserializer(HashMap<String, Chart> charts) {
        this.charts = charts;
    }

    @Override
    public CryptoTraderMove traverse(String s) {
        throw new RuntimeException("Not implemented");
    }

    public CryptoTraderMove traverse(String string, Date date) {
        try {
            return visitMove(string, date);
        } catch (InvalidInputException ex) {
            return new CryptoTraderMove(ex);
        } catch (Exception ex) {
            return new CryptoTraderMove(new InvalidInputException("Failed to parse action"));
        }
    }

    private CryptoTraderMove visitMove(String input, Date date) throws InvalidInputException {
        if (MoveType.fromString(input) == MoveType.PASS) {
            return new CryptoTraderMove();
        }

        String[] split = input.split(";");
        ArrayList<Order> orders = new ArrayList<>();

        for (String orderString : split) {
            orders.add(visitOrder(orderString.trim(), date));
        }

        return new CryptoTraderMove(orders);
    }

    private Order visitOrder(String input, Date date) throws InvalidInputException {
        String[] split = input.split(" ");

        if (split.length != 3) {
            throw new InvalidInputException("Order doesn't split into 3 parts");
        }

        MoveType type = visitMoveType(split[0].trim());
        String pair = visitPair(split[1].trim());
        BigDecimal amount = visitAmount(split[2].trim());
        BigDecimal rate = this.charts.get(pair).getChandleAt(date).getRate();

        return new Order(type, pair, amount, rate);
    }

    private MoveType visitMoveType(String input) throws InvalidInputException {
        MoveType moveType = MoveType.fromString(input);

        if (moveType == null) {
            throw new InvalidInputException(String.format("Can't parse order type '%s'", input));
        }

        return moveType;
    }

    private String visitPair(String input) throws InvalidInputException {
        if (!this.charts.keySet().contains(input)) {
            throw new InvalidInputException(String.format("Unknown pair '%s'", input));
        }

        return input;
    }

    private BigDecimal visitAmount(String input) throws InvalidInputException {
        BigDecimal amount;

        try {
            amount = new BigDecimal(input);
        } catch (Exception e) {
            throw new InvalidInputException(String.format("Can't parse amount '%s'", input));
        }

        if (amount.setScale(6, RoundingMode.DOWN).doubleValue() <= 0) {
            throw new InvalidInputException("Amount must be greater than 0");
        }

        return amount;
    }
}
