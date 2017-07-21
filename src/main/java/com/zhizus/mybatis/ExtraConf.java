package com.zhizus.mybatis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.convert.support.ConfigurableConversionService;
import org.springframework.core.convert.support.DefaultConversionService;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by Dempe on 2017/7/20 0020.
 */
public class ExtraConf {
    private static final Logger LOGGER = LoggerFactory.getLogger(ExtraConf.class);

    public static Map<ConfType, Map<String, Conf>> configs = new HashMap<ConfType, Map<String, Conf>>();

    static {
        init();
    }

    public static Set<String> getInitBeanNames(ConfType confType) {
        return configs.get(confType) == null ? null : configs.get(confType).keySet();
    }

    public static Conf getConfig(ConfType confType, String beanName) {
        if (configs.get(confType) == null) {
            return null;
        }
        return configs.get(confType).get(beanName);
    }

    public static boolean hasExtraConf(ConfType confType) {
        return configs.containsKey(confType);
    }

    private static void init() {
        InputStream inputStream = ExtraConf.class.getResourceAsStream("/extra.conf");
        if (inputStream == null) {
            LOGGER.warn("no extra.conf ");
            return;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        String line = null;
        try {
            ConfType type = null;
            while ((line = reader.readLine()) != null) {
                if (ConfType.MYSQL.is(line) || ConfType.NSQ.is(line) || ConfType.REDIS.is(line)) { //某一类配置开始
                    type = ConfType.getByFlag(line);
                    continue;
                }
                if (type == null) {
                    continue;  //必须先解析出 type
                }
                int index = line.indexOf(".");
                int valueIndex = line.indexOf("=");
                if (index == -1 || valueIndex == -1 || index > valueIndex) { //忽略不符合规范的配置
                    continue;
                }

                Map<String, Conf> map = configs.get(type);

                if (map == null) {
                    map = new HashMap<String, ExtraConf.Conf>();
                    configs.put(type, map);
                }

                String beanName = line.substring(0, index).trim();
                String name = line.substring(index + 1, valueIndex).trim();
                String value = line.substring(valueIndex + 1).trim();

                Conf conf = map.get(beanName);
                if (conf == null) {
                    conf = new Conf();
                    conf.setName(beanName);
                    map.put(beanName, conf);
                }

                conf.addProperty(name, value);

            }
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }

    }

    public static enum ConfType {
        MYSQL {
            @Override
            public boolean is(String flag) {
                return flag != null && flag.contains("[mysql]");
            }
        },
        NSQ {
            @Override
            public boolean is(String flag) {
                return flag != null && flag.contains("[nsq]");
            }
        },
        REDIS {
            @Override
            public boolean is(String flag) {
                return flag != null && flag.contains("[redis]");
            }
        };

        public static ConfType getByFlag(String flag) {
            for (ConfType type : ConfType.values()) {
                if (type.is(flag)) {
                    return type;
                }
            }
            return null;
        }

        public abstract boolean is(String flag);
    }

    public static class Conf {
        protected ConfigurableConversionService conversionService = new DefaultConversionService();
        Map<String, String> properties;
        private String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public <T> T getProperty(String key, Class<T> classz, T defaultValue) {
            String value = null;
            if (key == null || (value = properties.get(key)) == null) {
                return defaultValue;
            }

            if (conversionService.canConvert(String.class, classz)) {
                T rValue = conversionService.convert(value, classz);
                return rValue == null ? defaultValue : rValue;
            }

            return defaultValue;
        }

        public String getProperty(String key) {
            return properties == null ? null : properties.get(key);
        }

        public void addProperty(String key, String value) {
            if (properties == null) {
                properties = new HashMap<String, String>();
            }
            properties.put(key, value);
        }
    }

}

