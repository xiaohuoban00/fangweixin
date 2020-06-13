package com.zmq.service;

import com.zmq.mapper.UserMapper;
import com.zmq.pojo.User;
import com.zmq.utils.FastDFSClient;
import com.zmq.utils.FileUtils;
import com.zmq.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/12 14:11
 */
@Service
@Transactional(propagation = Propagation.REQUIRED)
public class UserService {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private QRCodeUtils qrCodeUtils;
    @Autowired
    private FastDFSClient fastDFSClient;

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

    public User save(User user) {
        String userId = Sid.nextShort();
        // 为每个用户生成一个唯一的二维码
        String qrCodePath = "C://user" + userId + "qrcode.png";
        qrCodeUtils.createQRCode(qrCodePath, "userId:" + userId);
        MultipartFile qrCodeFile = FileUtils.fileToMultipart(qrCodePath);
        String qrCodeUrl = "";
        try {
            qrCodeUrl = fastDFSClient.uploadQRCode(qrCodeFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        File file = new File(qrCodePath);
        file.delete();
        user.setQrcode(qrCodeUrl);
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
}
