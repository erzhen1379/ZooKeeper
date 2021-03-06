/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.zookeeper.client;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.yetus.audience.InterfaceAudience;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Most simple HostProvider, resolves only on instantiation.
 */
@InterfaceAudience.Public
public final class StaticHostProvider implements HostProvider {
    private static final Logger LOG = LoggerFactory
            .getLogger(StaticHostProvider.class);

    private final List<InetSocketAddress> serverAddresses = new ArrayList<InetSocketAddress>(
            5);
    //表示当前正在使用的服务器地址位置
    private int lastIndex = -1;
    //表示循环队列中当前遍历的那个元素位置
    private int currentIndex = -1;

    /**
     * Constructs a SimpleHostSet.
     *
     * @param serverAddresses possibly unresolved ZooKeeper server addresses
     * @throws IllegalArgumentException if serverAddresses is empty or resolves to an empty list
     */
    public StaticHostProvider(Collection<InetSocketAddress> serverAddresses) {
        for (InetSocketAddress address : serverAddresses) {
            try {
                InetAddress ia = address.getAddress();
                InetAddress resolvedAddresses[] = InetAddress.getAllByName((ia != null) ? ia.getHostAddress() :
                        address.getHostName());
                for (InetAddress resolvedAddress : resolvedAddresses) {
                    // If hostName is null but the address is not, we can tell that
                    // the hostName is an literal IP address. Then we can set the host string as the hostname
                    // safely to avoid reverse DNS lookup.
                    // As far as i know, the only way to check if the hostName is null is use toString().
                    // Both the two implementations of InetAddress are final class, so we can trust the return value of
                    // the toString() method.
                    if (resolvedAddress.toString().startsWith("/")
                            && resolvedAddress.getAddress() != null) {
                        //serverAddresses为构建的数组
                        this.serverAddresses.add(
                                new InetSocketAddress(InetAddress.getByAddress(
                                        address.getHostName(),
                                        resolvedAddress.getAddress()),
                                        address.getPort()));
                    } else {
                        this.serverAddresses.add(new InetSocketAddress(resolvedAddress.getHostAddress(), address.getPort()));
                    }
                }
            } catch (UnknownHostException e) {
                LOG.error("Unable to connect to server: {}", address, e);
            }
        }

        if (this.serverAddresses.isEmpty()) {
            throw new IllegalArgumentException(
                    "A HostProvider may not be empty!");
        }
        //shuffle操作，随机打散
        Collections.shuffle(this.serverAddresses);
    }

    public int size() {
        return serverAddresses.size();
    }

    public InetSocketAddress next(long spinDelay) {
        ++currentIndex;
        if (currentIndex == serverAddresses.size()) {
            currentIndex = 0;
        }
        /**
         * 如果发现当前游标的位置和上次已经使用的地址位置已经一样，进行spinDelay毫秒等待
         *
         */
        if (currentIndex == lastIndex && spinDelay > 0) {
            try {
                Thread.sleep(spinDelay);
            } catch (InterruptedException e) {
                LOG.warn("Unexpected exception", e);
            }
        } else if (lastIndex == -1) {
            // We don't want to sleep on the first ever connect attempt.
            lastIndex = 0;
        }

        return serverAddresses.get(currentIndex);
    }

    public void onConnected() {
        lastIndex = currentIndex;
    }
}
