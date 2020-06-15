package com.zmq.mapper;

import com.zmq.pojo.FriendsRequest;
import com.zmq.pojo.vo.FriendRequestVO;
import org.apache.ibatis.annotations.Select;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/12 13:35
 */
public interface FriendsRequestMapper extends Mapper<FriendsRequest> {
    @Select(value = "select u.id as sendUserId," +
            "u.username as sendUsername," +
            "u.face_image as sendFaceImage," +
            "u.nickname as sendNickname from friends_request fr left join users u on u.id = fr.send_user_id where " +
            "fr.accept_user_id = #{acceptUserId}")
    List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
}
