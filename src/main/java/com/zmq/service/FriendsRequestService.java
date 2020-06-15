package com.zmq.service;

import com.zmq.mapper.FriendsRequestMapper;
import com.zmq.mapper.MyFriendsMapper;
import com.zmq.pojo.FriendsRequest;
import com.zmq.pojo.MyFriends;
import com.zmq.pojo.vo.FriendRequestVO;
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
public class FriendsRequestService {

    @Autowired
    private FriendsRequestMapper friendsRequestMapper;
    @Autowired
    private MyFriendsMapper myFriendsMapper;
    @Autowired
    private MyFriendService myFriendService;

    /**
     * 查询好友请求
     *
     * @param acceptUserId
     * @return
     */
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return friendsRequestMapper.queryFriendRequestList(acceptUserId);
    }


    /**
     * 删除好友请求
     * @param acceptUserId
     * @param sendUserId
     */
    public void deleteFriendRequest(String acceptUserId,String sendUserId){
        FriendsRequest friendsRequest = new FriendsRequest();
        friendsRequest.setAcceptUserId(acceptUserId);
        friendsRequest.setSendUserId(sendUserId);
        friendsRequestMapper.delete(friendsRequest);
    }


    /**
     * 通过好友请求
     * @param acceptUserId
     * @param sendUserId
     */
    public void passFriendRequest(String acceptUserId,String sendUserId){
        deleteFriendRequest(acceptUserId,sendUserId);
        myFriendService.saveFriends(acceptUserId,sendUserId);
        myFriendService.saveFriends(sendUserId,acceptUserId);
    }

}
