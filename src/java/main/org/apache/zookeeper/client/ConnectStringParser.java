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

import java.net.InetSocketAddress;
import java.util.ArrayList;

import org.apache.zookeeper.common.PathUtils;

/**
 * A parser for ZooKeeper Client connect strings.
 *
 * This class is not meant to be seen or used outside of ZooKeeper itself.
 *
 * The chrootPath member should be replaced by a Path object in issue
 * ZOOKEEPER-849.
 *
 * @see org.apache.zookeeper.ZooKeeper
 */
public final class ConnectStringParser {
    private static final int DEFAULT_PORT = 2181;

    private final String chrootPath;
    /**
     * 将服务器地址和相应端口封装成一个InetSocketAddress对象
     * 会进一步封装到staticHostProvider类中
     */
    private final ArrayList<InetSocketAddress> serverAddresses = new ArrayList<InetSocketAddress>();

    /**
     *
     * @throws IllegalArgumentException
     *             for an invalid chroot path.
     *             解析服务器地址
     */
    public ConnectStringParser(String connectString) {
        // parse out chroot, if any
        int off = connectString.indexOf('/');
        if (off >= 0) {
            String chrootPath = connectString.substring(off);
            // ignore "/" chroot spec, same as null
            if (chrootPath.length() == 1) {
                this.chrootPath = null;
            } else {
                PathUtils.validatePath(chrootPath);
                this.chrootPath = chrootPath;
            }
            connectString = connectString.substring(0, off);
        } else {
            this.chrootPath = null;
        }

        String hostsList[] = connectString.split(",");
        for (String host : hostsList) {
            int port = DEFAULT_PORT;
            int pidx = host.lastIndexOf(':');
            if (pidx >= 0) {
                // otherwise : is at the end of the string, ignore
                if (pidx < host.length() - 1) {
                    port = Integer.parseInt(host.substring(pidx + 1));
                }
                host = host.substring(0, pidx);
            }
            serverAddresses.add(InetSocketAddress.createUnresolved(host, port));
        }
    }

    /**
     * 解析chrootPath,保存服务器地址
     * @return
     */
    public String getChrootPath() {
        return chrootPath;
    }

    public ArrayList<InetSocketAddress> getServerAddresses() {
        return serverAddresses;
    }
}