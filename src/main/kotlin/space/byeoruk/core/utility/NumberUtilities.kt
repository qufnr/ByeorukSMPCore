package space.byeoruk.core.utility

import java.time.Duration
import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds

object NumberUtilities {
    /**
     * 확률에 들었는지 확인
     *
     * @param chance 확률
     *
     * @return 확률에 들었다면 true 아니면 false 반환
     */
    fun isInChance(chance: Double): Boolean =
        ThreadLocalRandom.current().nextDouble() * 100.0 < chance

    /**
     * min, max 무작위 숫자
     *
     * @param min 최소 값
     * @param max 최대 값
     *
     * @return 무작위 숫자
     */
    fun getRandomInt(min: Int, max: Int): Int =
        Random.nextInt(min, max + 1)

    /**
     * 초 단위 숫자를 분:초 형태로 포멧
     *
     * @param s 초 단위 숫자
     * @return 분:초(mm:ss) 포멧 문자열
     */
    fun formatSeconds(s: Int): String {
        val minutes = s / 60
        val seconds = s % 60

        return "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
    }
}