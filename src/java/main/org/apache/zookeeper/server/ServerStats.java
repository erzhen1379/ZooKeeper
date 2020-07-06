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

package org.apache.zookeeper.server;


import org.apache.zookeeper.common.Time;

/**
 * Basic Server Statistics
 * 是zookeeper运行时的统计器，包含最基本的运行时信息
 */
public class ServerStats {
    //从zookeeper启动开始，或者最近一次重置服务端信息统计后，服务端向客户端发送的响应包次数
    private long packetsSent;
    ////从zookeeper启动开始，或者最近一次重置服务端信息统计后，服务端接收来客户端请求包次数
    private long packetsReceived;
    //最大延迟
    private long maxLatency;
    //最小延迟
    private long minLatency = Long.MAX_VALUE;
    //总延迟
    private long totalLatency = 0;
    //服务端处理客户端请求总次数
    private long count = 0;

    private final Provider provider;

    public interface Provider {
        public long getOutstandingRequests();

        public long getLastProcessedZxid();

        public String getState();

        public int getNumAliveConnections();
    }

    public ServerStats(Provider provider) {
        this.provider = provider;
    }

    // getters
    synchronized public long getMinLatency() {
        return minLatency == Long.MAX_VALUE ? 0 : minLatency;
    }

    synchronized public long getAvgLatency() {
        if (count != 0) {
            return totalLatency / count;
        }
        return 0;
    }

    synchronized public long getMaxLatency() {
        return maxLatency;
    }

    public long getOutstandingRequests() {
        return provider.getOutstandingRequests();
    }

    public long getLastProcessedZxid() {
        return provider.getLastProcessedZxid();
    }

    synchronized public long getPacketsReceived() {
        return packetsReceived;
    }

    synchronized public long getPacketsSent() {
        return packetsSent;
    }

    public String getServerState() {
        return provider.getState();
    }

    /** The number of client connections alive to this server */
    public int getNumAliveClientConnections() {
        return provider.getNumAliveConnections();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Latency min/avg/max: " + getMinLatency() + "/"
                + getAvgLatency() + "/" + getMaxLatency() + "\n");
        sb.append("Received: " + getPacketsReceived() + "\n");
        sb.append("Sent: " + getPacketsSent() + "\n");
        sb.append("Connections: " + getNumAliveClientConnections() + "\n");

        if (provider != null) {
            sb.append("Outstanding: " + getOutstandingRequests() + "\n");
            sb.append("Zxid: 0x" + Long.toHexString(getLastProcessedZxid()) + "\n");
        }
        sb.append("Mode: " + getServerState() + "\n");
        return sb.toString();
    }

    // mutators
    synchronized void updateLatency(long requestCreateTime) {
        long latency = Time.currentElapsedTime() - requestCreateTime;
        totalLatency += latency;
        count++;
        if (latency < minLatency) {
            minLatency = latency;
        }
        if (latency > maxLatency) {
            maxLatency = latency;
        }
    }

    synchronized public void resetLatency() {
        totalLatency = 0;
        count = 0;
        maxLatency = 0;
        minLatency = Long.MAX_VALUE;
    }

    synchronized public void resetMaxLatency() {
        maxLatency = getMinLatency();
    }

    synchronized public void incrementPacketsReceived() {
        packetsReceived++;
    }

    synchronized public void incrementPacketsSent() {
        packetsSent++;
    }

    synchronized public void resetRequestCounters() {
        packetsReceived = 0;
        packetsSent = 0;
    }

    synchronized public void reset() {
        resetLatency();
        resetRequestCounters();
    }

}
