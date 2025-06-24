package com.example.blockchain

import java.security.MessageDigest
import java.time.Instant
import java.util.concurrent.TimeUnit

// 1. Block Class
data class Block(
    val index: Int,
    val timestamp: Long,
    val data: String,
    val previousHash: String,
    var nonce: Int = 0, // Nonce for Proof-of-Work
    var hash: String = "" // The block's own hash
) {
    // Calculates the hash of the block based on its content
    fun calculateHash(): String {
        val input = "$index$timestamp$data$previousHash$nonce"
        return sha256(input)
    }
}

// SHA-256 Hashing Utility
fun sha256(input: String): String {
    val bytes = input.toByteArray(Charsets.UTF_8)
    val md = MessageDigest.getInstance("SHA-256")
    val digest = md.digest(bytes)
    return digest.fold("", { str, it -> str + "%02x".format(it) })
}


// 2. Blockchain Class
class Blockchain(private val difficulty: Int = 4) { // Difficulty: number of leading zeros required for hash

    private val chain: MutableList<Block> = mutableListOf()

    init {
        // Create the genesis block when the blockchain is initialized
        createGenesisBlock()
    }

    private fun createGenesisBlock() {
        val genesisBlock = Block(
            index = 0,
            timestamp = Instant.now().toEpochMilli(),
            data = "Genesis Block",
            previousHash = "0"
        )
        mineBlock(genesisBlock) // Mine the genesis block
        chain.add(genesisBlock)
        println("Genesis Block Mined: ${genesisBlock.hash}\n")
    }

    fun getLatestBlock(): Block = chain.last()

    // 3. Adding a New Block (with Mining)
    fun addBlock(data: String) {
        val previousBlock = getLatestBlock()
        val newBlock = Block(
            index = previousBlock.index + 1,
            timestamp = Instant.now().toEpochMilli(),
            data = data,
            previousHash = previousBlock.hash
        )
        println("Mining new block #${newBlock.index}...")
        val startTime = System.nanoTime()
        mineBlock(newBlock) // Mine the new block
        val endTime = System.nanoTime()
        val durationMillis = TimeUnit.NANOSECONDS.toMillis(endTime - startTime)

        chain.add(newBlock)
        println("Block #${newBlock.index} Mined in ${durationMillis}ms: ${newBlock.hash}\n")
    }

    // 4. Proof-of-Work (Mining)
    private fun mineBlock(block: Block) {
        val targetPrefix = "0".repeat(difficulty)
        while (!block.hash.startsWith(targetPrefix)) {
            block.nonce++
            block.hash = block.calculateHash()
        }
    }

    // 5. Chain Validation
    fun isValidChain(): Boolean {
        for (i in 1 until chain.size) {
            val currentBlock = chain[i]
            val previousBlock = chain[i - 1]

            // Check if current block's hash is correct
            if (currentBlock.hash != currentBlock.calculateHash()) {
                println("Block #${currentBlock.index} hash is invalid!")
                return false
            }

            // Check if previous hash matches the actual previous block's hash
            if (currentBlock.previousHash != previousBlock.hash) {
                println("Block #${currentBlock.index} previous hash is invalid!")
                return false
            }

            // Check Proof-of-Work validity
            val targetPrefix = "0".repeat(difficulty)
            if (!currentBlock.hash.startsWith(targetPrefix)) {
                println("Block #${currentBlock.index} does not meet Proof-of-Work requirement!")
                return false
            }
        }
        return true
    }

    fun printChain() {
        println("--- Blockchain ---")
        chain.forEach { block ->
            println("Index: ${block.index}")
            println("Timestamp: ${Instant.ofEpochMilli(block.timestamp)}")
            println("Data: ${block.data}")
            println("Previous Hash: ${block.previousHash}")
            println("Nonce: ${block.nonce}")
            println("Hash: ${block.hash}")
            println("------------------")
        }
    }
}

// Main function to run the blockchain example
fun main() {
    val myBlockchain = Blockchain(difficulty = 4) // Adjust difficulty as needed (higher = slower mining)

    myBlockchain.addBlock("First transaction: Alice sends 1 BTC to Bob")
    myBlockchain.addBlock("Second transaction: Bob sends 0.5 BTC to Carol")
    myBlockchain.addBlock("Third transaction: Carol buys coffee")

    myBlockchain.printChain()

    println("Blockchain is valid: ${myBlockchain.isValidChain()}")

    // Demonstrate tampering (optional)
    println("\n--- Tampering Demonstration ---")
    // Let's tamper with the data of the second block (index 2)
    val tamperedBlockIndex = 2
    if (myBlockchain.chain.size > tamperedBlockIndex) {
        println("Tampering with block #${myBlockchain.chain[tamperedBlockIndex].index} data...")
        myBlockchain.chain[tamperedBlockIndex].data = "Tampered data: Carol buys tea (instead of coffee)"
        // Recalculate its hash after tampering (but it won't meet PoW unless re-mined, which is the point)
        myBlockchain.chain[tamperedBlockIndex].hash = myBlockchain.chain[tamperedBlockIndex].calculateHash()
        println("Blockchain is valid after tampering: ${myBlockchain.isValidChain()}")
        println("Notice how the validation fails because the hash of the tampered block is now incorrect relative to its data, and its subsequent block's previousHash will no longer match.")
    }
}