package com.hex.bigdata.udsp.olq.provider.impl.model;

import com.hex.bigdata.udsp.common.model.ComDatasource;
import com.hex.bigdata.udsp.common.model.ComProperties;
import com.hex.bigdata.udsp.common.provider.model.Datasource;
import com.hex.bigdata.udsp.common.provider.model.Property;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * Created by junjiem on 2017-2-20.
 */
public class ImpalaDatasource extends Datasource {
    private String driverClass;
    private String jdbcUrl;
    private String username;
    private String password;
    private String initialSize;
    private String minIdle;
    private String maxIdle;
    private String maxWait;
    private String maxActive;
    private String validationQueryTimeout;
    private String timeBetweenEvictionRunsMillis;
    private String minEvictableIdleTimeMillis;
    private String validationQuery;
    private String testWhileIdle;
    private String testOnBorrow;
    private String testOnReturn;
    private int maxNum = 65535;

    public String getDriverClass() {
        String value = getProperty("driver.class").getValue();
        if (StringUtils.isBlank(value))
            value = "org.apache.hive.jdbc.HiveDriver";
        return value;
    }

    public String getJdbcUrl() {
        String value = getProperty("jdbc.url").getValue();
        if (StringUtils.isBlank(value))
            throw new IllegalArgumentException("jdbc.url不能为空");
        return value;
    }

    public String getUsername() {
        return getProperty("username").getValue();
    }

    public String getPassword() {
        return getProperty("password").getValue();
    }

    public String getInitialSize() {
        return getProperty("initial.size").getValue();
    }

    public String getMinIdle() {
        return getProperty("min.idle").getValue();
    }

    public String getMaxIdle() {
        return getProperty("max.idle").getValue();
    }

    public String getMaxWait() {
        return getProperty("max.wait").getValue();
    }

    public String getMaxActive() {
        return getProperty("max.active").getValue();
    }

    public String getValidationQueryTimeout() {
        return getProperty("validation.query.timeout").getValue();
    }

    public String getTimeBetweenEvictionRunsMillis() {
        return getProperty("time.between.eviction.runs.millis").getValue();
    }

    public String getMinEvictableIdleTimeMillis() {
        return getProperty("min.evictable.idle.time.millis").getValue();
    }

    public String getValidationQuery() {
        return getProperty("validation.query").getValue();
    }

    public String getTestWhileIdle() {
        return getProperty("test.while.idle").getValue();
    }

    public String getTestOnBorrow() {
        return getProperty("test.on.borrow").getValue();
    }

    public String getTestOnReturn() {
        return getProperty("test.on.return").getValue();
    }

    public ImpalaDatasource(List<Property> properties) {
        super(properties);
    }

    public ImpalaDatasource(Map<String, Property> propertieMap) {
        super(propertieMap);
    }

    public ImpalaDatasource(ComDatasource comDatasource, List<ComProperties> comPropertieList) {
        super(comDatasource, comPropertieList);
    }
    public int getMaxNum() {
        String value = getProperty("max.data.size").getValue();
        if (StringUtils.isNotBlank(value)) {
            try {
                maxNum = Integer.valueOf(value);
            } catch (Exception e) {
                throw new IllegalArgumentException("max.data.size为一整数");
            }
        }
        return maxNum;
    }
}
