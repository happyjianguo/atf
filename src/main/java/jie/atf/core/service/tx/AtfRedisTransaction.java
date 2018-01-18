package jie.atf.core.service.tx;

import java.util.List;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

import jie.atf.core.utils.AtfUtils;
import jie.atf.core.utils.exception.AtfException;

/**
 * Redis事务 - 乐观锁
 * 
 * @author Jie
 *
 */
@Component
public class AtfRedisTransaction implements IAtfTransaction {
	// @Autowired
	private RedisTemplate<String, String> redisTemplate;

	@Override
	public void lock(final String key, final String value, final Long seconds) throws AtfException {
		AtfUtils.checkNotNull(key, "Redis key can NOT be null or empty");
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection conn) throws DataAccessException {
				StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
				byte[] keyBytes = stringRedisSerializer.serialize(key);
				// WATCH 监视键
				// 在EXEC命令执行时，检查被监视的键是否已被修改。如果被修改（该键标记为dirty），服务器将拒绝事务提交
				conn.watch(keyBytes);

				// 若key对应的值存在，则已经lock
				String oldValue = (String) stringRedisSerializer.deserialize(conn.get(keyBytes));
				if (StringUtils.isNotEmpty(oldValue))
					throw new RuntimeException("Redis lock is already exist");

				// MULTI 事务开始
				conn.multi();
				// 将命令放入事务队列
				conn.set(keyBytes, stringRedisSerializer.serialize(value));
				// set expire time
				conn.expire(keyBytes, seconds);
				// EXEC 事务提交(commit)。遍历执行事务队列中的命令
				List<Object> rets = conn.exec();
				if (CollectionUtils.isEmpty(rets))
					throw new RuntimeException("Redis lock failed");
				return null;
			}
		});
	}

	@Override
	public void unlock(final String key) {
		redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection conn) throws DataAccessException {
				StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
				byte[] keyBytes = stringRedisSerializer.serialize(key);
				while (true) {
					conn.watch(keyBytes);

					conn.multi();
					conn.del(keyBytes);
					List<Object> rets = conn.exec();
					if (CollectionUtils.isEmpty(rets))
						continue; // retry unlock
				}
			}
		});
	}
}
