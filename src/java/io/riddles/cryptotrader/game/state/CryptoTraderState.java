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

import java.util.ArrayList;
import java.util.Date;

import io.riddles.cryptotrader.engine.CryptoTraderEngine;
import io.riddles.javainterface.game.state.AbstractState;

/**
 * io.riddles.cryptotrader.game.state.CryptoTraderState - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderState extends AbstractState<CryptoTraderPlayerState> {

    private boolean isBotDisqualified;
    private Date date;

    // For initial state only
    public CryptoTraderState(ArrayList<CryptoTraderPlayerState> playerStates, Date date) {
        super(null, playerStates, 0);
        this.isBotDisqualified = false;
        this.date = date;
    }

    public CryptoTraderState(
            CryptoTraderState previousState,
            ArrayList<CryptoTraderPlayerState> playerStates,
            int roundNumber
    ) {
        super(previousState, playerStates, roundNumber);
        this.isBotDisqualified = previousState.isBotDisqualified;

        long candleInterval = CryptoTraderEngine.configuration.getInt("candleInterval");
        this.date = new Date(previousState.date.getTime() + candleInterval);
    }

    public CryptoTraderState createNextState(int roundNumber) {
        // Create new player states from current player states
        ArrayList<CryptoTraderPlayerState> playerStates = new ArrayList<>();
        for (CryptoTraderPlayerState playerState : getPlayerStates()) {
            playerStates.add(new CryptoTraderPlayerState(playerState));
        }

        // Create new state from current state
        return new CryptoTraderState(this, playerStates, roundNumber);
    }

    public void setBotDisqualified() {
        this.isBotDisqualified = true;
    }

    public boolean isBotDisqualified() {
        return this.isBotDisqualified;
    }

    public Date getDate() {
        return this.date;
    }
}
