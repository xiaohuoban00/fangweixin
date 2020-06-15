package com.zmq.mapper;

import com.zmq.pojo.MyFriends;
import com.zmq.pojo.vo.MyFriendsVO;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/12 13:36
 */
public interface MyFriendsMapper extends Mapper<MyFriends> {
    @Select(value = "select u.id as friendUserId,u.username as friendUsername,u.face_image as friendFaceImage,u.nickname as friendNickname " +
            "from my_friends mf left join users u on u.id = mf.my_friend_user_id " +
            "where mf.my_user_id = #{userId}")
    List<MyFriendsVO> queryMyFriends(String userId);
}
