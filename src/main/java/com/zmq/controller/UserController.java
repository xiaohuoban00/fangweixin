package com.zmq.controller;

import com.zmq.pojo.User;
import com.zmq.pojo.bo.UserBO;
import com.zmq.pojo.vo.UsersVO;
import com.zmq.service.UserService;
import com.zmq.utils.FastDFSClient;
import com.zmq.utils.FileUtils;
import com.zmq.utils.JSONResult;
import com.zmq.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
        if (findUser!=null) {
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
        UsersVO userVO = new UsersVO();
        BeanUtils.copyProperties(userResult, userVO);
        return JSONResult.ok(userVO);
    }

    @PostMapping("uploadFaceBase64")
    public JSONResult uploadFaceBase64(UserBO userBO) throws Exception {
        String faceData = userBO.getFaceData();
        String userFacePath = "C:\\"+ userBO.getUserId()+"base64.png";
        FileUtils.base64ToFile(userFacePath,faceData);
        MultipartFile multipartFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(multipartFile);
        String thump = "_150X150.";
        String[] arr = url.split("\\.");
        String thumpImgUrl = arr[0]+thump+arr[1];
        User user = new User();
        user.setId(userBO.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);
        userService.update(user);
        return JSONResult.ok(userService.findById(user.getId()));
    }
}
