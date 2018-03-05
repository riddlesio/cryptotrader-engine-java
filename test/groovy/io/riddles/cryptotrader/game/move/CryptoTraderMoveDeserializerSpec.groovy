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

package io.riddles.cryptotrader.game.move

import io.riddles.cryptotrader.data.Chart
import io.riddles.cryptotrader.engine.CryptoTraderEngine
import io.riddles.javainterface.game.player.PlayerProvider
import io.riddles.javainterface.io.FileIOHandler
import spock.lang.Specification

/**
 * io.riddles.cryptotrader.game.move.CryptoTraderMoveDeserializerSpec - Created on 12-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
class CryptoTraderMoveDeserializerSpec extends Specification {

    def "test move deserializer"() {

        setup:
        FileIOHandler ioHandler = new FileIOHandler("./test/resources/wrapper.txt")
        CryptoTraderEngine engine = new CryptoTraderEngine(new PlayerProvider<>(), ioHandler)
        engine.willRun()
        Date date = new Date(1516147200)
        HashMap<String, Chart> charts = engine.getProcessor().getCharts()
        CryptoTraderMoveDeserializer moveDeserializer = new CryptoTraderMoveDeserializer(charts)

        when:
        CryptoTraderMove move1 = moveDeserializer.traverse("asdf", date)
        CryptoTraderMove move2 = moveDeserializer.traverse("pass", date)
        CryptoTraderMove move3 = moveDeserializer.traverse("wrong USDT_BTC 0.1", date)
        CryptoTraderMove move4 = moveDeserializer.traverse("buy wrong 0.1", date)
        CryptoTraderMove move5 = moveDeserializer.traverse("buy USDT_BTC wrong", date)
        CryptoTraderMove move6 = moveDeserializer.traverse("buy USDT_BTC 0.1,sell USDT_ETH 2", date)
        CryptoTraderMove move7 = moveDeserializer.traverse("buy USDT_BTC 0.1; sell USDT_ETH 2", date)
        CryptoTraderMove move8 = moveDeserializer.traverse("buy USDT_BTC -1", date)
        CryptoTraderMove move9 = moveDeserializer.traverse("buy USDT_BTC 0.1;buy BTC_ETH 0.5;sell USDT_ETH 0.5;buy USDT_BTC 0.1", date)

        then:
        move1.isInvalid()
        move1.getException().getMessage() == "Invalid input: Order doesn't split into 3 parts"
        move2.getOrders().isEmpty()
        move3.isInvalid()
        move3.getException().getMessage() == "Invalid input: Can't parse order type 'wrong'"
        move4.isInvalid()
        move4.getException().getMessage() == "Invalid input: Unknown pair 'wrong'"
        move5.isInvalid()
        move5.getException().getMessage() == "Invalid input: Can't parse amount 'wrong'"
        move6.isInvalid()
        move6.getException().getMessage() == "Invalid input: Order doesn't split into 3 parts"
        move7.getOrders().size() == 2
        move8.isInvalid()
        move8.getException().getMessage() == "Invalid input: Amount must be greater than 0"
        move9.isInvalid()
        move9.getException().getMessage() == "Invalid input: Can't have more than one order per pair"
    }
}
