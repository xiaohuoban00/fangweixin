package com.zmq.service;

import com.zmq.mapper.MyFriendsMapper;
import com.zmq.pojo.MyFriends;
import com.zmq.pojo.vo.MyFriendsVO;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/12 14:13
 */
@Service
@Transactional
public class MyFriendService {
    @Autowired
    private MyFriendsMapper myFriendsMapper;

    /**
     * 新增好友
     *
     * @param acceptUserId
     * @param sendUserId
     */
    public void saveFriends(String acceptUserId, String sendUserId) {
        MyFriends myFriends = new MyFriends();
        myFriends.setMyUserId(acceptUserId);
        myFriends.setMyFriendUserId(sendUserId);
        myFriends.setId(Sid.nextShort());
        myFriendsMapper.insert(myFriends);
    }

    /**
     * 查询好友列表
     *
     * @param userId
     * @return
     */
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return myFriendsMapper.queryMyFriends(userId);
    }
}
