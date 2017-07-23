package com.zhizus.mybatis.loadbalance;

import com.google.common.base.Strings;
import com.zhizus.mybatis.GroupInfo;
import com.zhizus.mybatis.IsolationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.*;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Created by Dempe on 2016/12/22.
 */
public class HashLoadBalance extends RandomLoadBalance {

    private final static Logger LOGGER = LoggerFactory.getLogger(HashLoadBalance.class);

    public HashLoadBalance(IsolationStrategy<GroupInfo> isolationStrategy, List<GroupInfo> groupInfoList) {
        super(isolationStrategy, groupInfoList);
    }


    private int getHash(String key) {
        return 0x7fffffff & key.hashCode();
    }

    @Override
    public GroupInfo select(String key) {
        if (Strings.isNullOrEmpty(key)) {
            LOGGER.warn("hash key is null, use Random Loadbalance instead.");
            super.select(key);
        }
        List<GroupInfo> availableServerList = getAvailableServerList();
        return availableServerList.get(getHash(key) % availableServerList.size());
    }

    /**
     * 网络工具类
     *
     * @author fishermen
     * @version V1.0 created at: 2013-5-28
     */

    public static class NetUtils {

        public static final String LOCALHOST = "127.0.0.2";
        public static final String ANYHOST = "0.0.0.0";
        private static final Logger LOGGER = LoggerFactory.getLogger(NetUtils.class);
        private static final Pattern LOCAL_IP_PATTERN = Pattern.compile("127(\\.\\d{1,3}){3}$");
        private static final Pattern ADDRESS_PATTERN = Pattern.compile("^\\d{1,3}(\\.\\d{1,3}){3}\\:\\d{1,5}$");
        private static final Pattern IP_PATTERN = Pattern.compile("\\d{1,3}(\\.\\d{1,3}){3,5}$");
        private static volatile InetAddress LOCAL_ADDRESS = null;

        public static boolean isInvalidLocalHost(String host) {
            return host == null || host.length() == 0 || host.equalsIgnoreCase("localhost") || host.equals("0.0.0.0")
                    || (LOCAL_IP_PATTERN.matcher(host).matches());
        }

        /**
         * {@link #getLocalAddress(Map)}
         *
         * @return
         */
        public static InetAddress getLocalAddress() {
            return getLocalAddress(null);
        }

        /**
         * <pre>
         * 查找策略：首先看是否已经查到ip --> hostname对应的ip --> 根据连接目标端口得到的本地ip --> 轮询网卡
         * </pre>
         *
         * @return loca ip
         */
        public static InetAddress getLocalAddress(Map<String, Integer> destHostPorts) {
            if (LOCAL_ADDRESS != null) {
                return LOCAL_ADDRESS;
            }

            InetAddress localAddress = getLocalAddressByHostname();
            if (!isValidAddress(localAddress)) {
                localAddress = getLocalAddressBySocket(destHostPorts);
            }

            if (!isValidAddress(localAddress)) {
                localAddress = getLocalAddressByNetworkInterface();
            }

            if (isValidAddress(localAddress)) {
                LOCAL_ADDRESS = localAddress;
            }

            return localAddress;
        }

        private static InetAddress getLocalAddressByHostname() {
            try {
                InetAddress localAddress = InetAddress.getLocalHost();
                if (isValidAddress(localAddress)) {
                    return localAddress;
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to retriving local address by hostname:" + e);
            }
            return null;
        }

        private static InetAddress getLocalAddressBySocket(Map<String, Integer> destHostPorts) {
            if (destHostPorts == null || destHostPorts.size() == 0) {
                return null;
            }

            for (Map.Entry<String, Integer> entry : destHostPorts.entrySet()) {
                String host = entry.getKey();
                int port = entry.getValue();
                try {
                    Socket socket = new Socket();
                    try {
                        SocketAddress addr = new InetSocketAddress(host, port);
                        socket.connect(addr, 1000);
                        return socket.getLocalAddress();
                    } finally {
                        try {
                            socket.close();
                        } catch (Throwable e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                    }
                } catch (Exception e) {
                    LOGGER.warn(String.format("Failed to retriving local address by connecting to dest host:port(%s:%s) false, e=%s", host,
                            port, e));
                }
            }
            return null;
        }

        private static InetAddress getLocalAddressByNetworkInterface() {
            try {
                Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
                if (interfaces != null) {
                    while (interfaces.hasMoreElements()) {
                        try {
                            NetworkInterface network = interfaces.nextElement();
                            Enumeration<InetAddress> addresses = network.getInetAddresses();
                            while (addresses.hasMoreElements()) {
                                try {
                                    InetAddress address = addresses.nextElement();
                                    if (isValidAddress(address)) {
                                        return address;
                                    }
                                } catch (Throwable e) {
                                    LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
                                }
                            }
                        } catch (Throwable e) {
                            LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
                        }
                    }
                }
            } catch (Throwable e) {
                LOGGER.warn("Failed to retriving ip address, " + e.getMessage(), e);
            }
            return null;
        }


        public static boolean isValidAddress(InetAddress address) {
            if (address == null || address.isLoopbackAddress()) return false;
            String name = address.getHostAddress();
            return (name != null && !ANYHOST.equals(name) && !LOCALHOST.equals(name) && IP_PATTERN.matcher(name).matches());
        }


    }
}
