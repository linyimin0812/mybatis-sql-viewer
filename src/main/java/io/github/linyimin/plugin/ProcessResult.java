package io.github.linyimin.plugin;

/**
 * @author banzhe
 * @date 2022/11/24 13:30
 **/
public class ProcessResult<T> {


    /**
     * 是否成功
     */
    private boolean success;

    /**
     * 错误码
     */
    private String errorCode;

    /**
     * 错误信息
     */
    private String errorMsg;

    private T data;

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private ProcessResult(boolean success, String errorCode, String errorMsg, T data) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMsg = errorMsg;
        this.data = data;
    }


    public static <T> ProcessResult<T> success(T data){
        return new ProcessResult<>(true,null,null,data);
    }

    public static <T> ProcessResult<T> fail(String errorCode, String  errorMsg){
        return new ProcessResult<>(false,errorCode,errorMsg,null);
    }

    public static <T> ProcessResult<T> fail(String errorMsg) {
        return new ProcessResult<>(false, null, errorMsg, null);
    }

    public static <T> ProcessResult<T> fail(String errorMsg, T data) {
        return new ProcessResult<>(false, null, errorMsg, data);
    }

}
