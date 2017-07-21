package com.zhizus.mybatis;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.util.List;

/**
 * Created by dempezheng on 2017/7/21.
 */
public class ConfigTest {

    public static void main(String[] args) {
        Config load = ConfigFactory.load("application.conf");
        Config group = load.getConfig("mybatis").getConfig("group");
        String aPackage = group.getString("package");
        System.out.println(aPackage);
        List<? extends Config> configList = group.getConfigList("source-group");
        for (Config config : configList) {
            System.out.println(config.getInt("group"));
            System.out.println(config.getConfig("datasource").getString("url"));
//            System.out.println(config.getString("group"));
        }


    }


}
