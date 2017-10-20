package com.heartbeat

import co.paralleluniverse.fibers.Suspendable
import com.heartbeat.HeartContract.Commands.Beat
import com.heartbeat.HeartContract.Companion.contractID
import net.corda.core.contracts.Command
import net.corda.core.contracts.StateRef
import net.corda.core.flows.*
import net.corda.core.transactions.TransactionBuilder
import net.corda.core.utilities.ProgressTracker

import net.corda.core.utilities.ProgressTracker.Step

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

@InitiatingFlow
@SchedulableFlow
class HeartbeatFlow(private val stateRef: StateRef) : FlowLogic<String>() {
    companion object {
        object TOKEN_STEP : Step("Generating transaction based on new IOU.")

        fun tracker() = ProgressTracker(TOKEN_STEP)
    }

    override val progressTracker = tracker()

    @Suspendable
    override fun call(): String {
        progressTracker.currentStep = TOKEN_STEP
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