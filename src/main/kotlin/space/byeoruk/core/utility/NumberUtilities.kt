package space.byeoruk.core.utility

import java.util.concurrent.ThreadLocalRandom
import kotlin.random.Random

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
     * 범위 값을 INT 배열로 변환
     *
     * @param value 범위 값 문자열 (예: `"1:4"`)
     * @param fallbackValue 첫 번째 매개변수 파싱 실패 시 대처될 값 (기본값: 1)
     * @return 범위 값 숫자 배열 (예: `[1, 4]`)
     */
    fun getRangeInt(value: String, fallbackValue: Int = 1): List<Int> {
        if(value.contains(":"))
             return value.split(":").map { it.toInt() }

        val n = value.toIntOrNull() ?: fallbackValue
        return listOf(n, n)
    }

    /**
     * 범위 값을 Double 배열로 변환
     *
     * @param value 범위 값 문자열 (예: `"1.1:1.5"`)
     * @param fallbackValue 첫 번째 매개변수 파싱 실패 시 대처될 값 (기본값: 1.0)
     * @return 범위 값 Double 배열 (예: `[1.1, 1.5]`)
     */
    fun getRangeDouble(value: String, fallbackValue: Double = 1.0): List<Double> {
        if(value.contains(":"))
            return value.split(":").map { it.toDouble() }

        val n = value.toDoubleOrNull() ?: fallbackValue
        return listOf(n, n)
    }

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