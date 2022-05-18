package com.bobo.redpacket.dao;

import com.baomidou.mybatisplus.mapper.BaseMapper;
import com.bobo.redpacket.model.po.RedpackActivityPO;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

/**
 * @author: HuangSiBo
 * @Description:
 * @Data: Created in 13:36 2021/9/20
 */

// 继承了MybatisPlus的BaseMapper接口，它就自动帮开发者把简单的crud方法都封装好了
@Repository
public interface RedpackActivityDao extends BaseMapper<RedpackActivityPO> {

    /**
     * @Description 这里是悲观锁的关键所在，悲观锁的实现往往依靠数据库提供的锁机制，只有数据库层提供的锁机制才能
     *  真正保证数据访问的排他性。select ... for update 语句在查询时，会把where语句中的字段锁住，如下面这条
     *  sql语句，id=#{id}这条语句就被锁定了，其他事务可以读但不能更新，必须等到本次事务提交后才能执行，这样可以
     *  保证当前的数据不会被其他事务修改。
     *  我们还需注意锁的级别，当where语句中的字段为主键或者索引，而且查到了该条数据，就是加行锁。其他情况，是加表锁。
     */
    @Select("select * from t_redpack_activity where id = #{id} for update")
    RedpackActivityPO selectByIDForUpdate(Long id);
}
