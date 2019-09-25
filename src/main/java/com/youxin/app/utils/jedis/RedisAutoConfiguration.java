package com.youxin.app.utils.jedis;


import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisAutoConfiguration {


	    private String address="redis://127.0.0.1:6379";

	    @Value("${spring.redis.port}")
	    private int port;

	    @Value("${spring.redis.timeout.seconds}")
	    private int timeout;

	    @Value("${spring.redis.jedis.pool.max-active}")
	    private int maxActive;

	    @Value("${spring.redis.jedis.pool.max-idle}")
	    private int maxIdle;

	    @Value("${spring.redis.jedis.pool.min-idle}")
	    private int minIdle;

	    @Value("${spring.redis.jedis.pool.max-wait.seconds}")
	    private long maxWaitMillis;
	    @Value("${spring.redis.isCluster}")
	    private Boolean isCluster;
	    
    @Bean(destroyMethod = "shutdown")
    public RedissonClient redissonSingle() {
    	
    	RedissonClient redissonClient=null;
    	System.out.println("redissonSingle start ");
    	try {
    		Config config = new Config();
    		
            config.setCodec(new JsonJacksonCodec()); 
            
            if(isCluster) {
//            	String[] nodes =redisConfig.getAddress().split(",");
//                 ClusterServersConfig serverConfig = config.useClusterServers();
//                serverConfig.addNodeAddress(nodes);
//                serverConfig.setKeepAlive(true);
//                serverConfig.setPingConnectionInterval(redisConfig.getPingConnectionInterval());
//                serverConfig.setPingTimeout(redisConfig.getPingTimeout());
//                serverConfig.setTimeout(redisConfig.getTimeout());
//                serverConfig.setConnectTimeout(redisConfig.getConnectTimeout());
//                if(!StringUtil.isEmpty(redisConfig.getPassword())) {
//                    serverConfig.setPassword(redisConfig.getPassword());
//                }
           }else {
            	  SingleServerConfig serverConfig = config.useSingleServer()
                  		.setAddress(address)
                  		.setDatabase(0);
            	  serverConfig.setKeepAlive(true);
                  serverConfig.setPingConnectionInterval(500);
                  serverConfig.setPingTimeout(timeout);
                  serverConfig.setTimeout(timeout);
                  serverConfig.setConnectTimeout(timeout);
                  serverConfig.setConnectionMinimumIdleSize(32);
                  
                  serverConfig.setConnectionPoolSize(64);
                  
//                   if(!StringUtil.isEmpty(redisConfig.getPassword())) {
//                      serverConfig.setPassword(redisConfig.getPassword());
//                  }
            }
            
            
          
             redissonClient= Redisson.create(config);
             
             System.out.println("redissonSingle create end ");
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        return redissonClient; 
        
    }
   /* @Bean
    public JedisPoolConfig jedisPoolConfig() {
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxWaitMillis(10000);
        config.setMaxTotal(100);
       
        //config.setMaxIdle(-1);
        config.setTestOnCreate(true);
        config.setTestWhileIdle(true);
        config.setTestOnReturn(true);
        return config;
    }

    @Bean
    public JedisPool jedisPool(JedisPoolConfig jedisPoolConfig) {
        return new JedisPool(jedisPoolConfig, redisConfig.getHost(), redisConfig.getPort(),
                5000,("".equals(redisConfig.getPassword())?null:redisConfig.getPassword()), redisConfig.getDatabase(), null);
    }*/

//    @Bean
//    public JedisTemplate jedisTemplate(JedisPoolConfig jedisPoolConfig, JedisPool jedisPool) {
//        if (properties.getHost().contains(",")) {
//            Set<HostAndPort> nodes = new HashSet<HostAndPort>();
//            // 配置redis集群
//            for (String host : properties.getHost().split(",")) {
//                String[] detail = host.split(":");
//                nodes.add(new HostAndPort(detail[0], Integer.parseInt(detail[1])));
//            }
//            return new JedisTemplate(new JedisCluster(nodes, jedisPoolConfig));
//        } else {
//            return new JedisTemplate(jedisPool);
//        }
//    }

   /* @Bean(value="redisCRUD")
    @ConditionalOnProperty(name = "im.redisConfig.isCluster", havingValue = "false")
    public RedisUtil jedis(JedisPool jedisPool){
    	RedisUtil redis=new RedisUtil(jedisPool);
        return redis;
    }*/

   /* @Bean(value="redisCRUD")
    @ConditionalOnProperty(name = "im.redisConfig.isCluster", havingValue = "true")
    public JedisCluster jedisCluster(JedisPoolConfig jedisPoolConfig){
        Set<HostAndPort> nodes = new HashSet<>();
        // 配置redis集群
        for (String host : redisConfig.getHost().split(",")) {
            String[] detail = host.split(":");
            nodes.add(new HostAndPort(detail[0], Integer.parseInt(detail[1])));
        }
        return new JedisCluster(nodes, jedisPoolConfig);
    }*/

}
