package io.github.linyimin.plugin.sql.checker;

import io.github.linyimin.plugin.sql.checker.enums.LevelEnum;

/**
 * @author banzhe
 * @date 2022/12/05 21:30
 **/
public class Report {

    // 通过标识
    private boolean isPass;
    // 错误提示
    private String desc;

    // 等级
    private LevelEnum level;

    // 正例
    private String sample;

    public Report isPass(boolean isPass) {
        this.isPass = isPass;
        return this;
    }

    public Report desc(String desc) {
        this.desc = desc;
        return this;
    }

    public Report level(LevelEnum level) {
        this.level = level;
        return this;
    }

    public Report sample(String sample) {
        this.sample = sample;
        return this;
    }

    public boolean isPass() {
        return isPass;
    }

    public void setPass(boolean pass) {
        isPass = pass;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public LevelEnum getLevel() {
        return level;
    }

    public void setLevel(LevelEnum level) {
        this.level = level;
    }

    public String getSample() {
        return sample;
    }

    public void setSample(String sample) {
        this.sample = sample;
    }
}
