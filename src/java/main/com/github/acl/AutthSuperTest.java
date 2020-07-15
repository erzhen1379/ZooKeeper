package com.github.acl;

import com.github.zookeeper.clientApi.ZKCreate;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;

import java.io.IOException;

/**
 * 测试超级管理员用户
 * 11111
 * 123
 */
public class AutthSuperTest {
    final static String path = "/zk-book";

    public static void main(String[] args) throws Exception {
        ZooKeeper zk1 = new ZooKeeper("172.16.3.200:2181", 5000, null);
        zk1.addAuthInfo("digest", "foo:true".getBytes());
        zk1.create(path, "init".getBytes(), ZooDefs.Ids.CREATOR_ALL_ACL, CreateMode.EPHEMERAL);

        ZooKeeper zk2 = new ZooKeeper("172.16.3.200:2181", 5000, null);
        zk2.addAuthInfo("digest", "foo:zk-book".getBytes());
        System.out.println(zk2.getData(path, false, null));


        ZooKeeper zk3 = new ZooKeeper("172.16.3.200:2181", 5000, null);
        zk3.addAuthInfo("digest", "foo:false".getBytes());
        System.out.println(zk3.getData(path, false, null));



    }
}
