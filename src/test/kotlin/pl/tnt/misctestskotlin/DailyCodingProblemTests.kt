package pl.tnt.misctestskotlin

import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

class DailyCodingProblemTests {

    @Test
    fun problem68() {

        data class P(val i: Int, val j: Int)

        fun isPair(p1: P, p2: P): Boolean {
            return abs(p1.i - p2.i) == abs(p1.j - p2.j)
        }

        val bishops: List<P> = listOf(P(0, 0), P(1, 2), P(2, 2), P(4, 0))

        var uniquePairs = 0

        for (i in bishops.indices) {
            for (j in (i + 1)..<bishops.size) {

                if (isPair(bishops[i], bishops[j])) {
                    uniquePairs++
                }
            }
        }

        println(uniquePairs)
    }

    @Test
    fun problem69() {
        val nums = arrayOf(2, 6, 2, 4, 2, 8, 8)

        val cache = Array(nums.size) { 0 }

        cache[2] = nums[0] * nums[1] * nums[2]

        for (i in 3..<cache.size) {
            val el = nums[i]

            val x = cache[i - 1] * el

            for (j in 0..i) {
                cache[i] = max(cache[i], x / nums[j])
            }
        }

        println(cache.toList())
    }


    @Test
    fun problem70() {

        //1124
        fun perfectNumber(n: Int): Int {
            val result: MutableList<Int> = mutableListOf()

            var divided = n
            var sum = 0
            while (divided > 0) {
                val digit = divided % 10
                sum += digit
                result += digit
                divided /= 10
            }

            return when {
                sum == 10 -> n
                sum > 10 -> -1
                else -> {
                    var resultN = 10 - sum

                    result.forEachIndexed { idx, v ->
                        resultN += v * 10.0.pow(idx + 1.0).toInt()
                    }

                    return resultN
                }
            }
        }

        println(perfectNumber(123))
        println(perfectNumber(413))
        println(perfectNumber(121))
        println(perfectNumber(11222))
    }
}