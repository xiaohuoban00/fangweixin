package com.zmq.service;

import com.zmq.enums.SearchFriendsStatusEnum;
import com.zmq.mapper.MyFriendsMapper;
import com.zmq.mapper.UserMapper;
import com.zmq.pojo.MyFriends;
import com.zmq.pojo.User;
import com.zmq.utils.FastDFSClient;
import com.zmq.utils.FileUtils;
import com.zmq.utils.Image;
import com.zmq.utils.QRCodeUtils;
import org.apache.commons.collections.CollectionUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/12 14:11
 */
@Service
@Transactional
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QRCodeUtils qrCodeUtils;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private MyFriendsMapper myFriendsMapper;

    public User findByUsername(String username) {
        User user = new User();
        user.setUsername(username);
        return userMapper.selectOne(user);
    }

    public User findByUsernameAndPassword(String username, String password) {
        Example userExample = new Example(User.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username", username);
        criteria.andEqualTo("password", password);
        return userMapper.selectOneByExample(userExample);
    }

    public User save(User user) throws IOException {
        String userId = Sid.nextShort();
        // 为每个用户生成一个唯一的二维码
        String qrCodePath = "E:\\image\\";
        String fileName = userId + "qrcode.png";
        File file = new File(qrCodePath);
        if(!file.exists()){
            file.mkdir();
        }
        qrCodeUtils.createQRCode(qrCodePath+fileName, "userId:" + userId);
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath+fileName);
        Image image = fastDFSClient.uploadFile(qrCodeFile);
        user.setQrcode(image.getFullPath());
        user.setId(userId);
        userMapper.insert(user);
        return user;
    }

    public void update(User user) {
        userMapper.updateByPrimaryKeySelective(user);
    }

    public User findById(String id){
        return userMapper.selectByPrimaryKey(id);
    }

    /**
     * 搜索好友的前置条件
     * @param id
     * @param username
     * @return
     */
    public Integer preconditionSearchFriends(String id, String username) {
        User user = findByUsername(username);
        if(user==null){
            return SearchFriendsStatusEnum.USER_NOT_EXIST.status;
        }
        User myUser = userMapper.selectByPrimaryKey(id);
        if(myUser.getId().equals(user.getId())){
            return SearchFriendsStatusEnum.NOT_YOURSELF.status;
        }
        MyFriends myFriends = new MyFriends();
        myFriends.setMyFriendUserId(user.getId());
        myFriends.setMyUserId(id);
        List<MyFriends> myFriendsList = myFriendsMapper.select(myFriends);
        if(CollectionUtils.isNotEmpty(myFriendsList)){
            return SearchFriendsStatusEnum.ALREADY_FRIENDS.status;
        }
        return SearchFriendsStatusEnum.SUCCESS.status;
    }

}
