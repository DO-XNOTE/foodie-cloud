package com.imooc.user.excepiton;

//import com.imooc.utils.IMOOCJSONResult;

import com.imooc.pojo.IMOOCJSONResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/**
 * @描述:
 * @Author
 */
@RestControllerAdvice
public class CustomExceptionHandler {
    /***
     * 对于 spring 来说它是一个助手类， 所以我们加上一个注解叫： @RestControllerAdvice
     *
     * 文件上传的我们做了限制，在 application.yml中
     *
     *      servlet:
     *          multipart:
     *              max-file-size: 512000     # 文件上传大小限制为500kb
     *              max-request-size: 512000  # 请求大小限制为500kb
     *
     *  如果上传的文件大于我们所做的限制，就会抛出一个异常， MaxUploadSizeExceededException 这个异常
     *  在<前端>我们应该做一个人性化的提示用户， 所以创建了这个类， CustomExceptionHandler, 从名字
     *  来看， 是一个助手类  handler , 对于 Spring 来说， 它就可以使用一个注解 @RestControllerAdvice 注解
     *  可以捕获到 MaxUploadSizeExceededException 这个异常
     */

    // 上传文件超过500k 捕获异常
    // 加上这个注解，传入异常类的字节码文件，就可以捕获了，执行下面的提示给前端用
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public IMOOCJSONResult handlerMaxUploadFile(MaxUploadSizeExceededException ex) {
        return IMOOCJSONResult.errorMsg("文件上传的大小不可以唱过500k,请压缩图片或者降低图片的质量再上传");
    }

}
