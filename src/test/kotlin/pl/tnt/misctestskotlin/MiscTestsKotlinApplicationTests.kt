package pl.tnt.misctestskotlin

import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.channels.produce
import kotlinx.coroutines.channels.ticker
import org.junit.jupiter.api.Test
import java.lang.System.currentTimeMillis

@ExperimentalCoroutinesApi
class MiscTestsKotlinApplicationTests {


    @Test
    fun coroutines1() {

        suspend fun extracted() {
            delay(1000L) // non-blocking delay for 1 second (default time unit is ms)
            println("World!")
        }

        fun main() = runBlocking { // this: CoroutineScope
            launch { // launch a new coroutine and continue
                extracted() // print after delay
            }
            println("Hello") // main coroutine continues while a previous one is delayed
        }
        main()
    }

    @Test
    fun coroutines2() {
        val start = currentTimeMillis();
        fun CoroutineScope.generateInt(): ReceiveChannel<Int> {
            return produce {
                var i = 0;
                while (true) {

                    delay(i * 1000L)
                    send(i++)
                }
            }
        }

        fun CoroutineScope.squareInt(input: ReceiveChannel<Int>): ReceiveChannel<Int> = produce {
            for (x in input) {
                send(x * x)
            }
        }

        runBlocking {
            val output = squareInt(generateInt())

            repeat(4) {
                println("square: ${output.receive()}, time: ${(currentTimeMillis() - start)}")
            }

            coroutineContext.cancelChildren()
        }

    }

    @Test
    fun coroutines3() {
        fun CoroutineScope.numbersFrom(start: Int) = produce<Int> {
            var x = start
            while (true) send(x++) // infinite stream of integers from start
        }

        fun CoroutineScope.filter(numbers: ReceiveChannel<Int>, prime: Int) = produce<Int> {
            for (x in numbers) {
                println("Checking $x against $prime")
                if (x % prime != 0) {
                    println("sending $x")
                    send(x)
                }
            }
        }

        runBlocking {
            var cur = numbersFrom(2)
            repeat(5) { i ->
                val prime = cur.receive()
                println("prime: $prime")
                cur = filter(cur, prime)
            }
            coroutineContext.cancelChildren()
        }


    }

    @Test
    fun coroutines6() {
        fun numbersFrom(start: Int) = iterator {
            var x = start
            while (true) {
                println("yielding $x")
                yield(x++)
            } // infinite stream of integers from start
        }

        fun filter(numbers: Iterator<Int>, prime: Int) = iterator {
            println("Creating filter for prime $prime")
            for (x in numbers) {
                println("Checking $x against $prime")
                if (x % prime != 0) {
                    println("sending $x")
                    yield(x)
                }
            }
        }

        var cur = numbersFrom(2) // Start with an infinite sequence from 2
        repeat(5) {
            val prime = cur.next() // Get the next prime
            println("prime: $prime")
            cur = filter(cur, prime) // Filter out multiples of the current prime
        }
    }

    @Test
    fun coroutines4() {
        data class Ball(var hits: Int)

        suspend fun player(name: String, table: Channel<Ball>) {
            for (ball in table) { // receive the ball in a loop
                ball.hits++
                println("$name $ball")
                delay(300) // wait a bit
                table.send(ball) // send the ball back
            }
        }

        runBlocking {
            val table = Channel<Ball>() // a shared table
            launch { player("ping", table) }
            launch { player("pong", table) }
            table.send(Ball(0)) // serve the ball
            delay(1000) // delay 1 second
            coroutineContext.cancelChildren() // game over, cancel them
        }
    }

    @Test
    fun foo2() = runBlocking<Unit> {
        val tickerChannel = ticker(delayMillis = 200, initialDelayMillis = 0) // create a ticker channel
        var nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Initial element is available immediately: $nextElement") // no initial delay

        nextElement = withTimeoutOrNull(100) { tickerChannel.receive() } // all subsequent elements have 200ms delay
        println("Next element is not ready in 100 ms: $nextElement")

        nextElement = withTimeoutOrNull(120) { tickerChannel.receive() }
        println("Next element is ready in 200 ms: $nextElement")

        // Emulate large consumption delays
        println("Consumer pauses for 300ms")
        delay(300)
        // Next element is available immediately
        nextElement = withTimeoutOrNull(1) { tickerChannel.receive() }
        println("Next element is available immediately after large consumer delay: $nextElement")
        // Note that the pause between `receive` calls is taken into account and next element arrives faster
        nextElement = withTimeoutOrNull(120) { tickerChannel.receive() }
        println("Next element is ready in 100ms after consumer pause in 300ms: $nextElement")

        tickerChannel.cancel() // indicate that no more elements are needed
    }


}
