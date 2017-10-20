package com.heartbeat

import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.transactions.LedgerTransaction

open class HeartContract : Contract {
    companion object {
        val contractID = "com.heartbeat.HeartContract"
    }

    override fun verify(tx: LedgerTransaction) {

    }

    interface Commands : CommandData {
        class Beat : Commands
    }
}