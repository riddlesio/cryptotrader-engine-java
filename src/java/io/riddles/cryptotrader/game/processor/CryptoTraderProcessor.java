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

package io.riddles.cryptotrader.game.processor;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.stream.Collectors;

import io.riddles.cryptotrader.data.Candle;
import io.riddles.cryptotrader.data.Chart;
import io.riddles.cryptotrader.game.move.ActionType;
import io.riddles.cryptotrader.game.move.CryptoTraderMove;
import io.riddles.cryptotrader.game.move.CryptoTraderMoveDeserializer;
import io.riddles.cryptotrader.game.player.CryptoTraderPlayer;
import io.riddles.cryptotrader.game.state.CryptoTraderPlayerState;
import io.riddles.cryptotrader.game.state.CryptoTraderState;
import io.riddles.javainterface.exception.InvalidMoveException;
import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.game.processor.SimpleProcessor;

/**
 * io.riddles.cryptotrader.game.processor.CryptoTraderProcessor - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderProcessor extends SimpleProcessor<CryptoTraderState, CryptoTraderPlayer> {

    private CryptoTraderMoveDeserializer moveDeserializer;
    private HashMap<String, Chart> charts;
    private Date finalDate;

    public CryptoTraderProcessor(PlayerProvider<CryptoTraderPlayer> playerProvider, HashMap<String, Chart> charts) {
        super(playerProvider);

        this.charts = charts;
        this.moveDeserializer = new CryptoTraderMoveDeserializer(charts);
        this.finalDate = getFinalDate();
    }

    @Override
    public boolean hasGameEnded(CryptoTraderState state) {
        return state.isBotDisqualified() || state.getDate().equals(this.finalDate);
    }

    @Override
    public Integer getWinnerId(CryptoTraderState state) {
        return null;
    }

    @Override
    public double getScore(CryptoTraderState state) {
        if (state.isBotDisqualified()) {
            return 0.0;
        }

        return state.getPlayerStates().get(0).getStackValue();
    }

    @Override
    public CryptoTraderState createNextState(CryptoTraderState state, int roundNumber) {
        CryptoTraderState nextState = state.createNextState(roundNumber);

        String nextCandleString = getCandleString(nextState);

        for (CryptoTraderPlayerState playerState : nextState.getPlayerStates()) {
            sendUpdatesToPlayer(playerState, nextCandleString);

            CryptoTraderPlayer player = getPlayer(playerState.getPlayerId());
            CryptoTraderMove move = getPlayerMove(player, nextState);
            playerState.setMove(move);

            if (!move.isInvalid()) {
                try {
                    playerState.updateStack(move);
                } catch (InvalidMoveException exception) {
                    move.setException(exception);
                }
            }

            if (move.isInvalid()) {
                nextState.setBotDisqualified();
                player.sendWarning(move.getException().getMessage());
            }

            playerState.updateStacksValue(nextState.getDate(), this.charts);
        }

        return nextState;
    }

    public HashMap<String, Chart> getCharts() {
        return this.charts;
    }

    private Date getFinalDate() {
        Chart chart = new ArrayList<>(this.charts.values()).get(0);

        return chart.getCandles().values().stream()
                .map(Candle::getDate)
                .max(Date::compareTo)
                .orElseThrow(() -> new RuntimeException("Can't find final candle date"));
    }

    private String getCandleString(CryptoTraderState state) {
        return this.charts.values().stream()
                .map(chart -> chart.getChandleAt(state.getDate()).toString())
                .collect(Collectors.joining(";"));
    }

    private void sendUpdatesToPlayer(CryptoTraderPlayerState playerState, String candleString) {
        CryptoTraderPlayer player = getPlayer(playerState.getPlayerId());

        player.sendUpdate("next_candles", candleString);
        player.sendUpdate("stacks", playerState.getStacksString());
    }

    private CryptoTraderMove getPlayerMove(CryptoTraderPlayer player, CryptoTraderState state) {
        String response = player.requestMove(ActionType.ORDER);
        return this.moveDeserializer.traverse(response, state.getDate());
    }

    private CryptoTraderPlayer getPlayer(int id) {
        return this.playerProvider.getPlayerById(id);
    }
}
