package com.heartbeat

import co.paralleluniverse.fibers.Suspendable
import com.heartbeat.HeartContract.Commands.Beat
import com.heartbeat.HeartContract.Companion.contractID
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

/**
 * Creates a Heartbeat state on the ledger.
 *
 * Every Heartbeat state has a scheduled activity to start a flow to consume itself and produce a
 * new Heartbeat state on the ledger after five seconds.
 *
 * By consuming the existing Heartbeat state and creating a new one, a new scheduled activity is
 * created.
 */
@InitiatingFlow
@StartableByRPC
class StartHeartbeatFlow : FlowLogic<Unit>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call() {
        val output = HeartState(ourIdentity)
        val cmd = Command(Beat(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addOutputState(output, contractID)
                .addCommand(cmd)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
    }
}

/**
 * This is the flow that a Heartbeat state runs when it consumes itself to create a new Heartbeat
 * state on the ledger.
 */
@InitiatingFlow
@SchedulableFlow
class HeartbeatFlow(private val stateRef: StateRef) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call(): String {
        val input = serviceHub.toStateAndRef<HeartState>(stateRef)
        val output = HeartState(ourIdentity)
        val beatCmd = Command(Beat(), ourIdentity.owningKey)
        val txBuilder = TransactionBuilder(serviceHub.networkMapCache.notaryIdentities.first())
                .addInputState(input)
                .addOutputState(output, contractID)
                .addCommand(beatCmd)
        val signedTx = serviceHub.signInitialTransaction(txBuilder)
        subFlow(FinalityFlow(signedTx))
        // The sound of a heart.
        return "Lub-dub"
    }
}