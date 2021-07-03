package com.imooc.user.center;

import com.imooc.controller.BaseController;
import com.imooc.pojo.IMOOCJSONResult;
import com.imooc.user.pojo.Users;
import com.imooc.user.pojo.bo.center.CenterUserBO;
import com.imooc.user.resourece.FileUpload;
import com.imooc.user.service.center.CenterUserService;
import com.imooc.utils.CookieUtils;
import com.imooc.utils.DateUtil;
import com.imooc.utils.JsonUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @描述:
 * @Author
 */
@Api(value = "修改用户信息相关接口",tags = "修改用户信息相关接口")
@RestController
@RequestMapping("userInfo")
public class CenterUserController extends BaseController {

    @Autowired
    private CenterUserService centerUserService;

    @Autowired
    private FileUpload fileUpload;

    @ApiOperation(value = "用户头像修改", notes = "用户头像修改", httpMethod = "POST")
    @PostMapping("/uploadFace")
    public IMOOCJSONResult uploadFace(
            @ApiParam(name = "userId",value = "用户id", required = true)
            @RequestParam String userId,
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(name = "file",value = "用户头像", required = true)
            MultipartFile file){

        // 定义头像保存的地址
        /** String fileSpeace = IMAGE_USER_FACE_LOCATION; */
        String imageUserFaceLocation = fileUpload.getImageUserFaceLocation();  /** 获取去目录的属性集 */

        // 在路径上为每个用户增加一个useId ,用于区分不同用户上传
        String uploadPathPrifix = File.separator + userId;
        FileOutputStream fileOutputStream = null;
        try {
            // 开始文件上传
            if (file != null) {
                // 获得文件上传的文件名称
                String fileName = file.getOriginalFilename();
                if (StringUtils.isNotBlank(fileName)) {
                    // 保存的时候按照一定的规则： file-{userid}.png
                    /** 重命名 imooc-face -> ["imooc-face","png"]*/
                    String[] fileNameArr = fileName.split("\\.");
                    // 获取文件的后缀名
                    String suffix = fileNameArr[fileNameArr.length - 1];
                    /**
                     前端已经做了限制，只可以上传 .jpg, .png, 等三种格式的文件， 不可以上传其他格式的，
                     这是为了防止黑客攻击， 不做限制的话， 被上传了如 .sh， .php，等可执行的文件到,，然后被执行了，导致
                     所有的文件被访问到， 是很危险的， 作为架构师必须考虑到这种情况， 并且在 代码 review的时候要注意到
                     **/
                    if (suffix.equalsIgnoreCase("png")
                        && suffix.equalsIgnoreCase("png")
                        && suffix.equalsIgnoreCase("png")) {

                        return IMOOCJSONResult.errorMsg("图片格式不正确!");
                    }

                    // 文件名重组 --->覆盖式上传， 增量式：需要额外凭借当前时间
                    String newFileName = "face-" + userId + "." + suffix;
                    // 上传的头像最终保存的位置
                    String finalFaceFile = imageUserFaceLocation + uploadPathPrifix + File.separator + newFileName;

                    /** 用于提供给 web 服务访问的地址 */
                    uploadPathPrifix += ("/" + newFileName);

                    File outFile = new File(finalFaceFile);

                    if (outFile.getParentFile() != null) {
                        // 创建文件夹
                        outFile.getParentFile().mkdirs();
                    }
                    /** 文件输出保存的目录 */
                    fileOutputStream = new FileOutputStream(outFile);
                    InputStream inputStream = file.getInputStream();
                    /** import org.apache.commons.io.IOUtils 中的工具IOUtils */
                    IOUtils.copy(inputStream, fileOutputStream);
                }
            } else {
                return IMOOCJSONResult.errorMsg("文件不能为空");
            }
        }catch (IOException e){
            e.printStackTrace();
        } finally {
            try {
                if (fileOutputStream != null) {
                    fileOutputStream.flush();
                    fileOutputStream.close();
                }
            }catch (IOException e) {
                e.printStackTrace();
            }
        }

        /** 获得图片服务地址 */
        String imageServerUrl = fileUpload.getImageServerUrl();

        /** 由于浏览器可能存在缓存，不能及时更改过来用户头像， 加上时间戳，浏览器发现地址栏发生改变，就会及时刷新 */
        String finalUserFaceUrl = imageServerUrl + uploadPathPrifix
                + "?t=" + DateUtil.getCurrentDateString(DateUtil.DATE_PATTERN);


        /** 更新用户的头像到数据库 */
        Users userResult = centerUserService.updateUserInfo(userId, finalUserFaceUrl);

        // TODO 后续要改， 增加令牌token,会整合redis，分布式会话
//        UsersVO usersVO = convertUserVo(userResult);

//        userResult = setNullProperty(userResult);
//        CookieUtils.setCookie(request,response,"user",JsonUtils.objectToJson(usersVO),true);  // 加密后前端就看不到值了


        return IMOOCJSONResult.ok();
    }

    @ApiOperation(value = "修改用户信息", notes = "修改用户信息", httpMethod = "POST")
    @PostMapping("/update")
    public IMOOCJSONResult getCenter(
            HttpServletRequest request,
            HttpServletResponse response,
            @ApiParam(name = "userId",value = "用户id", required = true)
            @RequestParam String userId,
            @RequestBody
            @Valid CenterUserBO centerUserBO,
            BindingResult result){
        /**
         @Valid 注解是Hibeinate-validator中的可以用来校验字段参数的规则的，是由spring-boot-starer-web
         一起引入的，max min pattter，emal等等的注解，校验结果封装到 BindingResult 中
          */
        /** 校验 BindingResult 是否保存错误的验证信息， 如果由，则直接return */
        if (result.hasErrors()) {
            Map<String,String> erroMap = getErrors(result);
            return IMOOCJSONResult.errorMap(erroMap);
        }
        Users userResult = centerUserService.updateUserInfo(userId, centerUserBO);

        // TODO 后续要改， 增加令牌token,会整合redis，分布式会话
//        UsersVO usersVO = convertUserVo(userResult);


//        userResult = setNullProperty(userResult);
//        CookieUtils.setCookie(request,response,"user",JsonUtils.objectToJson(usersVO),true);  // 加密后前端就看不到值了


        return IMOOCJSONResult.ok();
    }
    /** 校验结果处理 */
    private Map<String,String> getErrors(BindingResult result) {
        Map<String,String> map = new HashMap<>();
        List<FieldError> errorList = result.getFieldErrors();
        for (FieldError error : errorList) {
            // 发生验证错误所对应的某一个属性
            String errorField = error.getField();
            // 验证错误的信息
            String errorMessage = error.getDefaultMessage();
            map.put(errorField, errorMessage);
        }
        return map;
    }

    public Users setNullProperty(Users userResult){
        userResult.setUsername(null);
        userResult.setPassword(null);
        userResult.setEmail(null);
        userResult.setCreatedTime(null);
        userResult.setUpdatedTime(null);
        userResult.setBirthday(null);
        return userResult;
    }

}
