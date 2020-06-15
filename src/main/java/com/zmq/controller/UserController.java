package com.zmq.controller;

import com.zmq.enums.OperatorFriendRequestTypeEnum;
import com.zmq.enums.SearchFriendsStatusEnum;
import com.zmq.pojo.User;
import com.zmq.pojo.bo.UserBO;
import com.zmq.pojo.vo.FriendRequestVO;
import com.zmq.pojo.vo.MyFriendsVO;
import com.zmq.pojo.vo.UserVO;
import com.zmq.service.FriendsRequestService;
import com.zmq.service.MyFriendService;
import com.zmq.service.UserService;
import com.zmq.utils.*;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 *
 * @Author zmq
 * @Date 2020/6/12 14:11
 */
@RestController
@RequestMapping("user")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private FastDFSClient fastDFSClient;
    @Autowired
    private FriendsRequestService friendsRequestService;
    @Autowired
    private MyFriendService myFriendService;

    /**
     * 注册或登录
     *
     * @param user
     * @return
     * @throws Exception
     */
    @PostMapping("registerOrLogin")
    public JSONResult registerOrLogin(@RequestBody User user) throws Exception {
        // 0. 判断用户名和密码不能为空
        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            return JSONResult.errorMsg("用户名或密码不能为空...");
        }
        // 1. 判断用户名是否存在，如果存在就登录，如果不存在则注册
        User findUser = userService.findByUsername(user.getUsername());
        User userResult;
        if (findUser != null) {
            // 1.1 登录
            userResult = userService.findByUsernameAndPassword(user.getUsername(),
                    MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return JSONResult.errorMsg("用户名或密码不正确...");
            }
        } else {
            // 1.2 注册
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult = userService.save(user);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userResult, userVO);
        return JSONResult.ok(userVO);
    }

    /**
     * 上传头像
     *
     * @param userBO
     * @return
     * @throws Exception
     */
    @PostMapping("uploadFaceBase64")
    public JSONResult uploadFaceBase64(@RequestBody UserBO userBO) throws Exception {
        String faceData = userBO.getFaceData();
        String userFacePath = "E:\\image\\";
        String fileName = userBO.getUserId() + "base64.png";
        File file = new File(userFacePath);
        if (!file.exists()) {
            file.mkdir();
        }
        FileUtils.base64ToFile(userFacePath + fileName, faceData);
        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath + fileName);
        Image image = fastDFSClient.uploadFile(multipartFile);
        file.delete();
        User user = new User();
        user.setId(userBO.getUserId());
        user.setFaceImageBig(image.getFullPath());
        user.setFaceImage(image.getThumbPath());
        userService.update(user);
        User findUser = userService.findById(user.getId());
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(findUser, userVO);
        return JSONResult.ok(userVO);
    }

    /**
     * 更新用户
     *
     * @param userBO
     * @return
     */
    @PostMapping("update")
    public JSONResult update(@RequestBody UserBO userBO) {
        User user = new User();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());
        userService.update(user);
        return JSONResult.ok(userService.findById(user.getId()));
    }


    /**
     * 搜索好友
     *
     * @param id
     * @param username
     * @return
     */
    @PostMapping("searchUser")
    public JSONResult searchUser(String id, String username) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(username)) {
            return JSONResult.errorMsg("");
        }
        Integer status = userService.preconditionSearchFriends(id, username);
        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            User user = userService.findByUsername(username);
            UserVO userVO = new UserVO();
            BeanUtils.copyProperties(user, userVO);
            return JSONResult.ok(userVO);
        }
        return JSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
    }

    /**
     * 发送请求
     *
     * @param id
     * @param username
     * @return
     */
    @PostMapping("addFriendRequest")
    public JSONResult addFriendRequest(String id, String username) {
        if (StringUtils.isBlank(id) || StringUtils.isBlank(username)) {
            return JSONResult.errorMsg("");
        }
        Integer status = userService.preconditionSearchFriends(id, username);
        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            userService.sendFriendRequest(id, username);
            return JSONResult.ok();
        }
        return JSONResult.errorMsg(SearchFriendsStatusEnum.getMsgByKey(status));
    }


    @PostMapping("queryFriendRequest")
    public JSONResult queryFriendRequest(String id) {
        if (StringUtils.isBlank(id)) {
            return JSONResult.errorMsg("");
        }
        List<FriendRequestVO> friendRequestVOList = friendsRequestService.queryFriendRequestList(id);
        return JSONResult.ok(friendRequestVOList);
    }

    @PostMapping("operFriendRequest")
    public JSONResult operFriendRequest(String acceptUserId, String sendUserId, Integer operType) {
        if (StringUtils.isBlank(acceptUserId) || StringUtils.isBlank(sendUserId) || operType == null) {
            return JSONResult.errorMsg("");
        }
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return JSONResult.errorMsg("");
        }
        if (operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)) {
            friendsRequestService.deleteFriendRequest(acceptUserId, sendUserId);
        } else if (operType.equals(OperatorFriendRequestTypeEnum.PASS.type)) {
            friendsRequestService.passFriendRequest(acceptUserId, sendUserId);
        }
        return JSONResult.ok();
    }

    /**
     * 查询我的好友列表
     *
     * @param userId
     * @return
     */
    @PostMapping("myFriends")
    public JSONResult myFriends(String userId) {
        System.out.println(userId);
        if (StringUtils.isBlank(userId)) {
            return JSONResult.errorMsg("");
        }
        List<MyFriendsVO> myFriendsVOList = myFriendService.queryMyFriends(userId);
        return JSONResult.ok(myFriendsVOList);
    }
}
