/*
 * Copyright Debezium Authors.
 *
 * Licensed under the Apache Software License version 2.0, available at http://www.apache.org/licenses/LICENSE-2.0
 */

package io.quarkus.debezium.engine;

import java.io.DataInput;
import java.io.DataOutput;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jgroups.JChannel;
import org.jgroups.protocols.raft.Role;
import org.jgroups.raft.RaftHandle;
import org.jgroups.raft.StateMachine;

import io.debezium.runtime.DebeziumConnectorRegistry;
import io.debezium.runtime.configuration.DebeziumEngineConfiguration;
import io.quarkus.arc.runtime.BeanContainer;
import io.quarkus.runtime.RuntimeValue;
import io.quarkus.runtime.ShutdownContext;
import io.quarkus.runtime.annotations.Recorder;

@Recorder
public class DebeziumRecorder {

    private static final String CLUSTER = "debezium-cluster";
    private static final String CFG = "raft.xml";
    private final RuntimeValue<DebeziumEngineConfiguration> configuration;

    public DebeziumRecorder(RuntimeValue<DebeziumEngineConfiguration> configuration) {
        this.configuration = configuration;
    }

    public void startEngine(ShutdownContext context, BeanContainer container) {

        ExecutorService executor = Executors.newFixedThreadPool(1);

        executor.submit(() -> {
            DebeziumConnectorRegistry debeziumConnectorRegistry = container.beanInstance(DebeziumConnectorRegistry.class);

            System.setProperty("jgroups.bind_addr", "127.0.0.1");
            System.setProperty("jgroups.bind_port", configuration.getValue().debeziumPort().get());
            System.setProperty("raft.id", configuration.getValue().debeziumRaftId().get());

            try (JChannel jChannel = new JChannel(CFG)) {
                jChannel.setName(configuration.getValue().debeziumRaftId().get());
                RaftHandle raftHandle = new RaftHandle(jChannel, new StateMachine() {
                    @Override
                    public byte[] apply(byte[] bytes, int i, int i1, boolean b) throws Exception {
                        return new byte[0];
                    }

                    @Override
                    public void readContentFrom(DataInput dataInput) throws Exception {

                    }

                    @Override
                    public void writeContentTo(DataOutput dataOutput) throws Exception {

                    }
                }).raftId(configuration.getValue().debeziumRaftId().get());

                raftHandle.addRoleListener(role -> {
                    if (role == Role.Leader) {
                        debeziumConnectorRegistry
                                .engines()
                                .stream()
                                .map(debezium -> new DebeziumRunner(DebeziumThreadHandler.getThreadFactory(debezium), debezium))
                                .forEach(runner -> {
                                    runner.start();
                                    context.addShutdownTask(runner::shutdown);
                                });
                    }
                });

                jChannel.connect(CLUSTER);

                while (jChannel.isConnected()) {
                    // ignore
                }
            }
            catch (Exception e) {
                throw new RuntimeException(e);
            }
        });

    }

}
