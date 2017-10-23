package com.heartbeat

import net.corda.client.rpc.notUsed
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedNode<MockNode>

    @Before
    fun setup() {
        setCordappPackages("com.heartbeat")
        network = MockNetwork(threadPerNode = true)
        val nodes = network.createSomeNodes(1)
        a = nodes.partyNodes[0]
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unsetCordappPackages()
    }

    @Test
    fun `heartbeat occurs every second`() {
        val flow = StartHeartbeatFlow()
        a.services.startFlow(flow).resultFuture

        val enoughTimeForFiveScheduledTxs: Long = 5500
        Thread.sleep(enoughTimeForFiveScheduledTxs)

        val recordedTxs = a.database.transaction {
            val (recordedTxs, futureTxs) = a.services.validatedTransactions.track()
            futureTxs.notUsed()
            recordedTxs
        }

        val originalTxPlusFiveScheduledTxs = 6
        assertEquals(originalTxPlusFiveScheduledTxs, recordedTxs.size)
    }
}