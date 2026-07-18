package com.juul.kable.server

import kotlin.uuid.Uuid

/** Identifies an attribute (characteristic or descriptor) within a [ServerProfile]. */
internal sealed class AttributeKey {

    abstract val serviceUuid: Uuid
    abstract val characteristicUuid: Uuid

    data class Characteristic(
        override val serviceUuid: Uuid,
        override val characteristicUuid: Uuid,
    ) : AttributeKey()

    data class Descriptor(
        override val serviceUuid: Uuid,
        override val characteristicUuid: Uuid,
        val descriptorUuid: Uuid,
    ) : AttributeKey()
}

/**
 * Platform-agnostic representation of an event (usually a request from a remote [Central]) received
 * by a [ServerEngine], processed by the [RequestDispatcher].
 *
 * Requests carry `respond`/`fail` functions (capturing the platform specific response mechanics)
 * that must be invoked (at most once, in aggregate) to answer the request.
 */
internal sealed class InboundRequest {

    /** A remote [central] is reading the value of [attribute] (starting at [offset]). */
    class Read(
        val central: Central,
        val attribute: AttributeKey,
        val offset: Int,
        val respond: (value: ByteArray) -> Unit,
        val fail: (error: AttError) -> Unit,
    ) : InboundRequest()

    /**
     * A remote [central] is writing [value] to [attribute].
     *
     * When [prepared], the write is part of a long/queued write transaction ([value] is a fragment
     * at [offset]) that is later committed (or aborted) via [ExecuteWrite].
     *
     * [respond]/[fail] are `null` when the remote [central] did not request a response (i.e.
     * write-without-response).
     */
    class Write(
        val central: Central,
        val attribute: AttributeKey,
        val value: ByteArray,
        val offset: Int,
        val prepared: Boolean,
        val respond: (() -> Unit)?,
        val fail: ((error: AttError) -> Unit)?,
    ) : InboundRequest()

    /** A remote [central] is committing ([commit] is `true`) or aborting its queued [Write]s. */
    class ExecuteWrite(
        val central: Central,
        val commit: Boolean,
        val respond: () -> Unit,
        val fail: (error: AttError) -> Unit,
    ) : InboundRequest()

    /**
     * A remote [central] subscribed to [attribute] (only emitted by platforms that manage the CCCD
     * themselves — e.g. Apple; on other platforms, subscriptions are derived from CCCD [Write]s).
     */
    class Subscribe(
        val central: Central,
        val attribute: AttributeKey.Characteristic,
    ) : InboundRequest()

    /** A remote [central] unsubscribed from [attribute] (see [Subscribe]). */
    class Unsubscribe(
        val central: Central,
        val attribute: AttributeKey.Characteristic,
    ) : InboundRequest()

    /** A remote [central] connected (only emitted by platforms that provide connection events). */
    class CentralConnected(
        val central: Central,
    ) : InboundRequest()

    /** A remote [central] disconnected (only emitted by platforms that provide connection events). */
    class CentralDisconnected(
        val central: Central,
    ) : InboundRequest()
}
