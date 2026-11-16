package com.example.util

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Pure-Kotlin ISO/IEC 18004 QR Code encoder (Byte mode, ECC Level M, Versions 1..6)
 * so QR menus and UPI payment QRs render natively and scannably with zero external dependency.
 */
object QrEncoder {
    private val capacityTableM = intArrayOf(14, 26, 42, 62, 84, 106)
    private val totalCodewords = intArrayOf(26, 44, 70, 100, 134, 172)
    private val ecCodewordsPerBlock = intArrayOf(10, 16, 26, 18, 24, 16)
    private val blocksInGroup1 = intArrayOf(1, 1, 1, 2, 2, 4)

    fun encode(data: String): Array<BooleanArray> {
        val bytes = data.toByteArray(Charsets.UTF_8)
        val version = (1..6).firstOrNull { bytes.size + 3 <= capacityTableM[it - 1] } ?: 6
        val maxBytes = capacityTableM[version - 1]
        val payload = if (bytes.size > maxBytes - 3) bytes.copyOf(maxBytes - 3) else bytes

        val dataCodewords = totalCodewords[version - 1] - ecCodewordsPerBlock[version - 1] * blocksInGroup1[version - 1]
        val bits = ArrayList<Int>(dataCodewords * 8)

        fun appendBits(value: Int, count: Int) {
            for (i in (count - 1) downTo 0) {
                bits.add((value ushr i) and 1)
            }
        }

        // Byte mode indicator (0100) + 8-bit character count
        appendBits(0b0100, 4)
        appendBits(payload.size, 8)
        for (b in payload) {
            appendBits(b.toInt() and 0xFF, 8)
        }
        val maxBits = dataCodewords * 8
        val terminator = minOf(4, maxBits - bits.size)
        repeat(terminator) { bits.add(0) }
        while (bits.size % 8 != 0) bits.add(0)

        var padIndex = 0
        while (bits.size < maxBits) {
            appendBits(if (padIndex % 2 == 0) 0xEC else 0x11, 8)
            padIndex++
        }

        val dataBytes = IntArray(dataCodewords) { idx ->
            var v = 0
            for (b in 0 until 8) {
                v = (v shl 1) or bits[idx * 8 + b]
            }
            v
        }

        val numBlocks = blocksInGroup1[version - 1]
        val blockSize = dataCodewords / numBlocks
        val ecSize = ecCodewordsPerBlock[version - 1]
        val dataBlocks = Array(numBlocks) { i ->
            dataBytes.copyOfRange(i * blockSize, (i + 1) * blockSize)
        }
        val ecBlocks = Array(numBlocks) { i ->
            computeReedSolomon(dataBlocks[i], ecSize)
        }

        val finalBits = ArrayList<Int>()
        for (c in 0 until blockSize) {
            for (b in 0 until numBlocks) {
                val byteVal = dataBlocks[b][c]
                for (bit in 7 downTo 0) finalBits.add((byteVal ushr bit) and 1)
            }
        }
        for (c in 0 until ecSize) {
            for (b in 0 until numBlocks) {
                val byteVal = ecBlocks[b][c]
                for (bit in 7 downTo 0) finalBits.add((byteVal ushr bit) and 1)
            }
        }

        val size = version * 4 + 17
        val matrix = Array(size) { BooleanArray(size) }
        val reserved = Array(size) { BooleanArray(size) }

        fun placeFinder(r0: Int, c0: Int) {
            for (dr in -1..7) {
                for (dc in -1..7) {
                    val r = r0 + dr
                    val c = c0 + dc
                    if (r in 0 until size && c in 0 until size) {
                        val inOuter = dr in 0..6 && dc in 0..6
                        val onBorder = dr == 0 || dr == 6 || dc == 0 || dc == 6
                        val inInner = dr in 2..4 && dc in 2..4
                        matrix[r][c] = inOuter && (onBorder || inInner)
                        reserved[r][c] = true
                    }
                }
            }
        }

        placeFinder(0, 0)
        placeFinder(0, size - 7)
        placeFinder(size - 7, 0)

        // Timing patterns
        for (i in 8 until size - 8) {
            matrix[6][i] = i % 2 == 0
            reserved[6][i] = true
            matrix[i][6] = i % 2 == 0
            reserved[i][6] = true
        }

        // Alignment pattern for versions >= 2
        if (version >= 2) {
            val pos = size - 7
            for (dr in -2..2) {
                for (dc in -2..2) {
                    val r = pos + dr
                    val c = pos + dc
                    matrix[r][c] = kotlin.math.max(kotlin.math.abs(dr), kotlin.math.abs(dc)) != 1
                    reserved[r][c] = true
                }
            }
        }

        // Dark module & reserve format info areas
        matrix[size - 8][8] = true
        reserved[size - 8][8] = true
        for (i in 0..8) {
            if (i != 6) {
                reserved[8][i] = true
                reserved[i][8] = true
            }
        }
        for (i in 0..7) {
            reserved[8][size - 1 - i] = true
            reserved[size - 1 - i][8] = true
        }

        // Place data bits in zig-zag with mask 0 ((r + c) % 2 == 0)
        var bitIdx = 0
        var col = size - 1
        var upward = true
        while (col > 0) {
            if (col == 6) col--
            for (step in 0 until size) {
                val r = if (upward) size - 1 - step else step
                for (dc in 0..1) {
                    val c = col - dc
                    if (!reserved[r][c]) {
                        val bit = if (bitIdx < finalBits.size) finalBits[bitIdx++] == 1 else false
                        val masked = if ((r + c) % 2 == 0) !bit else bit
                        matrix[r][c] = masked
                    }
                }
            }
            upward = !upward
            col -= 2
        }

        // Format info for ECC M (00) + Mask 0 (000) -> 15-bit BCH with mask 101010000010010 = 0x5412 -> 0x5412 xor 0x0000
        // Format string for ECC=M (00), Mask=0 (000) is 101010000010010
        val formatBits = intArrayOf(1, 0, 1, 0, 1, 0, 0, 0, 0, 0, 1, 0, 0, 1, 0)
        for (i in 0..5) matrix[8][i] = formatBits[i] == 1
        matrix[8][7] = formatBits[6] == 1
        matrix[8][8] = formatBits[7] == 1
        matrix[7][8] = formatBits[8] == 1
        for (i in 9..14) matrix[14 - i][8] = formatBits[i] == 1

        for (i in 0..7) matrix[size - 1 - i][8] = formatBits[i] == 1
        for (i in 8..14) matrix[8][size - 15 + i] = formatBits[i] == 1

        return matrix
    }

