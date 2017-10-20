package com.heartbeat

import net.corda.core.utilities.getOrThrow
import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test

// TODO: Create some flow tests.

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
    fun `dummy test`() {
        val flow = StartHeartbeatFlow()
        val future = a.services.startFlow(flow).resultFuture
        future.getOrThrow()
    }
}