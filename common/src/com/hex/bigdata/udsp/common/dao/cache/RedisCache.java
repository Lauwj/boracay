package com.hex.bigdata.udsp.common.dao.cache;

import com.hex.bigdata.udsp.common.lock.RedisDistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Created by junjiem on 2017-2-16.
 */
@Repository("redisCache")
public class RedisCache<T> implements Cache<T> {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * Redis 分布式锁
     */
    @Autowired
    private RedisDistributedLock redisLock;

    @Override
    public boolean insertCache(String key, T t) {
        synchronized (key) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(key); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                if (StringUtils.isNotBlank(key) && t != null) {
                    redisTemplate.opsForValue().set(key, t);
                }
                return true;
            } finally {
                redisLock.unlock(key); // 分布式解锁
            }
        }
    }

    @Override
    public boolean updateCache(String key, T t) {
        return this.insertCache(key, t);
    }

    @Override
    public boolean deleteCache(String key) {
        synchronized (key) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(key); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                if (StringUtils.isNotBlank(key)) {
                    ValueOperations<String, T> valueOperation = redisTemplate.opsForValue();
                    RedisOperations<String, T> redisOperations = valueOperation.getOperations();
                    redisOperations.delete(key);
                }
                return true;
            } finally {
                redisLock.unlock(key); // 分布式解锁
            }
        }
    }

    @Override
    public T selectCache(String key) {
        synchronized (key) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(key); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                if (StringUtils.isNotBlank(key)) {
                    return (T) redisTemplate.opsForValue().get(key);
                }
                return null;
            } finally {
                redisLock.unlock(key); // 分布式解锁
            }
        }
    }

    @Override
    public boolean insertListCache(String key, List<T> list) {
        synchronized (key) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(key); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                if (StringUtils.isNotBlank(key) && list != null && list.size() != 0) {
                    this.deleteCache(key);
                    ListOperations<String, T> listOperations = redisTemplate.opsForList();
                    //listOperations.leftPushAll(key, list); // 这样查询出来的和插入的正好颠倒
                    listOperations.rightPushAll(key, list);
                }
                return true;
            } finally {
                redisLock.unlock(key); // 分布式解锁
            }
        }
    }

    @Override
    public boolean updateListCache(String key, List<T> list) {
        return this.insertListCache(key, list);
    }

    @Override
    public boolean deleteListCache(String key) {
        return this.deleteCache(key);
    }

    @Override
    public <T> List<T> selectListCache(String key) {
        synchronized (key) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(key); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                if (StringUtils.isNotBlank(key)) {
                    ListOperations<String, T> listOperations = (ListOperations<String, T>) redisTemplate.opsForList();
                    Long size = listOperations.size(key);
                    if (size != null && size > 0) {
                        return listOperations.range(key, 0, size - 1);
                    }
                }
                return null;
            } finally {
                redisLock.unlock(key); // 分布式解锁
            }
        }
    }

    @Override
    public <T> List<T> selectCacheLike(String likeKey) {
        synchronized (likeKey) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(likeKey); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                Set<String> keys = redisTemplate.keys(likeKey + "*");
                List<T> list = new ArrayList<>();
                Object obj = null;
                for (String key : keys) {
                    // 剔除掉“lock:”开头的作为锁的key
                    if (key.startsWith(RedisDistributedLock.LOCK_KEY_PREFIX)) {
                        continue;
                    }
                    //获取到键值之后，再拿键值去获取Value，此过程中，可能键值对应的Value已经被删除
                    //如果得到的value值是空，为空则不添加进list
                    obj = this.selectCache(key);
                    if (obj == null) {
                        continue;
                    }
                    list.add((T) obj);
                }
                return list;
            } finally {
                redisLock.unlock(likeKey); // 分布式解锁
            }
        }
    }

    @Override
    public boolean removeCacheLike(String likeKey) {
        synchronized (likeKey) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(likeKey); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                Set<String> keys = redisTemplate.keys(likeKey + "*");
                for (String key : keys) {
                    //剔除掉“lock:”开头的key
                    if (key.startsWith(RedisDistributedLock.LOCK_KEY_PREFIX)) {
                        continue;
                    }
                    this.deleteCache(key);
                }
                return true;
            } finally {
                redisLock.unlock(likeKey); // 分布式解锁
            }
        }
    }

    @Override
    public boolean insertTimeoutCache(String key, T t, long timeout) {
        synchronized (key) { // 单节点上锁（主要防止多线程并发资源不同步问题）
            redisLock.lock(key); // 分布式上锁 （主要防止多节点并发资源不同步问题）
            try {
                redisTemplate.opsForValue().set(key, t);
                redisTemplate.expire(key, timeout, TimeUnit.MILLISECONDS);
                return true;
            } finally {
                redisLock.unlock(key); // 分布式解锁
            }
        }
    }

}
