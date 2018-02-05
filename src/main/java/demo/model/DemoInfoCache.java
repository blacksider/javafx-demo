package demo.model;

import java.io.Serializable;

/**
 * Created by Snart Lu on 2018/2/5.
 */
public class DemoInfoCache implements Serializable {
    private static final long serialVersionUID = -4833209259288828879L;
    private DemoInfo demoInfo;
    private String url;

    public DemoInfo getDemoInfo() {
        return demoInfo;
    }

    public void setDemoInfo(DemoInfo demoInfo) {
        this.demoInfo = demoInfo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
