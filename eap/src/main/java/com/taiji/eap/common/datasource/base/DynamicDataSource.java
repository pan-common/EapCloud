package com.taiji.eap.common.datasource.base;

import com.alibaba.druid.pool.DruidDataSource;
import com.taiji.eap.common.datasource.bean.DataSource;
import org.apache.ibatis.mapping.VendorDatabaseIdProvider;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * 动态切换数据源
 */
public class DynamicDataSource extends AbstractRoutingDataSource implements ApplicationContextAware{

    private static String DATA_SOURCES_NAME = "targetDataSources";

    private ApplicationContext applicationContext;

    private DruidDataSource druidDataSource;//默认数据源

    @Autowired
    private VendorDatabaseIdProvider databaseIdProvider;//数据语言标识

    @Autowired
    private SqlSessionFactoryBean sqlSessionFactoryBean;//数据源会话管理

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setDefaultTargetDataSource(Object defaultTargetDataSource) {
        super.setDefaultTargetDataSource(defaultTargetDataSource);
        this.druidDataSource = (DruidDataSource) defaultTargetDataSource;
    }

    @Override
    protected Object determineCurrentLookupKey() {
        DataSource dataSource = DataSourceHolder.getDataSource();
        if(dataSource==null){
            return null;
        }
        try {
            Map<Object,Object> map = getTargetDataSources();
            synchronized (map){
                if(!map.containsKey(dataSource.getBeanName())){
                    map.put(dataSource.getBeanName(),createDataSource(dataSource));
                    super.afterPropertiesSet();//通知spring有bean更新
                }
            }
            updateDatabaseId(dataSource.getBeanName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return dataSource.getBeanName();
    }

    private void updateDatabaseId(String beanName) throws Exception{
        DruidDataSource druidDataSource = null;
        Map<Object,Object> dataSources = getTargetDataSources();
        druidDataSource = (DruidDataSource) dataSources.get(beanName);
        sqlSessionFactoryBean.setDataSource(druidDataSource);
        sqlSessionFactoryBean.getObject().getConfiguration().setDatabaseId(databaseIdProvider.getDatabaseId(druidDataSource));
    }

    private Object createDataSource(DataSource dataSourceBean) throws IllegalAccessException {
        ConfigurableApplicationContext context = (ConfigurableApplicationContext) applicationContext;
        DefaultListableBeanFactory factory = (DefaultListableBeanFactory) context.getBeanFactory();
        BeanDefinitionBuilder beanDefinitionBuilder = BeanDefinitionBuilder.genericBeanDefinition(DruidDataSource.class);
        Map<String,Object> properties = getPropertyKeyValues(DataSource.class,dataSourceBean);
        properties.remove("datasourceId");
        properties.remove("datasourceType");
        properties.remove("connectName");
        for (Map.Entry<String,Object> entry:properties.entrySet()){
            beanDefinitionBuilder.addPropertyValue(entry.getKey(),entry.getValue());
        }
        factory.registerBeanDefinition(dataSourceBean.getBeanName(),beanDefinitionBuilder.getBeanDefinition());
        return applicationContext.getBean(dataSourceBean.getBeanName());
    }

    private <T> Map<String,Object> getPropertyKeyValues(Class<T> clazz,Object object) throws IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Object> result = new HashMap<String,Object>();
        for (Field field : fields) {
            field.setAccessible(true);
            result.put(field.getName(), field.get(object));
        }
        result.remove("beanName");
        return result;
    }

    public Map<Object,Object> getTargetDataSources() throws NoSuchFieldException, IllegalAccessException {
        Field field = AbstractRoutingDataSource.class.getDeclaredField(DATA_SOURCES_NAME);
        field.setAccessible(true);
        return (Map<Object, Object>) field.get(this);
    }

    public DruidDataSource getDefaultDataSource() {
        return druidDataSource;
    }
}