    private fun computeReedSolomon(data: IntArray, ecLen: Int): IntArray {
        val gfExp = IntArray(512)
        val gfLog = IntArray(256)
        var x = 1
        for (i in 0 until 255) {
            gfExp[i] = x
            gfLog[x] = i
            x = x shl 1
            if (x and 0x100 != 0) x = x xor 0x11D
        }
        for (i in 255 until 512) gfExp[i] = gfExp[i - 255]

        fun gfMul(a: Int, b: Int): Int =
            if (a == 0 || b == 0) 0 else gfExp[gfLog[a] + gfLog[b]]

        var gen = intArrayOf(1)
        for (i in 0 until ecLen) {
            val next = IntArray(gen.size + 1)
            for (j in gen.indices) {
                next[j] = next[j] xor gen[j]
                next[j + 1] = next[j + 1] xor gfMul(gen[j], gfExp[i])
            }
            gen = next
        }

        val res = IntArray(data.size + ecLen)
        data.copyInto(res)
        for (i in data.indices) {
            val factor = res[i]
            if (factor != 0) {
                for (j in gen.indices) {
                    res[i + j] = res[i + j] xor gfMul(gen[j], factor)
                }
            }
        }
        return res.copyOfRange(data.size, res.size)
    }
}

@Composable
fun QrCodeCard(
    content: String,
    modifier: Modifier = Modifier,
    size: Dp = 180.dp,
    foreground: Color = Color(0xFF1A1412),
    background: Color = Color.White
) {
    val matrix = remember(content) { QrEncoder.encode(content) }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .padding(12.dp)
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val dim = matrix.size
            val cellSize = this.size.width / dim.toFloat()
            for (r in 0 until dim) {
                for (c in 0 until dim) {
                    if (matrix[r][c]) {
                        drawRect(
                            color = foreground,
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize + 0.5f, cellSize + 0.5f)
                        )
                    }
                }
            }
        }
    }
}
