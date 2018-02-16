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

package io.riddles.cryptotrader.engine;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.stream.Collectors;

import io.riddles.cryptotrader.CryptoTrader;
import io.riddles.cryptotrader.data.Candle;
import io.riddles.cryptotrader.data.Chart;
import io.riddles.cryptotrader.game.CryptoTraderSerializer;
import io.riddles.cryptotrader.game.player.CryptoTraderPlayer;
import io.riddles.cryptotrader.game.processor.CryptoTraderProcessor;
import io.riddles.cryptotrader.game.state.CryptoTraderPlayerState;
import io.riddles.cryptotrader.game.state.CryptoTraderState;
import io.riddles.javainterface.configuration.Configuration;
import io.riddles.javainterface.engine.AbstractEngine;
import io.riddles.javainterface.engine.GameLoopInterface;
import io.riddles.javainterface.engine.SimpleGameLoop;
import io.riddles.javainterface.exception.TerminalException;
import io.riddles.javainterface.game.player.PlayerProvider;
import io.riddles.javainterface.io.IOInterface;

/**
 * io.riddles.cryptotrader.engine.CryptoTraderEngine - Created on 8-2-18
 *
 * [description]
 *
 * @author Jim van Eeden - jim@riddles.io
 */
public class CryptoTraderEngine extends AbstractEngine<CryptoTraderProcessor, CryptoTraderPlayer, CryptoTraderState> {

    private HashMap<String, Chart> charts;
    private String candleFormat;

    public CryptoTraderEngine(
            PlayerProvider<CryptoTraderPlayer> playerProvider,
            IOInterface ioHandler
    ) throws TerminalException {
        super(playerProvider, ioHandler);
    }

    @Override
    protected Configuration getDefaultConfiguration() {
        Configuration configuration = new Configuration();

        configuration.put("dataFile", "/data.csv");
        configuration.put("candleInterval", 1800);
        configuration.put("initialStack", 1000);
        configuration.put("givenCandles", 336); // 1 week given beforehand

        return configuration;
    }

    @Override
    protected CryptoTraderProcessor createProcessor() {
        readDataFile(); // Must be called before sendSettingsToPlayer

        return new CryptoTraderProcessor(this.playerProvider, this.charts);
    }

    @Override
    protected GameLoopInterface createGameLoop() {
        return new SimpleGameLoop();
    }

    @Override
    protected CryptoTraderPlayer createPlayer(int id) {
        return new CryptoTraderPlayer(id);
    }

    @Override
    protected void sendSettingsToPlayer(CryptoTraderPlayer player) {
        Chart chart = new ArrayList<>(this.charts.values()).get(0);

        player.sendSetting("candle_interval", configuration.getInt("candleInterval"));
        player.sendSetting("candle_format", this.candleFormat);
        player.sendSetting("candles_total", chart.getCandles().size());
        player.sendSetting("candles_given", configuration.getInt("givenCandles"));
        player.sendSetting("initial_stack", configuration.getInt("initialStack"));
    }

    @Override
    protected CryptoTraderState getInitialState() {
        ArrayList<CryptoTraderPlayerState> playerStates = new ArrayList<>();

        Chart chart = new ArrayList<>(this.charts.values()).get(0);
        Date earliestDate = chart.getCandles().values().stream()
                .map(Candle::getDate)
                .min(Date::compareTo)
                .orElseThrow(() -> new RuntimeException("Can't find earliest candle date"));

        for (CryptoTraderPlayer player : this.playerProvider.getPlayers()) {
            int id = player.getId();
            CryptoTraderPlayerState playerState = new CryptoTraderPlayerState(
                    id, earliestDate, this.charts);

            playerStates.add(playerState);
        }

        long lastTimestamp = earliestDate.getTime() +
                (configuration.getInt("givenCandles") * configuration.getInt("candleInterval"));

        // Send candles to player before the game starts
        sendFirstUpdatesToPlayers(earliestDate.getTime(), lastTimestamp);

        return new CryptoTraderState(playerStates, new Date(lastTimestamp));
    }

    @Override
    protected String getPlayedGame(CryptoTraderState initialState) {
        CryptoTraderSerializer serializer = new CryptoTraderSerializer();
        return serializer.traverseToString(this.processor, initialState);
    }

    private void readDataFile() {
        this.charts = new HashMap<>();

        try {
            InputStream fileInputStream;
            String filePath = configuration.getString("dataFile");

            try {
                fileInputStream = new FileInputStream(filePath);
            } catch (FileNotFoundException ex) {
                fileInputStream = CryptoTrader.class.getResourceAsStream(filePath);
            }

            BufferedReader br = new BufferedReader(new InputStreamReader(fileInputStream));
            String line;
            String[] candleFormat = null;

            while ((line = br.readLine()) != null) {
                if (candleFormat == null) { // First line only
                    this.candleFormat = line;
                    candleFormat = this.candleFormat.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                } else { // Candle data
                    addCandleToCharts(new Candle(candleFormat, line));
                }
            }
        } catch (Exception ex) {
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
            System.exit(1);
        }

        validateChartData();
    }

    private void addCandleToCharts(Candle candle) {
        Chart currentChart = this.charts.get(candle.getPair());

        if (currentChart == null) {
            currentChart = new Chart();
            this.charts.put(candle.getPair(), currentChart);
        }

        currentChart.addCandle(candle);
    }

    private void validateChartData() {
        this.charts.forEach((pair, chart) -> {
            int size = new ArrayList<>(this.charts.values()).get(0).getCandles().size();

            if (chart.getCandles().size() != size) {
                throw new RuntimeException("Charts don't have a equal amount of candles.");
            }

            long lastTimeStamp = -1;
            ArrayList<Candle> sortedCandles = new ArrayList<>(chart.getCandles().values()).stream()
                    .sorted(Comparator.comparing(Candle::getDate))
                    .collect(Collectors.toCollection(ArrayList::new));

            for (Candle candle : sortedCandles) {
                if (lastTimeStamp < 0) {
                    lastTimeStamp = candle.getDate().getTime();
                    continue;
                }

                long newTimeStamp = lastTimeStamp + configuration.getInt("candleInterval");

                if (candle.getDate().getTime() != newTimeStamp) {
                    throw new RuntimeException("Candle timestamps are not according to settings");
                }

                lastTimeStamp = newTimeStamp;
            }
        });
    }

    private void sendFirstUpdatesToPlayers(long earliestTimestamp, long lastTimestamp) {
        long timestamp = earliestTimestamp;

        while (timestamp <= lastTimestamp) { // non-inclusive last timestamp
            for (CryptoTraderPlayer player : this.playerProvider.getPlayers()) {
                long finalTimestamp = timestamp;

                String candleString = this.charts.values().stream()
                        .map(chart -> chart.getChandleAt(new Date(finalTimestamp)).toString())
                        .collect(Collectors.joining(";"));

                player.sendUpdate("next_candles", candleString);
            }

            timestamp += configuration.getInt("candleInterval");
        }
    }
}
