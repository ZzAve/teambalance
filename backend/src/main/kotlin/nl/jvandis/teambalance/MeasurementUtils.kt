package nl.jvandis.teambalance

import kotlin.math.pow
import kotlin.math.round
import kotlin.math.roundToInt
import kotlin.math.sqrt
import kotlin.system.measureTimeMillis

private val log = loggerFor(" nl.jvandis.teambalance.MeasurementUtils")

fun measureTiming(
    repetitions: Int = 50,
    func: () -> Unit
) {
    val timings = mutableListOf<Long>()
    repeat(repetitions) {
        val measureTimeMillis = measureTimeMillis {
            func()
        }
        timings.add(measureTimeMillis)

        if (it % ((repetitions / 10.0).roundToInt()) == 0) {
            log.info("Processing iteration $it of $repetitions ...")
        }
    }

    val mean = timings.average()
    var standardDeviation = 0.0
    for (num in timings) {
        standardDeviation += (num - mean).pow(2.0)
    }
    standardDeviation = sqrt(standardDeviation / timings.size)

    log.info(
        """Results:  
Processing times: 
            sample size: ${timings.size}
            mean μ: $mean ms
            std δ: ${round(standardDeviation)} ms
            
| Processing times ||  
|---|---:|
| sample size:  |  ${timings.size}|
| mean μ:       |  $mean ms|
| std δ:        |  ${round(standardDeviation)} ms|
                        """
            .trimMargin()
    )
}
