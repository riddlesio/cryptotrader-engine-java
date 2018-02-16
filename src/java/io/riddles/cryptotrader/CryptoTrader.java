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

package io.riddles.cryptotrader;

import io.riddles.cryptotrader.engine.CryptoTraderEngine;
import io.riddles.cryptotrader.game.state.CryptoTraderState;
import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.io.IOHandler;

/**
 * io.riddles.cryptotrader.CryptoTrader - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTrader {

    public static void main(String[] args) throws Exception {
        CryptoTraderEngine engine = new CryptoTraderEngine(new PlayerProvider<>(), new IOHandler());

        CryptoTraderState firstState = engine.willRun();
        CryptoTraderState finalState = engine.run(firstState);

        engine.didRun(firstState, finalState);
    }
}
