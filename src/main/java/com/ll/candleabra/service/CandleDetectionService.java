package com.ll.candleabra.service;

import com.ll.candleabra.model.CandleType;
import com.ll.candleabra.model.StockIncrementInformation;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CandleDetectionService {

    public static Map<LocalDateTime, CandleType> detectCandleTypeAtTime(final Map<LocalDateTime, StockIncrementInformation> stockIncrementInformationMap) {
        final Map<LocalDateTime, CandleType> candlesFound = new HashMap<>();

        final List<Map.Entry<LocalDateTime, StockIncrementInformation>> stocksInTimeOrder = new ArrayList<>(stockIncrementInformationMap.entrySet());

        stocksInTimeOrder.sort(Map.Entry.comparingByKey());

        // Sliding window of 5 increments to use as data to determine candle pattern (if any)
        for (int i = 4; i < stocksInTimeOrder.size(); i++) {
            final StockIncrementInformation one = stocksInTimeOrder.get(i - 4).getValue();
            final StockIncrementInformation two = stocksInTimeOrder.get(i - 3).getValue();
            final StockIncrementInformation three = stocksInTimeOrder.get(i - 2).getValue();
            final StockIncrementInformation four = stocksInTimeOrder.get(i - 1).getValue();
            final StockIncrementInformation five = stocksInTimeOrder.get(i).getValue();

            if (isHammerCandlestick(one, two, three, four, five)) {
                candlesFound.put(stocksInTimeOrder.get(i).getKey(), CandleType.HAMMER);
            }

            if (isShootingStarCandlestick(one, two, three, four, five)) {
                candlesFound.put(stocksInTimeOrder.get(i).getKey(), CandleType.SHOOTING_STAR);
            }

            if (isBullishDojiCandlestick(one, two, three, four, five)) {
                candlesFound.put(stocksInTimeOrder.get(i).getKey(), CandleType.BULLISH_DOJI);
            }

            if (isBearishDojiCandlestick(one, two, three, four, five)) {
                candlesFound.put(stocksInTimeOrder.get(i).getKey(), CandleType.BEARISH_DOJI);
            }

            if (isBullishEngulfingCandlestick(one, two, three, four, five)) {
                candlesFound.put(stocksInTimeOrder.get(i).getKey(), CandleType.BULLISH_ENGULFING);
            }

            if (isBearishEngulfingCandlestick(one, two, three, four, five)) {
                candlesFound.put(stocksInTimeOrder.get(i).getKey(), CandleType.BEARISH_ENGULFING);
            }
        }

        return candlesFound;
    }

    /**
     * A hammer candlestick forms at the end of a downtrend and indicates a near-term price bottom.
     * The hammer candle has a lower shadow that makes a new low in the downtrend sequence and then closes
     * back up near or above the open. The lower shadow (also called a tail) must be at least two or more
     * times the size of the body. This represents the longs that finally threw in the towel and stopped
     * out as shorts start covering their positions and bargain hunters come in off the fence. A volume
     * increase also helps to solidify the hammer. To confirm the hammer candle, it is important for the
     * next candle to close above the low of the hammer candle and preferably above the body.
     */
    private static boolean isHammerCandlestick(final StockIncrementInformation threeBack,
                                               final StockIncrementInformation twoBack,
                                               final StockIncrementInformation oneBack,
                                               final StockIncrementInformation potentialHammer,
                                               final StockIncrementInformation next) {
        final boolean threeBackFall = threeBack.close() < threeBack.open();
        final boolean twoBackFall = twoBack.close() < twoBack.open();
        final boolean oneBackFall = oneBack.close() < oneBack.open();
        final boolean lowestCurrentLow = potentialHammer.low() < oneBack.low() && potentialHammer.low() < twoBack.low();

        final float bodySize = Math.abs(potentialHammer.close() - potentialHammer.open());

        final boolean lowAtLeastTwiceSizeOfBody = (potentialHammer.open() - (bodySize * 2)) > potentialHammer.low();
        final boolean volumeIncreased = potentialHammer.volume() > oneBack.volume();
        final boolean confirmHammer = next.low() > potentialHammer.close();

        return threeBackFall &&
                twoBackFall &&
                oneBackFall &&
                lowestCurrentLow &&
                lowAtLeastTwiceSizeOfBody &&
                volumeIncreased &&
                confirmHammer;
    }

    /**
     * The shooting star is a bearish reversal candlestick indicating a peak or top. It is the exact inverse version
     * of a hammer candle. The star should form after at least three or more subsequent green candles indicating a
     * rising price and demand. Eventually, the buyers lose patience and chase the price to new highs (of the sequence)
     * before realizing they overpaid.
     *
     * The upper shadow (also known as a wick) should generally be twice as large as the body. This indicates the last
     * of the frenzied buyers have entered the stock just as profit takers unload their positions followed by short-sellers
     * pushing the price down to close the candle near or below the open. This in essence, traps the late buyers who chased
     * the price too high. Fear is at the highest point here as the very next candle should close at or under the shooting
     * star candle, which will set off a panic selling spree as late buyers panic to get out and curb losses. The typical
     * short-sell signal forms when the low of the following candlestick price is broken with trail stops at the high of
     * the body or tail of the shooting star candlestick.
     */
    private static boolean isShootingStarCandlestick(final StockIncrementInformation threeBack,
                                                     final StockIncrementInformation twoBack,
                                                     final StockIncrementInformation oneBack,
                                                     final StockIncrementInformation potentialShootingStar,
                                                     final StockIncrementInformation confirmShootingStar) {
        final boolean threeBackRise = threeBack.close() > threeBack.open();
        final boolean twoBackRise = twoBack.close() > twoBack.open();
        final boolean oneBackRise = oneBack.close() > oneBack.open();
        final boolean highestCurrentHigh = potentialShootingStar.high() > oneBack.high() && potentialShootingStar.high() > twoBack.high();

        final float bodySize = Math.abs(potentialShootingStar.open() - potentialShootingStar.close());

        final boolean highAtLeastTwiceSizeOfBody = (potentialShootingStar.close() + (bodySize * 2)) < potentialShootingStar.high();
        final boolean confirmHammer = confirmShootingStar.high() < potentialShootingStar.close();

        return threeBackRise &&
                twoBackRise &&
                oneBackRise &&
                highestCurrentHigh &&
                highAtLeastTwiceSizeOfBody &&
                confirmHammer;
    }

    /**
     * The doji is a reversal pattern that can be either bullish or bearish depending on the context of the preceding candles.
     * The candle has the same (or close to) open and closing price with long shadows. It looks like a cross, but it can also
     * have a very tiny body. A doji is a sign of indecision but also a proverbial line in the sand. Since the doji is typically
     * a reversal candle, the direction of the preceding candles can give an early indication of which way the reversal will go.
     *
     * If the preceding candles are bullish before forming the doji, the next candle close under the body low triggers a
     * sell/short-sell signal on the break of the doji candlestick lows with trail stops above the doji highs.
     *
     * If the preceding candles are bearish then the doji candlestick will likely form a bullish reversal. Long triggers form
     * above the body or candlestick high with a trail stop under the low of the doji.
     */
    private static boolean isBullishDojiCandlestick(final StockIncrementInformation threeBack,
                                                    final StockIncrementInformation twoBack,
                                                    final StockIncrementInformation oneBack,
                                                    final StockIncrementInformation potentialDoji,
                                                    final StockIncrementInformation next) {
        final boolean threeBackRise = threeBack.close() > threeBack.open();
        final boolean twoBackRise = twoBack.close() > twoBack.open();
        final boolean oneBackRise = oneBack.close() > oneBack.open();

        final boolean isNoBodyOrVerysmallbody = Math.abs(potentialDoji.close() - potentialDoji.open()) < 2;
        final float bodySize = Math.abs(potentialDoji.open() - potentialDoji.close());
        // wick and tail are at least 8 times the body size away from the body
        final boolean longWickAndTail = potentialDoji.high() > (potentialDoji.close() + (bodySize * 8)) && potentialDoji.low() < (potentialDoji.close() - (bodySize * 8));

        final boolean confirmDoji = next.close() < potentialDoji.close();

        return threeBackRise &&
                twoBackRise &&
                oneBackRise &&
                isNoBodyOrVerysmallbody &&
                longWickAndTail &&
                confirmDoji;
    }

    /**
     * See: isBullishDojiCandlestick
     */
    private static boolean isBearishDojiCandlestick(final StockIncrementInformation threeBack,
                                                    final StockIncrementInformation twoBack,
                                                    final StockIncrementInformation oneBack,
                                                    final StockIncrementInformation potentialDoji,
                                                    final StockIncrementInformation next) {
        final boolean threeBackFall = threeBack.close() < threeBack.open();
        final boolean twoBackFall = twoBack.close() < twoBack.open();
        final boolean oneBackFall = oneBack.close() < oneBack.open();

        final boolean isNoBodyOrVerysmallbody = Math.abs(potentialDoji.close() - potentialDoji.open()) < 2;
        final float bodySize = Math.abs(potentialDoji.open() - potentialDoji.close());
        // wick and tail are at least 8 times the body size away from the body
        final boolean longWickAndTail = potentialDoji.high() > (potentialDoji.close() + (bodySize * 8)) && potentialDoji.low() < (potentialDoji.close() - (bodySize * 8));

        final boolean confirmDoji = next.open() > potentialDoji.open();

        return threeBackFall &&
                twoBackFall &&
                oneBackFall &&
                isNoBodyOrVerysmallbody &&
                longWickAndTail &&
                confirmDoji;
    }

    /**
     * A bullish engulfing candlestick is a large bodied green candle that completely engulfs the full range of the preceding red
     * candle. The larger the body, the more extreme the reversal becomes. The body should completely engulf the preceding red
     * candle body.
     *
     * The most effective bullish engulfing candlesticks form at the tail end of a downtrend to trigger a sharp reversal bounce
     * that overwhelms the short-sellers causing a panic short covering buying frenzy. This motivates bargain hunters to come off
     * the fence further adding to the buying pressure. Bullish engulfing candles are potential reversal signals on downtrends and
     * continuation signals on uptrends when they form after a shallow reversion pullback. The volume should spike to at least double
     * the average when bullish engulfing candles form to be most effective. The buy trigger forms when the next candlestick exceeds
     * the high of the bullish engulfing candlestick.
     */
    private static boolean isBullishEngulfingCandlestick(final StockIncrementInformation twoBack,
                                                         final StockIncrementInformation oneBack,
                                                         final StockIncrementInformation potentialEngulfing,
                                                         final StockIncrementInformation next,
                                                         final StockIncrementInformation confirmEngulf) {
        final boolean twoBackRise = twoBack.close() > twoBack.open();
        final boolean oneBackRise = oneBack.close() > oneBack.open();

        final boolean isEngulfing = next.close() > potentialEngulfing.open() && next.open() < potentialEngulfing.close();

        final boolean confirm = confirmEngulf.close() > next.high();
        return twoBackRise &&
                oneBackRise &&
                isEngulfing &&
                confirm;
    }

    /**
     * Inverse of bullish engulfing. @See {isBullishEngulfingCandlestick}
     *
     * The preceding green candle keeps unassuming buyers optimism, as it should be trading near the top of an up trend. The bearish
     * engulfing candle will actually open up higher giving longs hope for another climb as it initially indicates more bullish sentiment.
     * However, the sellers come in very strong and extreme fashion driving down the price through the opening level, which starts to stir
     * some concerns with the longs. The selling intensifies as the price falls through the low of the prior close, which then starts to
     * trigger some more panic selling as the majority of buyers from the prior day are now underwater on their shares. The selling
     * intensifies into the candle close as almost every buyer from the prior close is now holding losses. The magnitude of the reversal
     * is dramatic. The bearish engulfing candle is reversal candle when it forms on uptrends as it triggers more sellers the next day and
     * so forth as the trend starts to reverse into a breakdown. The short-sell trigger forms when the next candlestick exceeds the low of
     * the bullish engulfing candlestick. On existing downtrends, the bearish engulfing may form on a reversion bounce thereby resuming the
     * downtrends at an accelerated pace due to the new buyers that got trapped on the bounce. As with all candlestick patterns, it is
     * important to observe the volume especially on engulfing candles. The volume should be at least two or more times larger than the
     * average daily trading volume to have the most impact. Algorithm programs are notorious for painting the tape at the end of the day
     * with a mis-tick to close out with a fake engulfing candle to trap the bears.
     */
    private static boolean isBearishEngulfingCandlestick(final StockIncrementInformation threeBack,
                                                         final StockIncrementInformation twoBack,
                                                         final StockIncrementInformation oneBack,
                                                         final StockIncrementInformation potentialEngulfing,
                                                         final StockIncrementInformation next) {
        final boolean threeBackFall = threeBack.close() < threeBack.open();
        final boolean twoBackFall = twoBack.close() < twoBack.open();
        final boolean oneBackFall = oneBack.close() < oneBack.open();

        final boolean isEngulfing = next.open() < potentialEngulfing.close() && next.close() > potentialEngulfing.open();

        return threeBackFall &&
                twoBackFall &&
                oneBackFall &&
                isEngulfing;
    }

    /**
     * A bullish harami candle is like a backwards version of the bearish engulfing candlestick pattern where the large body engulfing
     * candle actually precedes the smaller harami candle. The preceding engulfing red candle should be a capitulation large body
     * candlestick that makes the lowest low point of the sequence indicating a capitulation sell-off preceding the harami candle which
     * should trading well within the range of the engulfing candle. The subtleness of the small body keeps the short-sellers in a
     * complacent mode as they assume the stock will drop again, but instead it stabilizes before forming a reversal bounce that takes the
     * short-seller by surprise as the stock reverses back up.
     *
     * The harami is a subtle clue that often keeps sellers complacent until the trend slowly reverses. It is not as intimidating or
     * dramatic as the bullish engulfing candle. The subtleness of the bullish harami candlestick is what makes it very dangerous for
     * short-sellers as the reversal happens gradually and then accelerates quickly. A buy long trigger forms when the next candle rises
     * through the high of the prior engulfing candle and stops can be placed under the lows of the harami candle.
     */
    private static boolean isBullishHaramiCandlestick() {
        return false;
    }

    /**
     * The bearish harami is the inverted version of the bullish harami. The preceding engulfing candle should completely eclipse the range
     * of the harami candle, like David versus Goliath. These form at the top of uptrends as the preceding green candle makes a new high
     * with a large body, before the small harami candlestick forms as buying pressure gradually dissipates. Due to the gradual nature of
     * the buying slow down, the longs assume the pullback is merely a pause before the up trend resumes.
     *
     * As the bearish harami candlestick closes, the next candle closes lower which starts to concern the longs. When the low of the
     * preceding engulfing candle broken, it triggers a panic sell-off as longs run for the exits to curtail further losses. The
     * conventional short-sell triggers form when the low of the engulfing candle is breached and stops can be placed above the high of
     * the harami candlestick.
     */
    private static boolean isBearishHaramiCandlestick() {
        return false;
    }

    /**
     * A hanging man candlestick looks identical to a hammer candlestick but forms at the peak of an uptrend, rather than a bottom of a
     * downtrend. The hanging man has a small body, lower shadow that is larger than the body (preferably twice the size or more) and a
     * very small upper shadow. It is differs from a doji since it has a body that is formed at the top of the range. For some reason, the
     * buyers thwarted a potential shooting star and lifted the candle to close at the upper range of the candle to maintain the bullish
     * sentiment, often times artificially. However, the truth hits when the next candle closes under the hanging man as selling accelerates.
     *
     * Hanging man candles are most effective at the peak of parabolic like price spikes composed of four or more consecutive green candles.
     * Most bearish reversal candles will form on shooting stars and doji candlesticks. Hanging man candles are uncommon as they are a sign
     * of a large buyer that gets trapped trying to support the momentum or an attempt the paint the tape to generate more liquidity to sell into.
     *
     * A hanging man candlestick signals a potential peak of an uptrend as buyers who chased the price look down and wonder why they chased the
     * price so high. It brings to mind the old road runner cartoons where Wile E. Coyote would be chasing the Road Runner and before he knew it,
     * he realized he overstepped the cliff when he looks down, right before he plunges.
     *
     * Short-sell triggers signal when the low of the hanging man candlestick is breached with trail stops placed above the high of the hanging
     * man candle.
     */
    private static boolean isHangingManCandlestick() {
        return false;
    }

    /**
     * This is actually a three candlestick reversal formation where the dark cloud cover candle will actually make a new high of the uptrend
     * sequence as it gaps above the prior candle close, but ends up closing red as sellers step in early. This indicates that longs were
     * anxious to take proactive measure and sell their positions even as new highs were being made. Dark cloud cover candles should have bodies
     * that close below the mid-point of the prior candlestick body. This is what distinguishes from a doji, shooting star or hanging man bearish
     * reversal pattern. The prior candle, dark cloud candle and the following confirmation candle compose the three-candle pattern. The preceding
     * candlesticks should be at least three consecutive green candles leading up the dark cloud cover candlestick.
     *
     * The selling overwhelms and traps the new buyers. If the next candle fails to make a new high (above the dark cloud cover candlestick) then
     * it sets up a short-sell trigger when the low of the third candlestick is breached. This opens up a trap door that indicates panic selling
     * as longs evacuate the burning theater in a frenzied attempt to curtail losses. Short-sell signals trigger when the low of the third candle
     * is breached, with trail stops set above the high of the dark cloud cover candle.
     */
    private static boolean isDarkCloudCoverCandlestick() {
        return false;
    }
}
