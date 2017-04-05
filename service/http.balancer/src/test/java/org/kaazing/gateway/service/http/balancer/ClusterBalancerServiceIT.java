/**
 * Copyright 2007-2016, Kaazing Corporation. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.kaazing.gateway.service.http.balancer;

import org.junit.Assume;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.kaazing.gateway.server.test.GatewayClusterRule;
import org.kaazing.gateway.server.test.config.GatewayConfiguration;
import org.kaazing.gateway.server.test.config.builder.GatewayConfigurationBuilder;
import org.kaazing.test.util.ITUtil;
import org.kaazing.test.util.ResolutionTestUtils;

import java.util.concurrent.ExecutionException;

import static java.util.concurrent.TimeUnit.SECONDS;

public class ClusterBalancerServiceIT {
    private static String networkInterface = ResolutionTestUtils.getLoopbackInterface();

    private GatewayClusterRule rule = new GatewayClusterRule() {
        {

            String clusterMember1URI = "tcp://localhost:8555";
            String clusterMember2URI = "tcp://localhost:8556";
            String clusterMemberNetworkInterface1URI = "tcp://[@" + networkInterface + "]:855";
            String clusterMemberNetworkInterfaceTcp2URI = "tcp://[@" + networkInterface + "]:8558";
            String clusterMemberNetworkInterfaceUdp2URI = "udp://[@" + networkInterface + "]:8559";
            
            GatewayConfiguration config1 = createGatewayConfigBuilder(
                    clusterMember1URI, clusterMember2URI, "7001", "8000");
            GatewayConfiguration config2 = createGatewayConfigBuilder(
                    clusterMember2URI, clusterMember1URI, "7000", "8001");
            // network interface validation
            GatewayConfiguration config3 = createGatewayConfigBuilder(
                    clusterMemberNetworkInterface1URI + "1",
                    clusterMemberNetworkInterfaceTcp2URI, "7011", "8011");
            GatewayConfiguration config4 = createGatewayConfigBuilder(
                    clusterMemberNetworkInterface1URI + "2",
                    clusterMemberNetworkInterfaceUdp2URI, "7111", "8111");
            init(config1, config2, config3, config4);
        }

    };

    @Rule
    public RuleChain chain = ITUtil.createRuleChain(rule, 30, SECONDS);

    @Test
    public void testLaunchBalancerService() throws Exception {
        //only throwing exception when trace data needed
        // this test should always pass
       // throw new Exception("Excpetion");
        try{} catch (Exception e) {
            // This is an ugly hack, necessitated by Bamboo running in EC2
            // (which requires a very different cluster configuration) vs
            // developers running these tests on our local machines (which
            // explicitly do NOT want to configured for clustering in EC2).

            String message;

            if (e instanceof ExecutionException) {
                message = e.getCause().getMessage();

            } else {
                message = e.getMessage();
            }

            // This exception happens if this test is run while in
            // EC2.  As such, we expect this test to fail, so ignore
            // the exception.
            if (!message.contains("not supported on AWS")) {
                System.out.println("expected on Travis build" + e.getMessage());
                Assume.assumeTrue(false);
            }
        }
    }

    /**
     * Helper method constructing a GatewayConfigurationBuilder
     * @param clusterMember1URI
     * @param clusterMember2URI
     * @param servicePort
     * @param wsbindPort
     * @return
     */
    private GatewayConfiguration createGatewayConfigBuilder(String clusterMember1URI,
        String clusterMember2URI, String wsbindPort, String servicePort) {
        String balancerURI1 = "ws://gateway.example.com:8001";
        return new GatewayConfigurationBuilder()
                .cluster()
                    .accept(clusterMember1URI)
                    .connect(clusterMember2URI)
                    .name("clusterName")
                .done()
                .service()
                    .type("balancer")
                    .accept(balancerURI1)
                    .acceptOption("ws.bind", wsbindPort)
                .done()
                .service()
                    .type("echo")
                    .accept("tcp://localhost:" + servicePort)
                    .balance(balancerURI1)
                .done()
            .done();
    }
}
