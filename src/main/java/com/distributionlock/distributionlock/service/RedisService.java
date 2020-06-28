package com.distributionlock.distributionlock.service;

import java.util.concurrent.TimeUnit;

public interface RedisService {

    /**
     *
     * @param key
     * @param value
     * @param timestamp
     * @param dur
     * @return
     */
    boolean put(String key,Object value,Long timestamp,boolean dur);


    /**
     *
     * @param key
     * @return
     */
    Object get(String key);


    /**
     *
     * @param key
     * @param time
     * @param timeUnit
     * @return
     */
    boolean expire(String key, Long time, TimeUnit timeUnit);
}
