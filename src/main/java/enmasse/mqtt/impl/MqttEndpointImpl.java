/*
 * Copyright 2016 Red Hat Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package enmasse.mqtt.impl;

import enmasse.mqtt.MqttAuth;
import enmasse.mqtt.MqttEndpoint;
import enmasse.mqtt.MqttWill;
import io.netty.handler.codec.mqtt.*;
import io.vertx.core.Handler;
import io.vertx.core.net.impl.ConnectionBase;

/**
 * Represents an MQTT endpoint
 */
public class MqttEndpointImpl implements MqttEndpoint {

    private final ConnectionBase conn;

    private final String clientIdentifier;
    private final MqttAuthImpl auth;
    private final MqttWill will;
    private final boolean isCleanSession;

    private Handler<MqttSubscribeMessage> subscribeHandler;

    private boolean closed;

    public MqttEndpointImpl(ConnectionBase conn, String clientIdentifier, MqttAuthImpl auth, MqttWillImpl will, boolean isCleanSession) {
        this.conn = conn;
        this.clientIdentifier = clientIdentifier;
        this.auth = auth;
        this.will = will;
        this.isCleanSession = isCleanSession;
    }

    @Override
    public String clientIdentifier() {
        return this.clientIdentifier;
    }

    @Override
    public MqttAuth auth() {
        return this.auth;
    }

    @Override
    public MqttWill will() {
        return this.will;
    }

    @Override
    public boolean isCleanSession() {
        return this.isCleanSession;
    }

    @Override
    public MqttEndpoint writeConnack(MqttConnectReturnCode connectReturnCode, boolean sessionPresent) {

        MqttFixedHeader mqttFixedHeader =
                new MqttFixedHeader(MqttMessageType.CONNACK, false, MqttQoS.AT_MOST_ONCE, false, 2);
        MqttConnAckVariableHeader mqttConnAckVariableHeader =
                new MqttConnAckVariableHeader(connectReturnCode, sessionPresent);

        MqttMessage connack = MqttMessageFactory.newMessage(mqttFixedHeader, mqttConnAckVariableHeader, null);

        this.write(connack);

        return this;
    }

    @Override
    public MqttEndpoint subscribeHandler(Handler<MqttSubscribeMessage> handler) {

        synchronized (this.conn) {
            this.checkClosed();
            this.subscribeHandler = handler;
            return this;
        }
    }

    public void handleSubscribe(MqttSubscribeMessage msg) {

        synchronized (this.conn) {
            if (this.subscribeHandler != null) {
                this.subscribeHandler.handle(msg);
            }
        }
    }

    @Override
    public void end() {

    }

    @Override
    public void end(MqttMessage mqttMessage) {

    }

    @Override
    public MqttEndpoint drainHandler(Handler<Void> handler) {
        return null;
    }

    @Override
    public MqttEndpoint setWriteQueueMaxSize(int i) {
        return null;
    }

    @Override
    public boolean writeQueueFull() {
        return false;
    }

    @Override
    public MqttEndpoint write(MqttMessage mqttMessage) {

        synchronized (this.conn) {
            this.checkClosed();
            this.conn.writeToChannel(mqttMessage);
            return this;
        }
    }

    @Override
    public MqttEndpoint endHandler(Handler<Void> handler) {
        return null;
    }

    @Override
    public MqttEndpoint resume() {
        return null;
    }

    @Override
    public MqttEndpoint pause() {
        return null;
    }

    @Override
    public MqttEndpoint handler(Handler<MqttMessage> handler) {
        return null;
    }

    @Override
    public MqttEndpoint exceptionHandler(Handler<Throwable> handler) {
        return null;
    }

    /**
     * Check if the MQTT endpoint is closed
     */
    private void checkClosed() {
        if (this.closed) {
            throw new IllegalStateException("MQTT endpoint is closed");
        }
    }
}
