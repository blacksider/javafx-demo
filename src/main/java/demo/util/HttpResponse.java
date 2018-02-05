package demo.util;

import org.apache.http.HttpStatus;

/**
 * Created by Snart Lu on 2018/2/5.
 */
public class HttpResponse {
    private int status = HttpStatus.SC_INTERNAL_SERVER_ERROR;
    private String data;
    private Exception exception;

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Exception getException() {
        return exception;
    }

    public void setException(Exception exception) {
        this.exception = exception;
    }
}
