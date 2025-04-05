package nl.jvandis.teambalance.testdata

import kotlin.random.Random

class Conditional(private val random: Random) {

    /**
     * Determines whether an event occurs based on the given success rate.
     *
     * @param successRate The probability of success, represented as a Double between 0.0 and 1.0.
     * A value of 1.0 indicates a 100% chance of success, while 0.0 represents a 0% chance.
     */
    operator fun invoke(successRate: Double) = random.nextDouble(1.0) < successRate

    /**
     * Invokes one of the provided functions based on the given success rate.
     *
     * @param successRate The probability of success, represented as a Double between 0.0 and 1.0.
     * A value of 1.0 indicates a 100% chance of success, while 0.0 represents a 0% chance.
     * @param onTrue A lambda that is executed if the success condition is met.
     * @param onFalse A lambda that is executed if the success condition is not met.
     * @return The result of invoking either the onTrue or onFalse lambda.
     */
    operator fun <T> invoke(successRate: Double, onTrue: () -> T, onFalse: () -> T) =
        if (invoke(successRate)) onTrue() else onFalse()

    /**
     * Invokes the provided `onTrue` lambda if the success condition is met, otherwise returns null.
     *
     * @param successRate The probability of success, represented as a Double between 0.0 and 1.0.
     * A value of 1.0 indicates a 100% chance of success, while 0.0 represents a 0% chance.
     * @param onTrue A lambda that is executed if the success condition is met.
     * @return The result of the `onTrue` lambda if the success condition is met, otherwise null.
     */
    operator fun <T> invoke(successRate: Double, onTrue: () -> T) = invoke(successRate, onTrue) { null }

    /**
     * Determines whether a conditional event should occur, bases on a random number between and the given argument.
     * One would say, this should succeed, 1 in a `succeedsOneIn` times.
     *
     * @param succeedsOneIn the one in x change that this method returns true. 1 would return true always,
     * 2 about 50% of the time. 10^10 would hardly ever return true
     */
    operator fun invoke(succeedsOneIn: Int): Boolean = invoke(1.0 / succeedsOneIn)
}
